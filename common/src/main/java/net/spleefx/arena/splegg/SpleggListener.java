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
package net.spleefx.arena.splegg;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.type.splegg.SpleggArena;
import net.spleefx.arena.type.splegg.extension.SpleggUpgrade;
import net.spleefx.backend.DelayContext;
import net.spleefx.backend.Schedulers;
import net.spleefx.event.PlayerArenaInteractionListener;
import net.spleefx.event.listen.EventListener;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent.BreakContext;
import net.spleefx.extension.MatchExtension;
import net.spleefx.listeners.interact.Projection;
import net.spleefx.model.ExplosionSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TNT;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.spleefx.extension.StandardExtensions.SPLEGG;

@RegisteredListener
public class SpleggListener implements PlayerArenaInteractionListener, Listener {

    private static final List<Block> EXPLODED = new ArrayList<>();

    @Override
    public void handle(@NotNull PlayerInteractEvent event, @NotNull MatchArena arena, @NotNull MatchExtension extension, @NotNull MatchPlayer player, @NotNull ItemStack item, @Nullable Block block, @NotNull Action action, @NotNull SpleefX plugin) {
        if (!(arena instanceof SpleggArena)) return;
        if (SPLEGG.isUpgradeSystemEnabled()) {
            if (Schedulers.DELAY.hasDelay(player, DelayContext.SPLEGG_SHOT)) return;
            SpleggUpgrade upgrade = player.getProfile().getSelectedSpleggUpgrade();
            if (upgrade == null) return;
            if (upgrade.getGameItem().isSimilar(item)) {
                Schedulers.DELAY.delay(player, DelayContext.SPLEGG_SHOT, (long) (upgrade.getDelay() * 1000), TimeUnit.MILLISECONDS);
                launchProjectile(player, arena);
                arena.getEngine().getStats(player).spleggShot();
            }
        } else {
            if (SPLEGG.getProjectileItem().isSimilar(item)) {
                launchProjectile(player, arena);
                arena.getEngine().getStats(player).spleggShot();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (EXPLODED.remove(event.getBlock())) {
            event.setYield(0);
        }
    }

    private void launchProjectile(MatchPlayer player, MatchArena arena) {
        Projection.make(player.player(), SPLEGG.getProjectileType().getType())
                .onLaunch(p -> p.setVelocity(p.getVelocity().add(new Vector(0.1, 0.1, 0.1))))
                .onHitEntity((p, e) -> e.setCancelled(true))
                .onLand((p, e) -> {
                    Block hitBlock = e.asBlock();
                    if (arena.canDestroy(hitBlock.getType())) {
                        Location loc = hitBlock.getLocation();
                        if (arena.getEngine().getStage() == ArenaStage.ACTIVE) {
                            ExplosionSettings explosionSettings = SPLEGG.getExplodeTNTWhenHit();
                            if (hitBlock.getType() == Material.TNT && explosionSettings != null && explosionSettings.isEnabled()) {
                                hitBlock.setType(Material.AIR);
                                EXPLODED.add(hitBlock);
                                hitBlock.getWorld().createExplosion(loc, explosionSettings.getPower(), explosionSettings.createFire());
                            } else {
                                hitBlock.setType(Material.AIR);
                            }
                        } else
                            p.remove();
                        EventListener.post(new PlayerDestroyBlockInArenaEvent(player.player(), arena, hitBlock, BreakContext.SHOT_SPLEGG));
                    }
                });
    }
}