package net.spleefx.event.arena.end;

import com.google.common.collect.ImmutableList;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.summary.GameSummary;

public class PostArenaEndEvent extends ArenaEndEvent {

    private final ImmutableList<MatchPlayer> trackedPlayers;
    private final GameSummary gameSummary;

    public PostArenaEndEvent(MatchArena arena, boolean forcibly, ImmutableList<MatchPlayer> trackedPlayers, GameSummary gameSummary) {
        super(arena, forcibly);
        this.trackedPlayers = trackedPlayers;
        this.gameSummary = gameSummary;
    }

    public ImmutableList<MatchPlayer> getTrackedPlayers() {
        return trackedPlayers;
    }

    public GameSummary getGameSummary() {
        return gameSummary;
    }
}
