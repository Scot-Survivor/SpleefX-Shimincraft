/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.spleefx.arena.spleef;

import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.event.listen.EventListener;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent.BreakContext;
import net.spleefx.extension.MatchExtension;
import net.spleefx.listeners.interact.Projection;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@RegisteredListener
public class SpleefListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.SNOWBALL) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player p = (Player) event.getEntity().getShooter();
        MatchPlayer player = MatchPlayer.wrap(p);
        if (player.getArena() != null) {
            if (!player.getArena().getExtension().getSnowballSettings().allowThrowing)
                event.setCancelled(true);
            else {
                MatchExtension extension = player.getArena().getExtension();
                Projection.track(event.getEntity())
                        .onHitEntity((pr, e) -> e.setCancelled(true))
                        .onLand((projectile, position) -> {
                            try {
                                Block hitBlock = position.asBlock();

                                if (extension.getSnowballSettings().thrownSnowballsRemoveHitBlocks.contains(hitBlock.getType())) {
                                    hitBlock.setType(Material.AIR);
                                    if (player.getArena() != null)
                                        EventListener.post(new PlayerDestroyBlockInArenaEvent(player.player(), player.getArena(), hitBlock, BreakContext.MINED));
                                }
                            } catch (Throwable ignored) {}
                        });
            }
        }
    }
}