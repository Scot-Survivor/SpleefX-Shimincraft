package net.spleefx.arena.summary;

import com.google.common.collect.ImmutableMap;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.core.data.GameStatType;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.extension.MatchExtension;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class PlayerMatchStats {

    public static final ImmutableMap<GameStatType, Integer> ZERO = ImmutableMap.copyOf(Arrays.stream(GameStatType.values).collect(Collectors.toMap(stat -> stat, count -> 0)));

    public final ImmutableMap<GameStatType, Integer> stats;
    public final int coins;

    PlayerMatchStats(ImmutableMap<GameStatType, Integer> stats, int coins) {
        this.stats = stats;
        this.coins = coins;
    }

    public int get(@NotNull GameStatType statType) {
        return stats.getOrDefault(statType, 0);
    }

    public CompletableFuture<PlayerProfile> push(@NotNull MatchPlayer player, @NotNull MatchExtension extension) {
        return player.changeStats(builder -> {
            for (Entry<GameStatType, Integer> storedStat : stats.entrySet()) {
                builder.replaceExtensionStat(extension, storedStat.getKey(), i -> i + storedStat.getValue());
            }
            builder.addCoins(coins);
        });
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<GameStatType, Integer> stats = new HashMap<>();
        private int coins = 0;

        Builder() {
            stats.putAll(ZERO);
        }

        public Builder win() {
            return inc(GameStatType.WINS);
        }

        public Builder gamePlayed() {
            return inc(GameStatType.GAMES_PLAYED);
        }

        public Builder lose() {
            return inc(GameStatType.LOSSES);
        }

        public Builder draw() {
            return inc(GameStatType.DRAWS);
        }

        public Builder blockMined() {
            return inc(GameStatType.BLOCKS_MINED);
        }

        public Builder spleggShot() {
            return inc(GameStatType.SPLEGG_SHOTS);
        }

        public Builder bowSpleefShot() {
            return inc(GameStatType.BOW_SPLEEF_SHOTS);
        }

        public Builder coins(@NotNull IntFunction<Integer> mod) {
            this.coins = mod.apply(coins);
            return this;
        }

        public Builder inc(@NotNull GameStatType stat) {
            stats.computeIfPresent(stat, (k, old) -> old + 1);
            return this;
        }

        @NotNull
        public PlayerMatchStats build() {
            return new PlayerMatchStats(ImmutableMap.copyOf(stats), coins);
        }

    }

}
