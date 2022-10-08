package net.spleefx.powerup;

import net.spleefx.SpleefX;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.model.Position;
import net.spleefx.model.Potion;
import net.spleefx.powerup.api.Powerup;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class NearbyPotionEffectPowerup extends Powerup {

    private Set<Potion> potions;
    private double radius;

    @Override public void onActivate(@NotNull MatchPlayer player, @NotNull ReloadedArenaEngine engine, @NotNull Position position, @NotNull SpleefX plugin) throws Throwable {
        List<Entity> entityList = player.player().getNearbyEntities(radius, radius, radius);
        entityList.stream()
                .filter(e -> e instanceof Player)
                .map(MatchPlayer::wrap)
                .filter(c -> c.getArena() == engine.getArena())
                .forEach(target -> potions.forEach(p -> p.give(target)));
    }
}
