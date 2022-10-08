package net.spleefx.event.player;

import net.spleefx.arena.MatchArena;
import org.bukkit.entity.Player;

/**
 * Invoked when a player loses
 */
public class PlayerLoseEvent extends PlayerArenaEvent {

    public PlayerLoseEvent(Player player, MatchArena arena) {
        super(player, arena);
    }
}
