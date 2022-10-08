package net.spleefx.arena.engine;

public class TimerTask extends SXRunnable {

    private final AbstractArenaEngine engine;
    private final int gameTime;

    public TimerTask(AbstractArenaEngine engine) {
        this.engine = engine;
        gameTime = engine.arena.getGameTime() * 60;
    }

    @Override
    public void run() {
        engine.timeLeft.decrementAndGet();
    }
}
