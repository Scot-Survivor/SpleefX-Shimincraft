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
package net.spleefx.listeners;

import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.player.PlayerState;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.event.listen.EventListener;
import net.spleefx.event.listen.EventListenerAdapter;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent.BreakContext;
import net.spleefx.extension.MatchExtension;
import net.spleefx.model.Item;
import net.spleefx.model.ability.DoubleJumpItems;
import net.spleefx.util.message.message.Message;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.stream.Collectors;

@RegisteredListener
public class ArenaListener extends EventListenerAdapter implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        Player damaged = (Player) event.getEntity();
        MatchPlayer p = MatchPlayer.wrap(damaged);
        if (p.getState() == PlayerState.WAITING) {
            MatchArena arena = p.getArena();
            if (arena.getExtension().getCancelledDamageInWaiting().contains(event.getCause())) {
                event.setCancelled(true);
            } else {
                if (damaged.getHealth() - event.getDamage() < 1 && SpleefXConfig.KICK_PLAYERS_ON_DEATH.get()) {
                    arena.getEngine().playerLeave(p, false, false);
                }
            }
        } else if (p.getState() == PlayerState.PLAYING || p.getState() == PlayerState.SPECTATING) {
            MatchArena arena = p.getArena();
            if (arena.getExtension().getCancelledDamageInGame().contains(event.getCause())) {
                event.setCancelled(true);
            } else {
                if (damaged.getHealth() - event.getDamage() < 1 && SpleefXConfig.KICK_PLAYERS_ON_DEATH.get()) {
                    arena.getEngine().playerLeave(p, false, false);
                }
            }

        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        MatchPlayer p = MatchPlayer.wrap(event.getPlayer());
        if (p.getArena() != null) {
            MatchArena arena = p.getArena();
            if (arena.getExtension().isPreventItemDropping())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
        if (player.getArena() != null) {
            Set<String> allowed = player.getArena().getExtension().getAllowedCommands();
            if (allowed.stream().anyMatch(event.getMessage()::startsWith)) return;
            if (player.player().hasPermission("spleefx.arena.command-exempt")) return;
            event.setCancelled(true);
            Message.DISALLOWED_COMMAND.reply(player.player(), event.getMessage(), player.getArena().getExtension());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
        if (player.getState() == PlayerState.WAITING) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true) public void onInventoryClick(InventoryClickEvent event) {
        MatchPlayer player = MatchPlayer.wrap(event.getWhoClicked());
        if (player.getArena() == null) return;
        DoubleJumpItems items = player.getArena().getExtension().getDoubleJump().getDoubleJumpItems();
        if (items.getAvailable().isSimilar(event.getCurrentItem())
                || items.getUnavailable().isSimilar(event.getCurrentItem()))
            event.setCancelled(true);
    }

    private static void handleTeamDamage(MatchPlayer p, EntityDamageByEntityEvent event) {
        if (p.getArena().isFFA()) return;
        if (!(event.getDamager() instanceof Player)) return;
        Player damager = (Player) event.getDamager();
        MatchPlayer d = MatchPlayer.wrap(damager);
        if (Objects.equals(d.getArena(), p.getArena())) {
            ReloadedArenaEngine a = d.getArena().getEngine();
            if (Objects.equals(a.getTeams().get(d), a.getTeams().get(p)) && SpleefXConfig.ARENA_CANCEL_TEAM_DAMAGE.get())
                event.setCancelled(true);
        }
    }

    @SuppressWarnings("Convert2MethodRef")
    public static class WGListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST) // we need to override it to allow games to run
        public void onBreakBlock(BreakBlockEvent event) {
            if (event.getCause().getRootCause() instanceof Player) {
                Player player = event.getCause().getFirstPlayer();
                MatchPlayer p = MatchPlayer.wrap(Objects.requireNonNull(player, "player is null!"));
                for (Block block : event.getBlocks()) {
                    event.setAllowed(PluginCompatibility.getWG().canBreak(player, block));
                    if (p.getState() == PlayerState.PLAYING) {
                        ItemStack mainHand = PluginCompatibility.attempt(() -> player.getInventory().getItemInMainHand(), () -> player.getItemInHand());
                        if (event.getResult() != Result.DENY) {
                            MatchArena arena = p.getArena();
                            if (!arena.isDropMinedBlocks()) {
                                Collection<ItemStack> oldDrops = getDrops(arena.getExtension(), block, mainHand);
                                block.setType(Material.AIR);
                                if (arena.getExtension().isGiveDroppedItems())
                                    p.player().getInventory().addItem(oldDrops.toArray(new ItemStack[0]));
                            }
                            EventListener.post(new PlayerDestroyBlockInArenaEvent(player, arena, block, BreakContext.MINED));
                        }
                    }
                }
            }
        }
    }

    public static class BlockBreakListener implements Listener {

        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            MatchPlayer p = MatchPlayer.wrap(event.getPlayer());
            if (p.getState() == PlayerState.PLAYING) {
                MatchArena arena = p.getArena();
                if (!arena.isDropMinedBlocks()) {
                    ItemStack mainHand = p.getMainHand();
                    Collection<ItemStack> oldDrops = getDrops(arena.getExtension(), event.getBlock(), mainHand);
                    event.getBlock().setType(Material.AIR);
                    if (arena.getExtension().isGiveDroppedItems())
                        p.player().getInventory().addItem(oldDrops.toArray(new ItemStack[0]));
                }
                EventListener.post(new PlayerDestroyBlockInArenaEvent(event.getPlayer(), arena, event.getBlock(), BreakContext.MINED));
            }
        }
    }

    private static List<ItemStack> getDrops(MatchExtension extension, Block block, ItemStack tool) {
        List<Item> drops = extension.getCustomDrops().get(block.getType());
        if (drops == null) return new ArrayList<>(block.getDrops(tool));
        return drops.stream().map(Item::createItem).collect(Collectors.toList());
    }

    @Override
    public void onPlayerDestroyBlockInArena(PlayerDestroyBlockInArenaEvent event) {
        event.getEngine().getStats(event.getPlayer()).blockMined();
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory() instanceof PlayerInventory) return;
        MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
        if (player.getArena() == null) return;
        if (player.getArena().getExtension().isDenyOpeningContainers())
            if (event.getInventory().getHolder() != null) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        MatchPlayer player = MatchPlayer.wrap(event.getWhoClicked());
        if (player.getArena() == null) return;
        if (player.getArena().getExtension().isDenyCrafting())
            event.setCancelled(true);
    }
}