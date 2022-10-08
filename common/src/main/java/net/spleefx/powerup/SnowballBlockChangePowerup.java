package net.spleefx.powerup;

import com.cryptomorin.xseries.XMaterial;
import net.spleefx.SpleefX;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.listeners.interact.Projection;
import net.spleefx.model.Item;
import net.spleefx.model.Position;
import net.spleefx.powerup.api.Powerup;
import net.spleefx.util.CuboidArea;
import net.spleefx.util.game.BukkitEvents;
import org.bukkit.Material;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class SnowballBlockChangePowerup extends Powerup {

    private Item snowballItem;
    private int radius;
    private List<XMaterial> replaced;
    private XMaterial replaceWith;

    @Override public void onActivate(@NotNull MatchPlayer player, @NotNull ReloadedArenaEngine engine, @NotNull Position position, @NotNull SpleefX plugin) throws Throwable {
        player.giveItems(snowballItem.createItem());
        Material[] materials = replaced.stream().map(XMaterial::parseMaterial).filter(Objects::nonNull)
                .toArray(Material[]::new);
        BukkitEvents.nextEvents(snowballItem.getCount(), PlayerInteractEvent.class, event -> {
            if (event.getPlayer() != player.player()) return false;
            return snowballItem.isSimilar(event.getItem());
        }, EventPriority.NORMAL, false).thenAccept(event -> {
            event.setUseItemInHand(Result.ALLOW);
            event.setCancelled(true);
            Projection.make(event.getPlayer(), Snowball.class)
                    .onHitEntity((projectile, e) -> e.setCancelled(true))
                    .onLand((projectile, pos) -> {
                        Position min = pos.subtract(radius, radius, radius);
                        Position max = pos.add(radius, radius, radius);
                        CuboidArea area = new CuboidArea(min, max);
                        area.getBlocksAsync(materials)
                                .thenAccept(b -> plugin.sync(() -> b.forEach(bl -> bl.setType(replaceWith.parseMaterial()))));
                    });
        });
    }
}
