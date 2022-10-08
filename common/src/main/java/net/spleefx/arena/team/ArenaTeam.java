package net.spleefx.arena.team;

import com.google.common.collect.ImmutableSet;
import net.spleefx.arena.engine.TeamsArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Team data in arena
 */
public class ArenaTeam implements/* EloComparable,*/ Comparable<ArenaTeam> {

    private final TeamsArenaEngine engine;
    public final MatchTeam team;
    public final Set<MatchPlayer> alive = new HashSet<>();
    public final Set<MatchPlayer> all = new HashSet<>();

    public ArenaTeam(TeamsArenaEngine engine, MatchTeam team) {
        this.engine = engine;
        this.team = team;
    }

    public void flush() {
        alive.clear();
        all.clear();
    }

    public boolean isFull() {
        return all.size() >= engine.getArena().getMembersPerTeam();
    }

    public boolean isAlive() {
        return alive.size() > 0;
    }

    /*
    @Override public int getElo() {
        int gameAverage = engine.getPlayers().stream().mapToInt(MatchPlayer::getElo).sum() / engine.getPlayers().size();
        return gameAverage * all.size();
    }
*/

    @Override public int compareTo(@NotNull ArenaTeam o) {
        return Integer.compare(all.size(), o.all.size());
    }

    @Override public String toString() {
        return String.format("ArenaTeam{color=%s, all=%s}, hashCode() = %s", team.getKey(), all.stream().map(MatchPlayer::name).collect(Collectors.joining(", ")), hashCode());
    }

    public static class ImmutableArenaTeam {

        public final ImmutableSet<MatchPlayer> all;

        private ImmutableArenaTeam(Set<MatchPlayer> all) {
            this.all = ImmutableSet.copyOf(all);
        }

        public static ImmutableArenaTeam from(@NotNull ArenaTeam team) {
            return new ImmutableArenaTeam(team.all);
        }

    }

}

