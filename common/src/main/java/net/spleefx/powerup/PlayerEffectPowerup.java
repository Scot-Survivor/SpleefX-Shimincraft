package net.spleefx.powerup;

import net.spleefx.SpleefX;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.model.Position;
import net.spleefx.model.Potion;
import net.spleefx.powerup.api.Powerup;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PlayerEffectPowerup extends Powerup {

    private Set<Potion> potions;

    @Override
    public void onActivate(@NotNull MatchPlayer player, @NotNull ReloadedArenaEngine engine, @NotNull Position position, @NotNull SpleefX plugin) {
        potions.forEach(p -> p.give(player));
    }
}
