package net.spleefx.arena.engine;

import com.google.gson.annotations.SerializedName;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.json.GsonHook;
import net.spleefx.json.GsonHook.AfterDeserialization;
import net.spleefx.model.Position;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@GsonHook
public class SpawnPointHelper {

    @SerializedName(value = "SpawnPoints", alternate = {"Spawnpoints", "spawnpoints"})
    private final Map<Integer, Position> spawnPoints = new HashMap<>();
    private final Map<Integer, Position> lobbies = new HashMap<>();

    private transient Map<MatchPlayer, Integer> players = new HashMap<>();

    public void registerSpawnpoint(int index, Position location) {
        spawnPoints.put(index, location);
    }

    public void registerLobby(int index, Position location) {
        lobbies.put(index, location);
    }

    public Position getSpawnPoint(MatchArena arena, MatchPlayer player) {
        Integer empty = getEmptySlot(spawnPoints, arena, player);
        if (empty == null)
            throw new IllegalStateException("Cannot find an available spawn-point. Did the arena state change?");
        return spawnPoints.get(empty);
    }

    @Nullable
    public Position getLobby(MatchArena arena, MatchPlayer player) {
        Integer empty = getEmptySlot(lobbies, arena, player);
        if (empty == null) return null;
        return lobbies.get(empty);
    }

    public Integer getEmptySlot(Map<Integer, Position> map, MatchArena arena, MatchPlayer player) {
        Integer empty = players.get(player);
        if (empty != null)
            return empty;
        empty = map.keySet().stream().filter(index -> index <= arena.getMaxPlayerCount() && !players.containsValue(index)).findFirst().orElse(-1);
        if (empty == -1) return null; // The arena is full
        players.put(player, empty);
        return empty;
    }

    public void remove(MatchPlayer player) {
        players.remove(player);
    }

    public void removeLobby(int index) {
        lobbies.remove(index);
    }

    public Map<Integer, Position> getSpawnPoints() {
        return spawnPoints;
    }

    @AfterDeserialization
    private void setPlayers() {
        players = new HashMap<>();
    }

}
