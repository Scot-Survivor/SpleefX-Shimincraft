package net.spleefx.core.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Represents a game statistic type
 */
public enum GameStatType {

    /**
     * The total amount of games played
     */
    GAMES_PLAYED(1),

    /**
     * The wins
     */
    WINS(10),

    /**
     * The losses
     */
    LOSSES(-10),

    /**
     * The draws
     */
    DRAWS(0),

    /**
     * The blocks that were mined or destroyed
     */
    BLOCKS_MINED(0.01),

    /**
     * Splegg shots
     */
    SPLEGG_SHOTS(0.01),

    /**
     * Bow spleef arrow shots
     */
    BOW_SPLEEF_SHOTS(0.1),

    /**
     * The player score
     */
    SCORE(0);

    public static final GameStatType[] values = values();
    private static final Map<String, GameStatType> BY_NAME = new HashMap<>();

    private final String name;

    private final double eloScore;

    GameStatType(double eloScore) {
        name = name().toLowerCase();
        this.eloScore = eloScore;
    }

    @Nullable
    public static GameStatType fromName(@NotNull String name) {
        requireNonNull(name, "name");
        return BY_NAME.get(name.toLowerCase());
    }

    static {
        for (GameStatType value : values) BY_NAME.put(value.name.toLowerCase(), value);
    }
}