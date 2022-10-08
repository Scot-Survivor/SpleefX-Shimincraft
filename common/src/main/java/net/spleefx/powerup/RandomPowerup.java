package net.spleefx.powerup;

import net.spleefx.SpleefX;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.model.Position;
import net.spleefx.powerup.api.Powerup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RandomPowerup extends Powerup {

    @Override public void onActivate(@NotNull MatchPlayer player, @NotNull ReloadedArenaEngine engine, @NotNull Position position, @NotNull SpleefX plugin) throws Throwable {
        List<Powerup> powerups = new ArrayList<>(engine.getArena().getPowerups());
        powerups.removeIf(p -> p instanceof RandomPowerup);
        if (powerups.isEmpty()) return;
        random(powerups).onActivate(player, engine, position, plugin);
    }
}
