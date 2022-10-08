package net.spleefx.core.data;

import net.spleefx.arena.type.splegg.extension.SpleggUpgrade;
import net.spleefx.core.data.database.sql.SQLSerializable;
import net.spleefx.core.data.impl.SXPlayerProfile.BuilderImpl;
import net.spleefx.extension.MatchExtension;
import net.spleefx.extension.StandardExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Represents the data of a player (immutable)
 * <p>
 * This class is thread-safe.
 *
 * @see Builder
 * @see PlayerRepository
 */
public interface PlayerProfile extends/* EloComparable,*/ SQLSerializable {

    /**
     * Returns the UUID of this player
     *
     * @return The player's UUID
     */
    @NotNull
    UUID getUUID();

    boolean modified();

    /**
     * Returns the player's coins
     *
     * @return The coins
     */
    int getCoins();

    /**
     * Returns per-stat game statistics
     *
     * @return Statistics
     */
    @NotNull
    Map<GameStatType, Integer> getGameStats();

    /**
     * Returns per-extension statistics
     *
     * @return Per-game mode stats
     * @see #getExtensionStatistics(String)
     * @see #getExtensionStatistics(MatchExtension)
     */
    @NotNull
    Map<String, Map<GameStatType, Integer>> getExtensionStatistics();

    /**
     * Returns per-extension statistics
     *
     * @param extension Extension to get for
     * @return Per-game mode stats
     */
    @NotNull
    Map<GameStatType, Integer> getExtensionStatistics(@NotNull String extension);

    default Map<GameStatType, Integer> getExtensionStatistics(@NotNull MatchExtension extension) {
        Objects.requireNonNull(extension, "extension is null");
        return getExtensionStatistics(extension.getKey());
    }

    /**
     * Returns the key of the selected splegg upgrade
     *
     * @return The selected upgrade
     */
    @NotNull
    String getSelectedSpleggUpgradeKey();

    /**
     * Returns the player's selected splegg upgrade
     *
     * @return The player's splegg upgrade
     */
    SpleggUpgrade getSelectedSpleggUpgrade();

    /**
     * Returns a list of all splegg upgrade purchases
     *
     * @return All splegg upgrades purchases
     * @see #upgradeKeys()
     */
    @NotNull
    Set<SpleggUpgrade> getPurchasedSpleggUpgrades();

    /**
     * Returns all {@link #getPurchasedSpleggUpgrades()} as a list of strings
     *
     * @return ^
     */
    @NotNull
    Set<String> upgradeKeys();

    /**
     * Returns a new builder for this data
     *
     * @return A new builder
     */
    @NotNull
    Builder asBuilder();

    default boolean isSpleggUpgradeSelected(@NotNull SpleggUpgrade upgrade) {
        return getSelectedSpleggUpgradeKey().equals(upgrade.getKey());
    }

    /**
     * Creates a new builder
     *
     * @return New builder instance
     */
    static Builder builder(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return new BuilderImpl(uuid);
    }

    static PlayerProfile blankProfile(@NotNull UUID uuid) {
        return PlayerProfile.builder(uuid)
                .setCoins(50)
                .setSpleggUpgrade("default")
                .resetStats()
                .build();
    }

    /**
     * Builder class for creating instances of {@link PlayerProfile}
     */
    interface Builder {

        /**
         * Sets the amount of coins
         *
         * @param coins New value to set
         * @return This builder instance
         */
        @NotNull
        Builder setCoins(int coins);

        /**
         * Sets the player's coins by applying a function on the original amount.
         *
         * @param modification Function modification
         * @return This builder instance
         */
        @NotNull
        Builder setCoins(IntFunction<Integer> modification);

        /**
         * Adds the specified amount of coins
         *
         * @param coins Value to add
         * @return This builder instance
         */
        @NotNull
        Builder addCoins(int coins);

        /**
         * Removes/takes the specified amount of coins
         *
         * @param coins Value to remove
         * @return This builder instance
         */
        @NotNull
        Builder subtractCoins(int coins);

        /**
         * Returns the amount of coins being maintained by this builder
         *
         * @return The coins
         */
        int coins();

        /**
         * Sets all statistics to 0
         *
         * @return This builder instance
         */
        @NotNull
        Builder resetStats();

        /**
         * Replaces the specified statistic by applying a function on it
         *
         * @param statType Stat type to replace
         * @param task     Task to modify the value with
         * @return This builder instance
         */
        @NotNull
        Builder replaceStat(@NotNull GameStatType statType, @NotNull IntFunction<Integer> task);

        /**
         * Sets the statistics map from the specified mapping
         *
         * @param stats Map of stats
         * @return This builder instance
         */
        @NotNull
        Builder setStats(@NotNull Map<GameStatType, Integer> stats);

        /**
         * Sets the per-mode statistics map from the specified mapping
         *
         * @param stats Map of stats
         * @return This builder instance
         */
        @NotNull
        Builder setModeStats(@NotNull Map<String, Map<GameStatType, Integer>> stats);

        /**
         * Adds the specified upgrade as a splegg upgrade
         *
         * @param spleggUpgrade Splegg upgrade to add
         * @return This builder instance
         */
        @NotNull
        Builder addPurchase(@NotNull SpleggUpgrade spleggUpgrade);

        /**
         * Replaces the specified statistic by applying a function on it
         *
         * @param statType Stat type to replace
         * @param task     Task to modify the value with
         * @return This builder instance
         */
        @NotNull
        Builder replaceExtensionStat(@NotNull String mode, @NotNull GameStatType statType, @NotNull IntFunction<Integer> task);

        /**
         * Replaces the specified statistic by applying a function on it
         *
         * @param statType Stat type to replace
         * @param task     Task to modify the value with
         * @return This builder instance
         */
        @NotNull
        default Builder replaceExtensionStat(@NotNull MatchExtension mode, @NotNull GameStatType statType, @NotNull IntFunction<Integer> task) {
            return replaceExtensionStat(Objects.requireNonNull(mode, "extension is null!").getKey(), statType, task);
        }

        /**
         * Sets the splegg upgrade key
         *
         * @param upgrade Upgrade to set
         * @return This builder instance
         */
        @NotNull
        Builder setSpleggUpgrade(@NotNull String upgrade);

        /**
         * Sets the purchased splegg upgrades
         *
         * @param upgrades A list of upgrades
         * @return This builder instance
         */
        @NotNull
        Builder setPurchasedSpleggUpgrades(@NotNull Set<SpleggUpgrade> upgrades);

        /**
         * Sets the purchased splegg upgrades
         *
         * @param upgrades A list of upgrades
         * @return This builder instance
         */
        @NotNull
        default Builder setPurchasedSpleggUpgradesFromKeys(@NotNull Set<String> upgrades) {
            return setPurchasedSpleggUpgrades(upgrades.stream().map(StandardExtensions.SPLEGG.getUpgrades()::get).collect(Collectors.toSet()));
        }

        /**
         * Copies all data from the cloned builder into this
         *
         * @param copyFrom Builder to copy from
         * @return This builder instance
         */
        @NotNull
        Builder copy(@NotNull Builder copyFrom);

        /**
         * Creates the player data
         *
         * @return The created data
         */
        @NotNull
        PlayerProfile build();

        /**
         * Pushes this profile to the data stack, hence applying all changes inside it
         *
         * @return A future of the built profile.
         */
        @NotNull
        CompletableFuture<PlayerProfile> push();
    }
}