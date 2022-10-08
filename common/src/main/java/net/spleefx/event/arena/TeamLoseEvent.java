package net.spleefx.event.arena;

import net.spleefx.arena.MatchArena;
import net.spleefx.arena.team.ArenaTeam.ImmutableArenaTeam;

public class TeamLoseEvent extends ArenaEvent {

    private final ImmutableArenaTeam team;

    public TeamLoseEvent(MatchArena arena, ImmutableArenaTeam team) {
        super(arena);
        this.team = team;
    }

    public ImmutableArenaTeam getTeam() {
        return team;
    }
}