package net.spleefx.extension.ability;

import com.google.common.collect.ImmutableList;

/**
 * Represents a bow spleef ability
 */
public enum GameAbility {

    /**
     * Double jump ability
     */
    DOUBLE_JUMP("double_jumps"),

    /**
     * Triple arrows ability
     */
    TRIPLE_ARROWS("triple_arrows"),

    /**
     * Rippler (launches nearby players into the air)
     */
    RIPPLER("rippler"),

    /**
     * Arrow volley ability (shoot arrows in all directions)
     */
    ARROW_VOLLEY("arrow_volley");

    public static final GameAbility[] values = values();

    public final ImmutableList<String> placeholders;

    GameAbility(String... placeholders) {
        this.placeholders = ImmutableList.copyOf(placeholders);
    }
}