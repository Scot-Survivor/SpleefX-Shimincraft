package net.spleefx.powerup;

import com.cryptomorin.xseries.XMaterial;
import net.spleefx.SpleefX;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.player.PlayerState;
import net.spleefx.model.Position;
import net.spleefx.powerup.api.Powerup;
import net.spleefx.util.game.BukkitEvents;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BlockTrailPowerup extends Powerup {

    private XMaterial blockTrail;
    private int duration;

    @Override public void onActivate(@NotNull MatchPlayer player, @NotNull ReloadedArenaEngine engine, @NotNull Position position, @NotNull SpleefX plugin) throws Throwable {
        Material material = blockTrail.parseMaterial();
        if (material == null) {
            plugin.getLogger().warning("No material: " + blockTrail + ". Block trail won't work.");
            return;
        }
        if (!material.isBlock()) {
            plugin.getLogger().warning("Material " + material + " is not a block. Block trail won't work.");
            return;
        }
        BukkitEvents.timedEvent(duration, TimeUnit.SECONDS, PlayerMoveEvent.class, e -> e.getPlayer().getUniqueId().equals(player.uuid()), EventPriority.NORMAL, false)
                .thenAccept(event -> {
                    if (player.getArena() == engine.getArena() && player.getState() == PlayerState.PLAYING)
                        event.getPlayer().getLocation().subtract(0, 0.5, 0).getBlock().setType(material);
                });
    }
}
