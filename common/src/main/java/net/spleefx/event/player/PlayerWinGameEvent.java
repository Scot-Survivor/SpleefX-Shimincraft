package net.spleefx.event.player;

import lombok.Getter;
import net.spleefx.arena.MatchArena;
import org.bukkit.entity.Player;

/**
 * Invoked when a player wins a game
 */
@Getter
public class PlayerWinGameEvent extends PlayerArenaEvent {

    public PlayerWinGameEvent(Player p, MatchArena arena) {
        super(p, arena);
    }
}
