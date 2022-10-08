package net.spleefx.event.arena;

import net.spleefx.arena.MatchArena;
import net.spleefx.arena.team.ArenaTeam.ImmutableArenaTeam;
import org.jetbrains.annotations.NotNull;

public class TeamWinEvent extends ArenaEvent {

    private final ImmutableArenaTeam team;

    public TeamWinEvent(MatchArena arena, ImmutableArenaTeam team) {
        super(arena);
        this.team = team;
    }

    public @NotNull ImmutableArenaTeam getTeam() {
        return team;
    }
}