package net.spleefx.event.player;

import lombok.Getter;
import net.spleefx.arena.MatchArena;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Invoked when a player destroys a block in an arena
 */
@Getter
public class PlayerDestroyBlockInArenaEvent extends PlayerArenaEvent {

    /**
     * The location of the broken block
     */
    @NotNull
    private final Block location;

    /**
     * The break context. See {@link BreakContext}
     */
    @NotNull
    private final BreakContext context;

    public PlayerDestroyBlockInArenaEvent(Player player, @NotNull MatchArena arena, @NotNull Block location, @NotNull BreakContext context) {
        super(player, arena);
        this.location = location;
        this.context = context;
    }

    /**
     * Represents how the block was broken
     */
    public enum BreakContext {

        /**
         * The block was mined manually (e.g a tool) or in a spleef snowball
         */
        MINED,

        /**
         * The block was shot by the splegg shooting tool
         */
        SHOT_SPLEGG,

        /**
         * The block was shot by bow spleef's bow
         */
        SHOT_BOW_SPLEEF,

        /**
         * The block was broken in an undefined context
         */
        OTHER
    }
}