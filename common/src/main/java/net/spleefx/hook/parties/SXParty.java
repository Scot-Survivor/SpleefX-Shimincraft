package net.spleefx.hook.parties;

import lombok.AllArgsConstructor;
import net.spleefx.arena.player.MatchPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@AllArgsConstructor
public class SXParty implements Iterable<MatchPlayer> {

    public static final SXParty NONE = new SXParty(Collections.emptyList(), Collections.emptyList()) {
        @Override public boolean isLeader(@NotNull MatchPlayer player) {
            return false;
        }

        @Override public @NotNull List<MatchPlayer> getPlayers() {
            return Collections.emptyList();
        }

        @Override public boolean isReal() {
            return false;
        }

        @Override public @NotNull Iterator<MatchPlayer> iterator() {
            return Collections.emptyIterator();
        }
    };

    private final List<MatchPlayer> players, leaders;

    public boolean isLeader(@NotNull MatchPlayer player) {
        return leaders.contains(player);
    }

    public @NotNull List<MatchPlayer> getPlayers() {
        return players;
    }

    public boolean isReal() {
        return !players.isEmpty();
    }

    @NotNull @Override public Iterator<MatchPlayer> iterator() {
        return players.iterator();
    }
}
