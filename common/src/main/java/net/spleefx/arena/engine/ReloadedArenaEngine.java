package net.spleefx.arena.engine;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.summary.PlayerMatchStats;
import net.spleefx.arena.team.ArenaTeam;
import net.spleefx.core.command.Prefixable;
import net.spleefx.core.scoreboard.ScoreboardType;
import net.spleefx.extension.MatchExtension;
import net.spleefx.powerup.api.PowerupLifecycle;
import net.spleefx.spectate.Spectating.SpectatingCause;
import net.spleefx.util.message.message.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class ReloadedArenaEngine implements Prefixable, Iterable<MatchPlayer> {

    public abstract MatchArena getArena();

    public abstract ArenaStage getStage();

    public abstract void setStage(@NotNull ArenaStage newStage);

    public JoinResult playerJoin(@NotNull MatchPlayer player, boolean force) {
        return playerJoin(player, force, true);
    }

    public abstract JoinResult playerJoin(@NotNull MatchPlayer player, boolean force, boolean doJoin);

    public abstract void playerLeave(@NotNull MatchPlayer player, boolean disconnect, boolean urgent);

    protected abstract void gameStart(@NotNull MatchPlayer player);

    public abstract void playerSpectate(@NotNull MatchPlayer player, SpectatingCause cause);

    public abstract void playerEliminated(@NotNull MatchPlayer player, boolean disconnect);

    public abstract void playerWin(@NotNull MatchPlayer player);

    public abstract void playerLeaveSpectating(@NotNull MatchPlayer player);

    protected abstract PreGamePlayerData storeData(@NotNull MatchPlayer player);

    protected abstract CompletableFuture<Void> restoreData(@NotNull MatchPlayer player, boolean urgent);

    protected abstract void renderSidebar(@NotNull MatchPlayer player);

    public abstract void countdown();

    public abstract void start();

    public abstract void draw();

    public abstract void end(boolean force);

    public abstract void setCountdown(int countdown);

    @NotNull
    public abstract CompletableFuture<@Nullable Void> regenerate();

    public abstract boolean isReady();

    public abstract boolean isFull();

    public abstract ScoreboardType getCurrentScoreboard();

    public abstract PlayerMatchStats.Builder getStats(@NotNull Player player);

    public PlayerMatchStats.Builder getStats(@NotNull MatchPlayer player) {
        return getStats(player.player());
    }

    public Map<MatchPlayer, ArenaTeam> getTeams() {
        throw new UnsupportedOperationException();
    }

    protected abstract void handleDoubleJumps(@NotNull MatchPlayer player);

    public abstract void addDoubleJumpItems(@NotNull MatchPlayer player, boolean newState);

    public abstract Collection<MatchPlayer> checkForWinners();

    public abstract Set<MatchPlayer> getPlayers();

    public abstract Set<MatchPlayer> getSpectators();

    public abstract Map<Integer, PowerupLifecycle> getActivePowerUps();

    public abstract AbilityHandler getAbilities();

    public abstract void clearStats();

    public abstract int getCountdown();

    public abstract int getTimeLeft();

    public abstract long started();

    protected void onPlayerJoin(MatchPlayer player) {
    }

    protected void onPlayerLeave(MatchPlayer player, boolean disconnect) {
    }

    protected void onPlayerEliminate(MatchPlayer player) {
    }

    protected abstract void onPlayerWin(MatchPlayer winner);

    protected abstract void onContextLoad(MatchPlayer player);

    protected abstract void onGameStart(@NotNull MatchPlayer player);

    protected abstract void onGamePlayerLoop(MatchPlayer player);

    protected abstract void onGameLoop();

    protected abstract void onCountdownChange(int newCountdown);

    protected abstract void onPreEnd(boolean force);

    protected abstract void onPostEnd(boolean force);

    protected abstract void onGracePeriodStart(MatchPlayer player, boolean real);

    protected abstract void onGracePeriodEnd(MatchPlayer player, boolean real);

    public abstract void setExtension(@NotNull MatchExtension extension);

    @AllArgsConstructor @Getter
    public static final class JoinResult {

        private static final JoinResult ALLOW = new JoinResult(true, "");

        private final boolean allowed;
        private final String message;

        public static JoinResult allow() {
            return ALLOW;
        }

        public static JoinResult deny(String message) {
            return new JoinResult(false, message);
        }

        public static JoinResult deny(Message message, Object... args) {
            return new JoinResult(false, message.create(args));
        }

        public void handle(CommandSender player) {
            if (!message.isEmpty()) {
                player.sendMessage(message);
            }
        }

        public void handle(MatchPlayer player) {
            handle(player.player());
        }

    }

    public abstract boolean isGracePeriodActive();

    protected abstract void onGameStart();

}
