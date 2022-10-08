package net.spleefx.arena.engine;

import net.spleefx.SpleefX;
import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.scoreboard.ScoreboardType;
import net.spleefx.model.GracePeriod;
import net.spleefx.util.Placeholders.ColoredNumberEntry;
import net.spleefx.util.message.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static net.spleefx.config.SpleefXConfig.ARENA_UPDATE_INTERVAL;
import static net.spleefx.config.SpleefXConfig.DISPLAY_COUNTDOWN_ON_EXP_BAR;

public class ArenaLoopTask extends SXRunnable {

    private final AbstractArenaEngine engine;
    private final int gameTime;
    private final TimerTask timerTask;

    public ArenaLoopTask(AbstractArenaEngine engine) {
        this.engine = engine;
        gameTime = engine.arena.getGameTime() * 60;
        engine.timeLeft.set(gameTime);
        startGracePeriod();
        timerTask = new TimerTask(engine);
        timerTask.runTaskTimer(getPlugin(), 20, 20);
        engine.currentScoreboard = ScoreboardType.GAME_ACTIVE;
        for (MatchPlayer player : engine.players)
            engine.renderSidebar(player);
    }

    private void startGracePeriod() {
        GracePeriod period = engine.extension.getGracePeriod();
        if (period.isEnabled()) {
            for (MatchPlayer player : engine.players) {
                getEngine().onGracePeriodStart(player, true);
            }
            SpleefX.nextTick(engine.extension.getGracePeriod().getTime() * 20, () -> {
                for (MatchPlayer player : engine.players) {
                    getEngine().onGracePeriodEnd(player, true);
                }
            });
        }
    }

    @Override
    public void run() {
        engine.setStage(ArenaStage.ACTIVE);
        int time = engine.timeLeft.get();
        Collection<MatchPlayer> winners = engine.checkForWinners();
        if (winners != null) {
            if (winners.isEmpty()) {
                engine.draw();
                complete();
                return;
            }
            for (MatchPlayer winner : winners)
                engine.playerWin(winner);
            engine.end(false);
            complete();
            return;
        }
        if (time == 0 || engine.players.size() == 0) {
            engine.draw();
            complete();
            return;
        }
        @Nullable String warning = SpleefXConfig.TIME_OUT_WARN.get().get(time);
        for (MatchPlayer player : engine) {
            if (DISPLAY_COUNTDOWN_ON_EXP_BAR.get())
                player.expLevel(time).exp((float) time / gameTime);
            if (warning != null)
                player.msg(Message.GAME_TIMEOUT, new ColoredNumberEntry(warning), time);
        }
        for (MatchPlayer player : engine.players) {
            engine.getEngine().onGamePlayerLoop(player);
            if (player.getLocation().getY() <= engine.arena.getDeathLevel()) {
                engine.playerEliminated(player, false);
            }
        }
        engine.getEngine().onGameLoop();
    }

    public ArenaLoopTask schedule() {
        runTaskTimer(getPlugin(), ARENA_UPDATE_INTERVAL.get(), ARENA_UPDATE_INTERVAL.get());
        return this;
    }

    @Override public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        timerTask.cancel();
    }

    private ReloadedArenaEngine getEngine() {
        return engine.arena.getEngine();
    }

}