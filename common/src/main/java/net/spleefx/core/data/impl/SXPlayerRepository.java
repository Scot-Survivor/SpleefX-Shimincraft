package net.spleefx.core.data.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import net.spleefx.SpleefX;
import net.spleefx.config.ConfigOption;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.data.GameStatType;
import net.spleefx.core.data.LeaderboardTopper;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.core.data.PlayerProfile.Builder;
import net.spleefx.core.data.PlayerRepository;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static java.util.Collections.reverseOrder;
import static java.util.Objects.requireNonNull;

public class SXPlayerRepository implements PlayerRepository {

    private final ForwardingCacheManager forwardingCacheManager = new ForwardingCacheManager();

    private final LoadingCache<UUID, PlayerProfile> cache;

    public SXPlayerRepository() {
        Caffeine<UUID, PlayerProfile> builder = Caffeine.newBuilder()
                .maximumSize(SpleefXConfig.MAX_CACHE_SIZE.get())
                .executor(SpleefX.POOL)
                .writer(forwardingCacheManager);
        if (!SpleefXConfig.LEADERBOARDS.get())
            builder.expireAfterAccess(Duration.ofHours(6));
        cache = builder.build(forwardingCacheManager);
    }

    private Map<GameStatType, ImmutableSet<LeaderboardTopper>> top = new ConcurrentHashMap<>();
    private Map<GameStatType, Map<String, ImmutableSet<LeaderboardTopper>>> topByExtension = new ConcurrentHashMap<>();
    private boolean initialized = false;

    @Override
    public void insert(@NotNull UUID uuid, @NotNull PlayerProfile data) {
        cache.put(uuid, data);
    }

    @Override
    public @NotNull List<LeaderboardTopper> getTopPlayers(@NotNull GameStatType stat, @Nullable MatchExtension extension) {
        requireNonNull(stat, "stat");
        if (extension == null)
            return new ArrayList<>(getTop().getOrDefault(stat, ImmutableSet.of()));
        return new ArrayList<>(getTopByExtension().getOrDefault(stat, Collections.emptyMap()).getOrDefault(extension.getKey(), ImmutableSet.of()));
    }

    @Override
    public @Nullable PlayerProfile lookup(@NotNull UUID uuid) {
        return cache.getIfPresent(uuid);
    }

    @Override
    public @NotNull CompletableFuture<PlayerProfile> getOrQuery(@NotNull UUID uuid) {
        CompletableFuture<PlayerProfile> future = new CompletableFuture<>();
        ForwardingCacheManager.delegate().executor().execute(() -> {
            try {
                future.complete(cache.get(uuid));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return future;
    }

    @Override public @NotNull CompletableFuture<PlayerProfile> apply(@NotNull UUID uuid, @NotNull BiConsumer<PlayerProfile, Builder> modification) {
        CompletableFuture<PlayerProfile> f = new CompletableFuture<>();
        ForwardingCacheManager.delegate().executor().execute(() -> {
            try {
                cache.asMap().computeIfPresent(uuid, (k, v) -> {
                    try {
                        Builder builder = v.asBuilder();
                        modification.accept(v, builder);
                        PlayerProfile profile = builder.build();
                        f.complete(profile);
                        return profile;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });
                f.complete(null);
            } catch (Exception e) {
                e.printStackTrace();
                f.complete(null);
            }
        });
        return f;
    }

    public void init(@NotNull SpleefX plugin) {
        if (!initialized) {
            ForwardingCacheManager.delegate().init(requireNonNull(plugin, "Plugin is null!"));
            initialized = true;
        }
    }

    @Override
    public void shutdown(@NotNull SpleefX plugin) {
        ForwardingCacheManager.delegate().shutdown(requireNonNull(plugin, "Plugin is null!"));
    }

    @Override public void cacheAll() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ForwardingCacheManager.delegate().executor().execute(() -> {
            try {
                ForwardingCacheManager.delegate().cacheAll(cache);
                future.complete(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        future.thenAccept(v -> {
            SpleefX.logger().info("Successfully loaded data of " + cache.asMap().size() + " player(s).");
            Bukkit.getScheduler().runTaskTimer(SpleefX.getPlugin(), this::sortLeaderboards, 0, 600 * 20);
        });
    }

    private void sortLeaderboards() {
        if (!SpleefXConfig.LEADERBOARDS.get()) return;
        CompletableFuture<Map<GameStatType, ImmutableSet<LeaderboardTopper>>> topFuture = new CompletableFuture<>();
        CompletableFuture<Map<GameStatType, Map<String, ImmutableSet<LeaderboardTopper>>>> topByExtFuture = new CompletableFuture<>();

        SpleefX.POOL.submit(() -> {
            try {
                Map<GameStatType, Map<String, ImmutableSet<LeaderboardTopper>>> topByExt = new HashMap<>();

                Map<GameStatType, ImmutableSet<LeaderboardTopper>> top = new HashMap<>();

                for (GameStatType stat : GameStatType.values) {
                    List<Entry<UUID, PlayerProfile>> profiles = new ArrayList<>(cache.asMap().entrySet());
                    profiles.sort(reverseOrder(Comparator.comparingInt(e -> e.getValue().getGameStats().getOrDefault(stat, 0))));
                    ImmutableSet<LeaderboardTopper> toppers = profiles.stream()
                            .map(player -> LeaderboardTopper.of(player.getKey(), player.getValue().getGameStats().getOrDefault(stat, 0)))
                            .collect(ConfigOption.toImmutableSet());
                    top.put(stat, toppers);

                    Map<String, ImmutableSet<LeaderboardTopper>> tops = new HashMap<>();
                    for (MatchExtension extension : Extensions.getExtensions()) {
                        ImmutableSet<LeaderboardTopper> topEx = cache.asMap().entrySet().stream()
                                .sorted(reverseOrder(Comparator.comparingInt(e -> e.getValue().getExtensionStatistics(extension).getOrDefault(stat, 0))))
                                .map(e -> LeaderboardTopper.of(e.getKey(), e.getValue().getExtensionStatistics(extension).getOrDefault(stat, 0)))
                                .collect(ConfigOption.toImmutableSet());
                        tops.put(extension.getKey(), topEx);
                    }
                    topByExt.put(stat, tops);
                    topByExtFuture.complete(topByExt);
                    topFuture.complete(top);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

        topFuture.thenAcceptAsync(i -> top = i);
        topByExtFuture.thenAcceptAsync(i -> topByExtension = i);
    }

    public Map<GameStatType, ImmutableSet<LeaderboardTopper>> getTop() {
        return top;
    }

    public Map<GameStatType, Map<String, ImmutableSet<LeaderboardTopper>>> getTopByExtension() {
        return topByExtension;
    }

    @Override public void save() {
        ForwardingCacheManager.delegate().executor()
                .execute(() -> ForwardingCacheManager.delegate().writeAll(cache.asMap(), false));
    }

    public @NotNull LoadingCache<UUID, PlayerProfile> getCache() {
        return cache;
    }

    @Override public void saveOnMainThread() {
        ForwardingCacheManager.delegate().writeAll(cache.asMap(), false);
    }

}