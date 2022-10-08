package net.spleefx.core.data.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.ToString.Exclude;
import lombok.val;
import net.spleefx.SpleefX;
import net.spleefx.arena.summary.PlayerMatchStats;
import net.spleefx.arena.type.splegg.extension.SpleggUpgrade;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.data.GameStatType;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.core.data.PlayerRepository;
import net.spleefx.core.data.impl.SXPlayerProfile.FastAdapter;
import net.spleefx.extension.StandardExtensions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static net.spleefx.core.data.PlayerCacheManager.GSON;
import static net.spleefx.core.data.PlayerCacheManager.UPGRADES_TYPE;
import static net.spleefx.util.Util.n;

@ToString
@EqualsAndHashCode
@JsonAdapter(FastAdapter.class)
public class SXPlayerProfile implements PlayerProfile {

    private final UUID uuid;
    @Exclude private transient final OfflinePlayer offlineCopy;
    private final int coins;
    private final ImmutableMap<GameStatType, Integer> stats;
    private final ImmutableMap<String, Map<GameStatType, Integer>> modeStats;
//    private final @Nullable ImmutableMap<String, Map<GameStatType, Integer>> arenaStats;

    private final ImmutableSet<SpleggUpgrade> spleggUpgrades;
    private transient final Set<String> spleggUpgradesKeys;
    private final SpleggUpgrade spleggUpgrade;
    private String selectedSpleggUpgrade;
    //    private final int eloScore;
    protected boolean modified = false;

    public SXPlayerProfile(
            UUID uuid,
            int coins,
            ImmutableMap<GameStatType, Integer> stats,
            ImmutableMap<String, Map<GameStatType, Integer>> modeStats,
            ImmutableSet<SpleggUpgrade> spleggUpgrades,
            String selectedSpleggUpgrade) {
        this.uuid = uuid;
        offlineCopy = Bukkit.getOfflinePlayer(uuid);
        if (SpleefXConfig.otherEconomy()) {
            coins = 0;
        }
        this.coins = coins;
        this.stats = n(stats, "stats");
        this.modeStats = n(modeStats, "modeStats");
        if (selectedSpleggUpgrade.equals("default"))
            this.spleggUpgrades = new ImmutableSet.Builder<SpleggUpgrade>()
                    .addAll(n(spleggUpgrades, "spleggUpgrades"))
                    .addAll(StandardExtensions.SPLEGG.getUpgrades().values().stream().filter(SpleggUpgrade::isDefault).collect(Collectors.toList()))
                    .build();
        else
            this.spleggUpgrades = n(spleggUpgrades, "spleggUpgrades");
        spleggUpgradesKeys = spleggUpgrades.stream().map(SpleggUpgrade::getKey).collect(Collectors.toSet());
        this.selectedSpleggUpgrade = n(selectedSpleggUpgrade, "selectedSpleggUpgrade");
//        eloScore = stats.getOrDefault(GameStatType.WINS, 0) - stats.getOrDefault(GameStatType.LOSSES, 0);
        spleggUpgrade = SpleggUpgrade.get(getSelectedSpleggUpgradeKey());
    }

    @Override public int getCoins() {
        if (SpleefXConfig.otherEconomy())
            return (int) SpleefX.getSpleefX().getVaultHandler().getCoins(offlineCopy);
        return coins;
    }

    @Override public @NotNull UUID getUUID() {
        return uuid;
    }

    @Override public boolean modified() {
        return modified;
    }

    @Override public @NotNull Map<GameStatType, Integer> getGameStats() {
        return stats;
    }

    @Override public @NotNull Map<String, Map<GameStatType, Integer>> getExtensionStatistics() {
        return modeStats;
    }

    @Override
    public @NotNull Map<GameStatType, Integer> getExtensionStatistics(@NotNull String extension) {
        return modeStats.getOrDefault(n(extension, "extension is null!"), PlayerMatchStats.ZERO);
    }

    @Override public @NotNull String getSelectedSpleggUpgradeKey() {
        if (selectedSpleggUpgrade.equals("default"))
            return selectedSpleggUpgrade = StandardExtensions.SPLEGG.getUpgrades().values().stream()
                    .filter(SpleggUpgrade::isDefault).findFirst().get().getKey();
        return selectedSpleggUpgrade;

    }

    @Override public @NotNull SpleggUpgrade getSelectedSpleggUpgrade() {
        return spleggUpgrade;
    }

    @Override public @NotNull Set<SpleggUpgrade> getPurchasedSpleggUpgrades() {
        return spleggUpgrades;
    }

    @Override public @NotNull Set<String> upgradeKeys() {
        return spleggUpgradesKeys;
    }

    @Override public @NotNull Builder asBuilder() {
        modified = true;
        return new BuilderImpl(this);
    }

    @Override public @NotNull String asPlaceholders() {
        return "(?, ?, ?, ?, ?, ?)";
    }

    @Override
    @SuppressWarnings("PointlessArithmeticExpression") // more readable
    public int passToStatement(@NotNull UUID uuid, @NotNull PreparedStatement statement, final int index) throws SQLException {
        statement.setString(index + 0, uuid.toString());
        statement.setInt(   index + 1, coins);
        statement.setString(index + 2, selectedSpleggUpgrade);
        statement.setString(index + 3, GSON.toJson(upgradeKeys()));
        statement.setString(index + 4, GSON.toJson(stats));
        statement.setString(index + 5, GSON.toJson(modeStats));
        return 6;
    }

    public static class BuilderImpl implements Builder {

        private static final Set<SpleggUpgrade> nullUpgrades = Collections.singleton(null);

        protected boolean modified;
        protected final UUID uuid;
        protected final OfflinePlayer offlineCopy;
        protected int coins = 0;
        protected Map<GameStatType, Integer> stats = new HashMap<>();
        protected Map<String, Map<GameStatType, Integer>> modeStats = new HashMap<>();
        protected Set<SpleggUpgrade> spleggUpgrades = new LinkedHashSet<>();

        protected String spleggUpgrade;

        public BuilderImpl(UUID uuid) {
            this.uuid = uuid;
            offlineCopy = Bukkit.getOfflinePlayer(uuid);
        }

        public BuilderImpl(SXPlayerProfile data) {
            uuid = data.uuid;
            coins = data.getCoins();
            stats = new HashMap<>(data.stats);
            modeStats = new HashMap<>(data.modeStats);
            spleggUpgrades = new LinkedHashSet<>(data.spleggUpgrades);
            spleggUpgrade = data.selectedSpleggUpgrade;
            offlineCopy = Bukkit.getOfflinePlayer(uuid);
            modified = true;
        }

        @Override public @NotNull Builder setCoins(int coins) {
            this.coins = coins;
            return this;
        }

        @Override public @NotNull Builder setCoins(IntFunction<Integer> modification) {
            coins = modification.apply(coins);
            return this;
        }

        @Override public @NotNull Builder addCoins(int coins) {
            if (SpleefXConfig.otherEconomy()) {
                if (coins < 0)
                    SpleefX.getSpleefX().getVaultHandler().withdraw(offlineCopy, Math.abs(coins));
                else
                    SpleefX.getSpleefX().getVaultHandler().add(offlineCopy, coins);
                return this;
            }
            this.coins += coins;
            return this;
        }

        @Override public @NotNull Builder subtractCoins(int coins) {
            if (SpleefXConfig.otherEconomy()) {
                SpleefX.getSpleefX().getVaultHandler().withdraw(offlineCopy, coins);
                return this;
            }
            this.coins -= coins;
            return this;
        }

        @Override public int coins() {
            if (SpleefXConfig.otherEconomy()) {
                coins = (int) SpleefX.getSpleefX().getVaultHandler().getCoins(offlineCopy);
            }
            return coins;
        }

        @Override public @NotNull Builder resetStats() {
            for (GameStatType type : GameStatType.values)
                stats.put(type, 0);
            modeStats.replaceAll((k, v) -> {
                v.replaceAll((n, e) -> 0);
                return v;
            });
            return this;
        }

        @Override public @NotNull Builder replaceStat(@NotNull GameStatType statType, @NotNull IntFunction<Integer> task) {
            n(statType);
            n(task);
            stats.compute(statType, (stat, value) -> task.apply(value == null ? 0 : value));
            return this;
        }

        @Override public @NotNull Builder setStats(@NotNull Map<GameStatType, Integer> stats) {
            this.stats = n(stats, "stats");
            return this;
        }

        @Override public @NotNull Builder setModeStats(@NotNull Map<String, Map<GameStatType, Integer>> stats) {
            n(stats, "stats");
            modeStats = stats;
            return this;
        }

        @Override public @NotNull Builder addPurchase(@NotNull SpleggUpgrade spleggUpgrade) {
            n(spleggUpgrade, "Splegg upgrade");
            spleggUpgrades.add(spleggUpgrade);
            return this;
        }

        @Override public @NotNull Builder replaceExtensionStat(@NotNull String mode, @NotNull GameStatType statType, @NotNull IntFunction<Integer> task) {
            n(mode);
            n(statType);
            n(task);
            val map = modeStats.computeIfAbsent(mode, k -> new HashMap<>());
            map.compute(statType, (stat, value) -> task.apply(value == null ? 0 : value));
            replaceStat(statType, task);
            return this;
        }

        @Override public @NotNull Builder setSpleggUpgrade(@NotNull String upgrade) {
            spleggUpgrade = n(upgrade, "upgrade");
            return this;
        }

        @Override public @NotNull Builder setPurchasedSpleggUpgrades(@NotNull Set<SpleggUpgrade> upgrades) {
            spleggUpgrades = n(upgrades, "upgrades");
            return this;
        }

        @Override
        public @NotNull CompletableFuture<PlayerProfile> push() {
            return PlayerRepository.REPOSITORY.apply(uuid, (profile, builder) -> builder.copy(this));
        }

        @Override
        public @NotNull Builder copy(@NotNull Builder copyFrom) {
            n(copyFrom, "copyFrom is null!");
            BuilderImpl data = (BuilderImpl) copyFrom;
            modified = data.modified;
            coins = data.coins();
            stats = new HashMap<>(data.stats);
            modeStats = new HashMap<>(data.modeStats);
            spleggUpgrades = new LinkedHashSet<>(data.spleggUpgrades);
            spleggUpgrade = data.spleggUpgrade;
            return this;
        }

        @Override public @NotNull PlayerProfile build() {
            if (spleggUpgrades.equals(nullUpgrades))
                spleggUpgrades.clear();
            SXPlayerProfile prof = new SXPlayerProfile(
                    uuid,
                    coins,
                    ImmutableMap.copyOf(stats),
                    ImmutableMap.copyOf(modeStats),
                    ImmutableSet.copyOf(spleggUpgrades),
                    spleggUpgrade);
            prof.modified = modified;
            return prof;
        }

    }

    public static class ImmutableMapAdapter<K, V> implements JsonDeserializer<ImmutableMap<K, V>> {

        private JsonDeserializer<Map<K, V>> delegate;

        public ImmutableMapAdapter(@Nullable JsonDeserializer<Map<K, V>> delegate) {
            this.delegate = delegate;
        }

        public ImmutableMapAdapter() {
            this(null);
        }

        @Override
        public ImmutableMap<K, V> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            Map<K, V> map = delegate != null ? delegate.deserialize(jsonElement, type, context) : context.deserialize(jsonElement, new TypeToken<LinkedHashMap<K, V>>() {
            }.getType());
            return ImmutableMap.copyOf(map);
        }
    }

    public static class FastAdapter implements TypeAdapterFactory {

        private static final Type STATS = new TypeToken<HashMap<GameStatType, Integer>>() {
        }.getType();

        private static final Type MODE_STATS = new TypeToken<HashMap<String, HashMap<GameStatType, Integer>>>() {
        }.getType();

        @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            if (!PlayerProfile.class.isAssignableFrom(typeToken.getRawType())) return null;
            return new TypeAdapter<T>() {
                @Override public void write(JsonWriter out, T t) throws IOException {
                    PlayerProfile p = ((PlayerProfile) t);
                    out.beginObject();
                    out.name("uuid").value(p.getUUID().toString());
                    out.name("coins").value(p.getCoins());
                    out.name("stats").jsonValue(gson.toJson(p.getGameStats()));
                    out.name("modeStats").jsonValue(gson.toJson(p.getExtensionStatistics()));
                    out.name("spleggUpgrades").jsonValue(gson.toJson(p.upgradeKeys()));
                    out.name("selectedSpleggUpgrade").value(p.getSelectedSpleggUpgradeKey());
                    out.endObject();
                }

                @SuppressWarnings("ConstantConditions")
                @Override public T read(JsonReader in) throws IOException {
                    in.beginObject();
                    Builder builder = null;
                    while (in.hasNext()) {
                        try {
                            switch (in.nextName()) {
                                case "uuid":
                                    builder = PlayerProfile.builder(UUID.fromString(in.nextString()));
                                    break;
                                case "coins":
                                    builder.setCoins(in.nextInt());
                                    break;
                                case "stats":
                                    builder.setStats(gson.fromJson(in, STATS));
                                    break;
                                case "modeStats":
                                    builder.setModeStats(gson.fromJson(in, MODE_STATS));
                                    break;
                                case "spleggUpgrades":
                                case "purchasedSpleggUpgrades":
                                    builder.setPurchasedSpleggUpgrades(gson.fromJson(in, UPGRADES_TYPE));
                                    break;
                                case "selectedSpleggUpgrade":
                                    builder.setSpleggUpgrade(in.nextString());
                                    break;
                            }
                        } catch (IllegalStateException e) {
                            in.beginObject();
                            in.endObject();
                        }
                    }
                    in.endObject();
                    return (T) builder.build();
                }
            };
        }
    }

//    @Override public int getElo() {
//        return eloScore;
//    }
}