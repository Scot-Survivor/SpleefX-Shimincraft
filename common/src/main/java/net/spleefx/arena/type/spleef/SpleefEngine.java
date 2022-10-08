package net.spleefx.arena.type.spleef;

import com.cryptomorin.xseries.XMaterial;
import net.spleefx.SpleefX;
import net.spleefx.arena.engine.ForwardingArenaEngine;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.collect.EntityMap;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.extension.StandardExtensions;
import net.spleefx.util.Percentage;
import net.spleefx.util.game.InventoryUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static net.spleefx.extension.StandardExtensions.SPLEEF;

public class SpleefEngine extends ForwardingArenaEngine {

    private BukkitRunnable meltingTask;

    public SpleefEngine(ReloadedArenaEngine delegate) {
        super(delegate);
        setExtension(StandardExtensions.SPLEEF);
    }

    @Override public void onGameStart() {
        super.onGameStart();
        long interval = SpleefXConfig.ARENA_MELTING_INTERVAL.get().longValue();
        meltingTask = new MeltingTask(this);
        meltingTask.runTaskTimer(SpleefX.getPlugin(), interval, interval);
    }

    @Override public void onPostEnd(boolean force) {
        super.onPostEnd(force);
        if (meltingTask != null) meltingTask.cancel();
    }

    /**
     * A simple implementation for the melting
     */
    private static final Comparator<Location> LOCATION_COMPARATOR =
            comparingInt((Location location) -> SpleefXConfig.ARENA_MELTING_IGNORE_X.get() ? 0 : location.getBlockX())
                    .thenComparingInt(location -> SpleefXConfig.ARENA_MELTING_IGNORE_Y.get() ? 0 : location.getBlockY())
                    .thenComparingInt(location -> SpleefXConfig.ARENA_MELTING_IGNORE_Z.get() ? 0 : location.getBlockZ());

    static class MeltingTask extends BukkitRunnable {

        private final EntityMap<Player, Location> locations = EntityMap.hashMap();

        private final SpleefEngine engine;
        private final SpleefArena arena;

        private final List<Material> meltableBlocks;

        public MeltingTask(SpleefEngine engine) {
            this.engine = engine;
            meltableBlocks = SpleefXConfig.ARENA_MELTABLE_BLOCKS.get().stream().map(XMaterial::parseMaterial).collect(Collectors.toList());
            meltableBlocks.removeIf(Objects::isNull);
            arena = (SpleefArena) engine.getArena();
        }

        @Override
        public void run() {
            if (arena.isMelt() && SpleefXConfig.ARENA_MELTING_RADIUS.get() != 0) {
                for (MatchPlayer player : engine.getPlayers()) {
                    Location prev = locations.put(player.player(), player.getLocation());
                    if (prev == null) continue;
                    if (LOCATION_COMPARATOR.compare(prev, player.getLocation()) != 0)
                        continue; // Player is in a different location
                    Block b = pickBlock(getLowestBlock(player.getLocation()).getLocation(), SpleefXConfig.ARENA_MELTING_RADIUS.get());
                    if (b == null) continue; // No meltable block found
                    b.setType(Material.AIR);
                    if (SPLEEF.getSnowballSettings().removeSnowballsGraduallyOnMelting) {
                        Percentage p = SPLEEF.getSnowballSettings().removalChance;
                        if (p.isApplicable())
                            InventoryUtils.removeItem(player.player().getInventory(), new ItemStack(Objects.requireNonNull(XMaterial.SNOWBALL.parseMaterial()), SPLEEF.getSnowballSettings().removedAmount), SPLEEF.getSnowballSettings().removedAmount);
                    }
                }
            }
        }

        private static Block getLowestBlock(Location location) {
            Block lowestBlock = null;
            for (int y = location.getBlockY() - 1; y > 0; y--) {
                lowestBlock = Objects.requireNonNull(location.getWorld()).getBlockAt(location.add(0, -0.5, 0));
                if (lowestBlock.getType() != Material.AIR) {
                    return lowestBlock;
                }
            }
            return lowestBlock;
        }

        private Block pickBlock(Location location, int radius) {
            final int x = location.getBlockX();
            final int y = (int) Math.round(location.getY());
            final int z = location.getBlockZ();
            final int minX = x - radius;
            final int minZ = z - radius;
            final int maxX = x + radius;
            final int maxZ = z + radius;
            List<Block> blocks = new ArrayList<>();
            for (int counterX = minX; counterX <= maxX; counterX++) {
                for (int counterZ = minZ; counterZ <= maxZ; counterZ++) {
                    Block block = location.getWorld().getBlockAt(counterX, y, counterZ);
                    if (!meltableBlocks.contains(block.getType())) continue;
                    blocks.add(block);
                }
            }
            return blocks.size() == 0 ? null : blocks.get(ThreadLocalRandom.current().nextInt(blocks.size()));
        }

    }

}
