package net.spleefx.model;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import io.papermc.lib.PaperLib;
import net.spleefx.json.SimpleAdapter;
import net.spleefx.model.Position.Adapter;
import net.spleefx.util.JsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Objects;

import static net.spleefx.compatibility.ProtocolNMS.RANDOM;

/**
 * A 3-dimensional, immutable position inside a {@link World}.
 */
@JsonAdapter(Adapter.class)
public class Position {

    public final double x, y, z;
    public final float yaw, pitch;
    public transient final World world;

    private Position(double x, double y, double z, float yaw, float pitch, @NotNull World world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = Objects.requireNonNull(world, "world");
    }

    private Position(double x, double y, double z, float yaw, float pitch, String worldName) {
        this(x, y, z, yaw, pitch, Objects.requireNonNull(Bukkit.getWorld(worldName), "Invalid world: " + worldName));
    }

    private Position(double x, double y, double z, String worldName) {
        this(x, y, z, 0, 0, worldName);
    }

    private Position(double x, double y, double z, @NotNull World world) {
        this(x, y, z, 0, 0, world);
    }

    @NotNull
    public Position withX(double x) {
        return at(x, y, z, yaw, pitch, world);
    }

    @NotNull
    public Position withY(double y) {
        return at(x, y, z, yaw, pitch, world);
    }

    @NotNull
    public Position withZ(double z) {
        return at(x, y, z, yaw, pitch, world);
    }

    public Position add(double dX, double dY, double dZ) {
        return at(x + dX, y + dY, z + dZ, yaw, pitch, world);
    }

    public Position subtract(double dX, double dY, double dZ) {
        return at(x - dX, y - dY, z - dZ, yaw, pitch, world);
    }

    @NotNull
    public Position withWorld(World world) {
        return at(x, y, z, yaw, pitch, world);
    }

    @NotNull
    public Position withWorld(String world) {
        return at(x, y, z, yaw, pitch, world);
    }

    @NotNull
    public Location asLocation() {
        return new Location(world, x, y, z, yaw, pitch);
    }

    @NotNull
    public Block asBlock() {
        return world.getBlockAt(block(x), block(y), block(z));
    }

    @NotNull
    public <S extends BlockState> S getState() {
        return (S) asBlock().getState();
    }

    @NotNull
    public Position centered() {
        return at(center(x), center(y), center(z), yaw, pitch, world);
    }

    @NotNull
    public Location centeredLoc() {
        return new Location(world, center(x), center(y), center(z), yaw, pitch);
    }

    public Location random(int radius) {
        Location location = asLocation();
        double a = RANDOM.nextDouble() * 2 * Math.PI;
        double dist = RANDOM.nextDouble() * radius;
        return location.add(dist * Math.sin(a), 0, dist * Math.cos(a));
    }

    @NotNull
    public Position block() {
        return at(block(x), block(y), block(z), yaw, pitch, world);
    }

    public void warp(Entity entity) {
        PaperLib.teleportAsync(entity, (centeredLoc()));
    }

    public static double center(double v) {
        return block(v) + 0.5;
    }

    public static int block(double v) {
        return (int) Math.floor(v);
    }

    public static Position at(@NotNull Location location) {
        return at(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), location.getWorld());
    }

    public static Position at(@NotNull Block block) {
        return at(block.getLocation());
    }

    public static Position at(@NotNull Entity entity) {
        return at(entity.getLocation());
    }

    public static Position at(double x, double y, double z) {
        return new Position(x, y, z, "worldName");
    }

    public static Position at(double x, double y, double z, World world) {
        return new Position(x, y, z, world);
    }

    public static Position at(double x, double y, double z, String world) {
        return new Position(x, y, z, world);
    }

    public static Position at(double x, double y, double z, float yaw, float pitch, World world) {
        return new Position(x, y, z, yaw, pitch, world);
    }

    public static Position at(double x, double y, double z, float yaw, float pitch, String world) {
        return new Position(x, y, z, yaw, pitch, world);
    }

    @Override public String toString() {
        return "Position{" + "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                ", world=" + world +
                '}';
    }

    public String toCompactString() {
        return "X: " + x +
                ", Y: " + y +
                ", Z: " + z;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return Double.compare(position.x, x) == 0 &&
                Double.compare(position.y, y) == 0 &&
                Double.compare(position.z, z) == 0 &&
                Float.compare(position.yaw, yaw) == 0 &&
                Float.compare(position.pitch, pitch) == 0 &&
                Objects.equals(world, position.world);
    }

    @Override public int hashCode() {
        return Objects.hash(x, y, z, yaw, pitch, world);
    }

    public static class Adapter extends SimpleAdapter<Position> {

        @Override public Position deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                String[] data = jsonElement.getAsString().split(":");
                if (data.length == 4) {
                    return Position.at(
                            Double.parseDouble(data[1]),
                            Double.parseDouble(data[2]),
                            Double.parseDouble(data[3]),
                            data[0]
                    );
                }
                return Position.at(
                        Double.parseDouble(data[1]),
                        Double.parseDouble(data[2]),
                        Double.parseDouble(data[3]),
                        Float.parseFloat(data[4]),
                        Float.parseFloat(data[5]),
                        data[0]
                );
            }
            JsonObject json = jsonElement.getAsJsonObject();
            return Position.at(
                    json.get("X").getAsDouble(),
                    json.get("Y").getAsDouble(),
                    json.get("Z").getAsDouble(),
                    json.get("Yaw").getAsFloat(),
                    json.get("Pitch").getAsFloat(),
                    json.get("World").getAsString()
            );
        }

        @Override public JsonElement serialize(Position position, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonBuilder()
                    .map("X", position.x)
                    .map("Y", position.y)
                    .map("Z", position.z)
                    .map("Yaw", position.yaw)
                    .map("Pitch", position.pitch)
                    .map("World", position.world.getName())
                    .build().getAsJsonObject();
        }
    }

}