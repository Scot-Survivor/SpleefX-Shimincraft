package net.spleefx.arena.engine;

import net.spleefx.SpleefX;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.model.CommandExecution;
import net.spleefx.model.Position;
import net.spleefx.util.message.message.Message;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import static net.spleefx.util.Placeholders.firstNotNull;

public class FFAArenaEngine extends AbstractArenaEngine {

    private final List<MatchPlayer> deadPlayers = new ArrayList<>();

    public FFAArenaEngine(@NotNull MatchArena arena) {
        super(arena);
    }

    @Override protected CompletableFuture<Void> prepare0(MatchPlayer player) {
        Position position = firstNotNull(arena.getLobby(), arena.getFfaSettings().getLobby(arena, player), arena.getFfaSettings().getSpawnPoint(arena, player));
        player.teleport(position);
        return super.prepare0(player);
    }

    @Override protected CompletableFuture<Void> prepareForGame0(MatchPlayer player) {
        player.teleport(arena.getFfaSettings().getSpawnPoint(arena, player));
        return super.prepareForGame0(player);
    }

    @Override protected void onPlayerJoin(MatchPlayer player) {
        super.onPlayerJoin(player);
        for (MatchPlayer pl : this) {
            pl.msg(Message.PLAYER_JOINED_FFA, player);
        }
    }

    @Override protected void onPlayerEliminate(MatchPlayer player) {
        deadPlayers.add(player);
        getStats(player).lose();
        for (MatchPlayer tr : total()) {
            Message.PLAYER_LOST_FFA.reply(tr, arena, extension, player);
        }
    }

    @Override protected void onPlayerWin(MatchPlayer winner) {
        deadPlayers.add(winner);
        for (MatchPlayer tr : total()) {
            tr.msg(Message.PLAYER_WINS_FFA, winner, arena, extension);
        }
    }

    @Override public boolean isReady() {
        return (arena.getMaxPlayerCount() >= 2) && arena.getFfaSettings().getSpawnPoints().size() >= arena.getMaxPlayerCount();
    }

    protected void runWinningCommands() {
        // run reward commands
        Collections.reverse(deadPlayers);
        List<MatchPlayer> deadPlayers = new ArrayList<>(this.deadPlayers);
        SpleefX.nextTick(20, () -> {
            for (Entry<Integer, CommandExecution> reward : extension.getRunCommandsForFFAWinners().entrySet()) {
                try {
                    MatchPlayer player = deadPlayers.remove(reward.getKey() - 1);
                    reward.getValue().execute(player.player(), arena);
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
            CommandExecution reward = extension.getRunCommandsForTeamWinners().get(-1);
            if (reward != null) {
                deadPlayers.forEach(t -> reward.execute(t.player(), arena));
            }
        });

    }

    @Override protected void onPreEnd(boolean force) {
        if (!force)
            runWinningCommands();
    }

    @Override protected void onPostEnd(boolean force) {
        deadPlayers.clear();
    }

    @Override protected boolean isSingleUnitAlive() {
        return players.size() == 1;
    }

    @Override protected void onContextLoad(MatchPlayer player) {
        arena.getFfaSettings().remove(player);
    }
}
