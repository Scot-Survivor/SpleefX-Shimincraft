package net.spleefx.arena;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.core.command.CommandArgs;
import net.spleefx.model.Item;
import net.spleefx.model.Position;
import net.spleefx.util.CuboidArea;
import net.spleefx.util.Metadata;
import net.spleefx.util.game.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;

public class SpawnPointScanner {

    private static final Metadata<ScanSession> ARENA = Metadata.of("spleefx.spawnpoint.scanner");

    private static final ItemStack WAND = Item.builder().type(XMaterial.BLAZE_ROD)
            .name("&eSpawn-Point Wand")
            .lore("", "&eLeft click &7to set first position", "&aRight click &7to set second position.")
            .enchant(XEnchantment.LURE.getEnchant(), 1)
            .itemFlag(ItemFlag.HIDE_ENCHANTS)
            .build().createItem();

    public static void newState(Player player, MatchArena arena, CommandArgs args) {
        ScanSession scanner = ARENA.get(player);
        if (scanner == null) {
            ARENA.set(player, new ScanSession(arena));
            player.getInventory().addItem(new ItemStack(WAND));
            player.getInventory().addItem(new ItemStack(Material.BEACON));
            Chat.prefix(player, arena,
                    "&aEntered spawn-point scanner mode. Place beacons on spawn-point locations, " +
                            "and set the 2 corners in which beacons inside the region should be translated as spawn-points, " +
                            "&ausing the magical &eSpawn-Point Wand &ayou just received.");
            Chat.prefix(player, arena, "&bWhen you're done, run &e/" + args.getCommand().getName() + " arena spawnpoint " + arena.getKey() + " scan &bagain.");
        } else {
            if (scanner.first == null) {
                Chat.prefix(player, scanner.arena, "&cYou must set the &efirst location&c!");
                return;
            }
            if (scanner.second == null) {
                Chat.prefix(player, scanner.arena, "&cYou must set the &esecond location&c!");
                return;
            }
            CuboidArea cuboid = new CuboidArea(scanner.first, scanner.second);
            AtomicInteger integer = new AtomicInteger(0);
            Chat.prefix(player, scanner.arena, "&eScanning... Please wait.");
            cuboid.getBlocksAsync(Material.BEACON).thenAccept(c -> {
                if (c.isEmpty()) {
                    Chat.prefix(player, scanner.arena, "&cCould not find any beacons.");
                    return;
                }
                for (Block beacon : c) {
                    arena.getFfaSettings().registerSpawnpoint(integer.incrementAndGet(), args.centerize(beacon.getLocation()));
                    Bukkit.getScheduler().runTask(SpleefX.getPlugin(), () -> beacon.setType(Material.AIR));
                }
                Chat.prefix(player, scanner.arena, "&aSuccessfully registered &e" + integer.get() + " &aspawn-points.");
                player.getInventory().remove(Material.BEACON);
                player.getInventory().removeItem(new ItemStack(WAND));
                ARENA.remove(player);
            });
        }
    }

    private static class ScanSession {

        private final MatchArena arena;
        private Position first, second;

        public ScanSession(MatchArena arena) {
            this.arena = arena;
        }
    }

    @RegisteredListener
    public static class WandListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onPlayerInteract(PlayerInteractEvent event) {
            Block block = event.getClickedBlock();
            ItemStack item = event.getItem();
            if (block == null || item == null) return;
            if (!item.isSimilar(WAND)) return;
            event.setCancelled(true);
            ScanSession scanner = ARENA.get(event.getPlayer());
            if (scanner == null) return;
            if (event.getAction().name().contains("LEFT_")) {
                scanner.first = Position.at(block.getLocation());
                Chat.prefix(event.getPlayer(), scanner.arena, "&aFirst &7position has been set");
            } else if (event.getAction().name().contains("RIGHT_")) {
                scanner.second = Position.at(block.getLocation());
                Chat.prefix(event.getPlayer(), scanner.arena, "&eSecond &7position has been set");
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerDropItem(PlayerDropItemEvent event) {
            if (event.getItemDrop().getItemStack().isSimilar(WAND)) {
                event.setCancelled(true);
                Chat.sendUnprefixed(event.getPlayer(), "&cYou thought you could get rid of me easily? >:)");
            }
        }
    }
}