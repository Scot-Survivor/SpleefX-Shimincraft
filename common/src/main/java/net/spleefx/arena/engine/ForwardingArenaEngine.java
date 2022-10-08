package net.spleefx.arena.engine;

import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.summary.PlayerMatchStats.Builder;
import net.spleefx.arena.team.ArenaTeam;
import net.spleefx.core.scoreboard.ScoreboardType;
import net.spleefx.extension.MatchExtension;
import net.spleefx.powerup.api.PowerupLifecycle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.spleefx.spectate.Spectating.SpectatingCause;

/**
 * An arena engine that delegates its work to another engine.
 */
public class ForwardingArenaEngine extends ReloadedArenaEngine {

    private final ReloadedArenaEngine delegate;

    public ForwardingArenaEngine(ReloadedArenaEngine delegate) {
        this.delegate = delegate;
    }

    @Override public MatchArena getArena() {
        return delegate.getArena();
    }

    @Override public ArenaStage getStage() {
        return delegate.getStage();
    }

    @Override public void setStage(@NotNull ArenaStage newStage) {
        delegate.setStage(newStage);
    }

    @Override public JoinResult playerJoin(@NotNull MatchPlayer player, boolean force) {
        return delegate.playerJoin(player, false);
    }

    @Override public JoinResult playerJoin(@NotNull MatchPlayer player, boolean force, boolean doJoin) {
        return delegate.playerJoin(player, false, doJoin);
    }

    @Override public void playerLeave(@NotNull MatchPlayer player, boolean disconnect, boolean urgent) {
        delegate.playerLeave(player, disconnect, urgent);
    }

    @Override public void playerSpectate(@NotNull MatchPlayer player, @NotNull SpectatingCause cause) {
        delegate.playerSpectate(player, cause);
    }

    @Override public void playerEliminated(@NotNull MatchPlayer player, boolean disconnect) {
        delegate.playerEliminated(player, disconnect);
    }

    @Override public void playerWin(@NotNull MatchPlayer player) {
        delegate.playerWin(player);
    }

    @Override public void playerLeaveSpectating(@NotNull MatchPlayer player) {
        delegate.playerLeaveSpectating(player);
    }

    @Override public void countdown() {
        delegate.countdown();
    }

    @Override public void start() {
        delegate.start();
    }

    @Override public void draw() {
        delegate.draw();
    }

    @Override public void end(boolean force) {
        delegate.end(force);
    }

    @Override public void setCountdown(int countdown) {
        delegate.setCountdown(countdown);
    }

    @Override @NotNull public CompletableFuture<@Nullable Void> regenerate() {
        return delegate.regenerate();
    }

    @Override public boolean isReady() {
        return delegate.isReady();
    }

    @Override public boolean isFull() {
        return delegate.isFull();
    }

    @Override public ScoreboardType getCurrentScoreboard() {
        return delegate.getCurrentScoreboard();
    }

    @Override public Builder getStats(@NotNull Player player) {
        return delegate.getStats(player);
    }

    @Override public Builder getStats(@NotNull MatchPlayer player) {
        return delegate.getStats(player);
    }

    @Override public Map<MatchPlayer, ArenaTeam> getTeams() {
        return delegate.getTeams();
    }

    @Override public void addDoubleJumpItems(@NotNull MatchPlayer player, boolean newState) {
        delegate.addDoubleJumpItems(player, newState);
    }

    @Override public Collection<MatchPlayer> checkForWinners() {
        return delegate.checkForWinners();
    }

    @Override public Set<MatchPlayer> getPlayers() {
        return delegate.getPlayers();
    }

    @Override public Set<MatchPlayer> getSpectators() {
        return delegate.getSpectators();
    }

    @Override public AbilityHandler getAbilities() {
        return delegate.getAbilities();
    }

    @Override public void clearStats() {
        delegate.clearStats();
    }

    @Override public int getCountdown() {
        return delegate.getCountdown();
    }

    @Override public int getTimeLeft() {
        return delegate.getTimeLeft();
    }

    @Override public long started() {
        return delegate.started();
    }

    @Override @NotNull public String getPrefix() {
        return delegate.getPrefix();
    }

    @Override protected void gameStart(@NotNull MatchPlayer player) {
        delegate.gameStart(player);
    }

    @Override protected PreGamePlayerData storeData(@NotNull MatchPlayer player) {
        return delegate.storeData(player);
    }

    @Override protected CompletableFuture<Void> restoreData(@NotNull MatchPlayer player, boolean urgent) {
        return delegate.restoreData(player, urgent);
    }

    @Override public void renderSidebar(@NotNull MatchPlayer player) {
        delegate.renderSidebar(player);
    }

    @Override public void handleDoubleJumps(@NotNull MatchPlayer player) {
        delegate.handleDoubleJumps(player);
    }

    @Override protected void onPlayerJoin(MatchPlayer player) {
        delegate.onPlayerJoin(player);
    }

    @Override public void onPlayerLeave(MatchPlayer player, boolean disconnect) {
        delegate.onPlayerLeave(player, disconnect);
    }

    @Override public void onPlayerEliminate(MatchPlayer player) {
        delegate.onPlayerEliminate(player);
    }

    @Override public void onPlayerWin(MatchPlayer winner) {
        delegate.onPlayerWin(winner);
    }

    @Override public void onContextLoad(MatchPlayer player) {
        delegate.onContextLoad(player);
    }

    @Override protected void onGameStart() {
        delegate.onGameStart();
    }

    @Override public Map<Integer, PowerupLifecycle> getActivePowerUps() {
        return delegate.getActivePowerUps();
    }

    @Override public void onGameStart(@NotNull MatchPlayer player) {
        delegate.onGameStart(player);
    }

    @Override public void onGamePlayerLoop(MatchPlayer player) {
        delegate.onGamePlayerLoop(player);
    }

    @Override public void onGameLoop() {
        delegate.onGameLoop();
    }

    @Override public void onCountdownChange(int newCountdown) {
        delegate.onCountdownChange(newCountdown);
    }

    @Override public void onPreEnd(boolean force) {
        delegate.onPreEnd(force);
    }

    @Override public void onPostEnd(boolean force) {
        delegate.onPostEnd(force);
    }

    @Override public void onGracePeriodStart(MatchPlayer player, boolean real) {
        delegate.onGracePeriodStart(player, real);
    }

    @Override public void onGracePeriodEnd(MatchPlayer player, boolean real) {
        delegate.onGracePeriodEnd(player, real);
    }

    @Override public boolean isGracePeriodActive() {
        return delegate.isGracePeriodActive();
    }

    @Override public void setExtension(@NotNull MatchExtension extension) {
        delegate.setExtension(extension);
    }

    @NotNull @Override public Iterator<MatchPlayer> iterator() {
        return delegate.iterator();
    }
}