package net.spleefx.powerup;

import net.spleefx.SpleefX;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.model.Position;
import net.spleefx.powerup.api.Powerup;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public class FlashBangPowerup extends Powerup {

    @Override
    public void onActivate(@NotNull MatchPlayer player,
                           @NotNull ReloadedArenaEngine engine,
                           @NotNull Position position,
                           @NotNull SpleefX plugin) {
        Location loc = position.asLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 3);
    }
}
