package net.spleefx.powerup;

import net.spleefx.SpleefX;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.model.Position;
import net.spleefx.model.Potion;
import net.spleefx.powerup.api.Powerup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RandomPlayerEffectPowerup extends Powerup {

    private Set<Potion> potions;
    private boolean excludePicker;

    @Override public void onActivate(@NotNull MatchPlayer player,
                                     @NotNull ReloadedArenaEngine engine,
                                     @NotNull Position position,
                                     @NotNull SpleefX plugin) {
        List<MatchPlayer> players = new ArrayList<>(engine.getPlayers());
        if (excludePicker)
            players.remove(player);
        MatchPlayer target = random(players);
        potions.forEach(p -> p.give(target));
    }
}
