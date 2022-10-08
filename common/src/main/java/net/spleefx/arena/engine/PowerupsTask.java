package net.spleefx.arena.engine;

import net.spleefx.arena.MatchArena;
import net.spleefx.powerup.api.Powerup;
import net.spleefx.powerup.api.PowerupLifecycle;
import net.spleefx.util.Util;
import org.bukkit.Location;

import static net.spleefx.config.SpleefXConfig.SPAWN_POWERUPS_EVERY;

public class PowerupsTask extends SXRunnable {

    private final AbstractArenaEngine engine;

    public PowerupsTask(AbstractArenaEngine engine) {
        this.engine = engine;
    }

    @Override public void run() {
        MatchArena arena = engine.getArena();
        if (arena.getPowerupsCenter() == null) return;
        if (arena.getPowerups().isEmpty()) return;
        Location location = arena.getPowerupsCenter().random(arena.getPowerupsRadius());
        Powerup powerup = Util.random(arena.getPowerups());
        powerup.spawn(arena, location.add(0, 1, 0));
    }

    public PowerupsTask schedule() {
        runTaskTimer(getPlugin(), 20L * SPAWN_POWERUPS_EVERY.get(), 20L * SPAWN_POWERUPS_EVERY.get());
        return this;
    }

    @Override public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        engine.getActivePowerUps().values().forEach(PowerupLifecycle::destroy);
        engine.getActivePowerUps().clear();
    }
}
