package net.spleefx.arena.engine;

import net.spleefx.SpleefX;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.core.scoreboard.ScoreboardType;
import net.spleefx.util.Placeholders.ColoredNumberEntry;
import net.spleefx.util.message.message.Message;

import java.util.concurrent.atomic.AtomicInteger;

import static net.spleefx.config.SpleefXConfig.*;
import static net.spleefx.core.scoreboard.ScoreboardType.COUNTDOWN_AND_FULL;
import static net.spleefx.core.scoreboard.ScoreboardType.COUNTDOWN_AND_WAITING;

public class CountdownTask extends SXRunnable {

    private final AbstractArenaEngine engine;

    public CountdownTask(AbstractArenaEngine engine) {
        this.engine = engine;
        SpleefX.nextTick(3, () -> {
            for (MatchPlayer player : engine.players) {
                player.msg(Message.GAME_STARTING, engine.countdown.get());
            }
        });
    }

    @Override
    public synchronized void run() {
        AtomicInteger countdown = engine.countdown;
        int cd = countdown.decrementAndGet();
        ScoreboardType oldType = engine.currentScoreboard;
        engine.currentScoreboard = engine.isFull() ? COUNTDOWN_AND_FULL : COUNTDOWN_AND_WAITING;
        String title = TITLE_ON_COUNTDOWN_NUMBERS.get().get(countdown.get());
        boolean sound = PLAY_SOUND_ON_EACH_BROADCAST_WHEN.get().contains(countdown.get());
        for (MatchPlayer player : engine.players) {
            if (oldType != engine.currentScoreboard)
                engine.renderSidebar(player);
            if (cd > 0 && DISPLAY_COUNTDOWN_ON_EXP_BAR.get()) {
                player.expLevel(countdown.get());
                player.exp((float) countdown.get() / COUNTDOWN_ON_ENOUGH_PLAYERS.get());
            }
            player.title(TITLE_ON_COUNTDOWN.get().withTitle(title));
            if (TITLE_ON_COUNTDOWN.get().isEnabled() && title != null)
                player.msg(Message.GAME_COUNTDOWN, engine.arena, new ColoredNumberEntry(title), engine.countdown.get());
            if (sound)
                player.sound(PLAY_SOUND_ON_EACH_BROADCAST_SOUND.get().parseSound());
        }
        engine.getEngine().onCountdownChange(cd);
        if (countdown.get() < 1) {
            complete();
            countdown.set(COUNTDOWN_ON_ENOUGH_PLAYERS.get());
        }
    }
}
