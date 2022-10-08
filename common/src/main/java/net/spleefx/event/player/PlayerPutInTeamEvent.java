package net.spleefx.event.player;

import lombok.Getter;
import lombok.Setter;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.team.ArenaTeam;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Invoked when a player is put in a team. Use this event to manipulate team selection
 */
@Getter
public class PlayerPutInTeamEvent extends PlayerArenaEvent {

    @Setter
    @NotNull
    private ArenaTeam team;

    public PlayerPutInTeamEvent(@NotNull Player player, @NotNull MatchArena arena, @NotNull ArenaTeam team) {
        super(player, arena);
        this.team = team;
    }
}
