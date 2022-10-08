package net.spleefx.util;

import net.spleefx.backend.Schedulers;
import net.spleefx.model.Position;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CuboidArea implements Cloneable, ConfigurationSerializable, Iterable<Block> {

    protected String worldName;
    protected final Vector minimumPoint, maximumPoint;
    private final List<Block> blocksCache;

    public CuboidArea(Position loc) {
        this(loc, loc);
    }

    public CuboidArea(Position loc1, Position loc2) {
        if (loc1 != null && loc2 != null) {
            if (loc1.world != null && loc2.world != null) {
                if (!loc1.world.getUID().equals(loc2.world.getUID()))
                    throw new IllegalStateException("The 2 locations of the cuboid must be in the same world!");
            } else {
                throw new NullPointerException("One/both of the worlds is/are null!");
            }
            worldName = loc1.world.getName();

            double xPos1 = Math.min(loc1.x, loc2.x);
            double yPos1 = Math.min(loc1.y, loc2.y);
            double zPos1 = Math.min(loc1.z, loc2.z);
            double xPos2 = Math.max(loc1.x, loc2.x);
            double yPos2 = Math.max(loc1.y, loc2.y);
            double zPos2 = Math.max(loc1.z, loc2.z);
            minimumPoint = new Vector(xPos1, yPos1, zPos1);
            maximumPoint = new Vector(xPos2, yPos2, zPos2);
            blocksCache = blocks();
        } else {
            throw new NullPointerException("One/both of the locations is/are null!");
        }
    }

    public CuboidArea(String worldName, double x1, double y1, double z1, double x2, double y2, double z2) {
        if (worldName == null || Bukkit.getServer().getWorld(worldName) == null)
            throw new NullPointerException("One/both of the worlds is/are null!");
        this.worldName = worldName;

        double xPos1 = Math.min(x1, x2);
        double xPos2 = Math.max(x1, x2);
        double yPos1 = Math.min(y1, y2);
        double yPos2 = Math.max(y1, y2);
        double zPos1 = Math.min(z1, z2);
        double zPos2 = Math.max(z1, z2);
        minimumPoint = new Vector(xPos1, yPos1, zPos1);
        maximumPoint = new Vector(xPos2, yPos2, zPos2);
        blocksCache = blocks();
    }

    public boolean containsLocation(Location location) {
        return location != null && location.getWorld().getName().equals(worldName) && location.toVector().isInAABB(minimumPoint, maximumPoint);
    }

    public boolean containsVector(Vector vector) {
        return vector != null && vector.isInAABB(minimumPoint, maximumPoint);
    }

    public List<Block> blocks() {
        List<Block> blockList = new ArrayList<>();
        World world = getWorld();
        if (world != null) {
            for (int x = minimumPoint.getBlockX(); x <= maximumPoint.getBlockX(); x++) {
                for (int y = minimumPoint.getBlockY(); y <= maximumPoint.getBlockY() && y <= world.getMaxHeight(); y++) {
                    for (int z = minimumPoint.getBlockZ(); z <= maximumPoint.getBlockZ(); z++) {
                        blockList.add(world.getBlockAt(x, y, z));
                    }
                }
            }
        }
        return blockList;
    }

    public List<Block> getBlocks() {
        return blocksCache;
    }

    public Location getLowerLocation() {
        return minimumPoint.toLocation(getWorld());
    }

    public double getLowerX() {
        return minimumPoint.getX();
    }

    public double getLowerY() {
        return minimumPoint.getY();
    }

    public double getLowerZ() {
        return minimumPoint.getZ();
    }

    public Location getUpperLocation() {
        return maximumPoint.toLocation(getWorld());
    }

    public double getUpperX() {
        return maximumPoint.getX();
    }

    public double getUpperY() {
        return maximumPoint.getY();
    }

    public double getUpperZ() {
        return maximumPoint.getZ();
    }

    public double getVolume() {
        return (getUpperX() - getLowerX() + 1) * (getUpperY() - getLowerY() + 1) * (getUpperZ() - getLowerZ() + 1);
    }

    public World getWorld() {
        World world = Bukkit.getServer().getWorld(worldName);
        if (world == null) throw new NullPointerException("World '" + worldName + "' is not loaded.");
        return world;
    }

    public void setWorld(World world) {
        if (world != null) worldName = world.getName();
        else throw new NullPointerException("The world cannot be null.");
    }

    @NotNull
    @Override
    public ListIterator<Block> iterator() {
        return blocks().listIterator();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serializedCuboid = new HashMap<>();
        serializedCuboid.put("worldName", worldName);
        serializedCuboid.put("x1", minimumPoint.getX());
        serializedCuboid.put("x2", maximumPoint.getX());
        serializedCuboid.put("y1", minimumPoint.getY());
        serializedCuboid.put("y2", maximumPoint.getY());
        serializedCuboid.put("z1", minimumPoint.getZ());
        serializedCuboid.put("z2", maximumPoint.getZ());
        return serializedCuboid;
    }

    public static CuboidArea deserialize(Map<String, Object> serializedCuboid) {
        try {
            String worldName = (String) serializedCuboid.get("worldName");

            double xPos1 = (Double) serializedCuboid.get("x1");
            double xPos2 = (Double) serializedCuboid.get("x2");
            double yPos1 = (Double) serializedCuboid.get("y1");
            double yPos2 = (Double) serializedCuboid.get("y2");
            double zPos1 = (Double) serializedCuboid.get("z1");
            double zPos2 = (Double) serializedCuboid.get("z2");

            return new CuboidArea(worldName, xPos1, yPos1, zPos1, xPos2, yPos2, zPos2);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Block> getBlocks(Material... materials) {
        return blocks().stream().filter(b -> ArrayUtils.contains(materials, b.getType()))
                .collect(Collectors.toList());
    }

    public CompletableFuture<List<Block>> getBlocksAsync(Material... material) {
        CompletableFuture<List<Block>> blocks = new CompletableFuture<>();
        Schedulers.POOL.submit(() -> blocks.complete(getBlocks(material)));
        return blocks;
    }

}