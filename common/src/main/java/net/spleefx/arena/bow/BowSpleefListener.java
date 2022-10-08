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
package net.spleefx.arena.bow;

import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.type.bowspleef.BowSpleefArena;
import net.spleefx.event.listen.EventListener;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent.BreakContext;
import net.spleefx.extension.StandardExtensions;
import net.spleefx.listeners.interact.Projection;
import net.spleefx.util.Metadata;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@RegisteredListener
public class BowSpleefListener implements Listener {

    public static final Metadata<BowSpleefArena> ARROW = Metadata.of("spleefx.bowspleef.projectile");

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player) event.getEntity().getShooter();
        MatchPlayer mp = MatchPlayer.wrap(player);
        if (!(mp.getArena() instanceof BowSpleefArena)) return;
        BowSpleefArena arena = mp.getArena();

        Projection.track(event.getEntity())
                .onLaunch(p -> {
                    arena.getEngine().getStats(mp).bowSpleefShot();
                    p.setBounce(StandardExtensions.BOW_SPLEEF.isBounceArrows());
                })
                .onHitEntity((p, e) -> e.setCancelled(true))
                .onLand(((arrow, position) -> {
                    Block hitBlock = position.asBlock();
                    if (hitBlock.getType() == Material.TNT && StandardExtensions.BOW_SPLEEF.isRemoveTNTWhenPrimed()) {
                        if (arena.getEngine().getStage() == ArenaStage.ACTIVE) {
                            hitBlock.setType(Material.AIR);
                            EventListener.post(new PlayerDestroyBlockInArenaEvent(player, arena, hitBlock, BreakContext.SHOT_BOW_SPLEEF));
                        } else arrow.remove();
                    }
                }));
    }
//
//    @EventHandler
//    public void onEntityCombustByEntity(EntityCombustByEntityEvent event) {
//        if (event.getCombuster().getType() == EntityType.ARROW && ARROW.has(event.getCombuster()))
//            event.setCancelled(true);
//    }
}