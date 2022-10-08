package net.spleefx.extension;

import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import net.spleefx.SpleefX;
import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.ArenaType;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.team.ArenaTeam;
import net.spleefx.collect.NeverNullList;
import net.spleefx.util.Placeholders;
import net.spleefx.util.Placeholders.AbilitiesEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Getter
public class MatchScoreboard {

    private boolean enabled = true;
    private String title = "";

    @JsonAdapter(MapToList.class)
    private List<String> text = Collections.emptyList();

    public void createScoreboard(MatchPlayer p) {
        if (!enabled) return;
        try {
            p.renderSidebar();
        } catch (IllegalArgumentException e) {
            List<String> possibleCauses = Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(pl -> pl.getName().toLowerCase().contains("board"))
                    .map(Plugin::getName).collect(Collectors.toList());
            SpleefX.getSpleefX().warn(
                    "#####################",
                    "Failed to create a scoreboard for player " + p.player().getName() + ". Perhaps that shouldn't happen.",
                    "Possible cause(s): " + possibleCauses + ".",
                    "#####################"
            );
        }
    }

    public static String replacePlaceholders(@Nullable MatchPlayer player, String message, MatchArena arena) {
        if (arena == null) return message;
        ReloadedArenaEngine engine = arena.getEngine();

        @Nullable Location location = null;
        if (player != null) location = player.player().getLocation();
        List<Object> formats = new NeverNullList<>(new ArrayList<>());
        if (player != null)
            formats.add(player.player());
        formats.add(arena);
        if (location != null)
            formats.add(location);

        if (arena.getType() == ArenaType.TEAMS && player != null) {
            ArenaTeam team = engine.getTeams().get(player);
            if (team != null)
                formats.add(team.team);
        }
        AbilitiesEntry abilitiesEntry = player == null ? null : engine.getStage() == ArenaStage.ACTIVE ? new AbilitiesEntry(engine.getAbilities().get(player.player())) : null;
        if (abilitiesEntry != null)
            formats.add(abilitiesEntry);
        return Placeholders.on(message, formats.toArray());
    }

    protected static class MapToList extends TypeAdapter<List<String>> {

        @Override
        public void write(JsonWriter out, List<String> strings) throws IOException {
            out.beginArray();
            for (String e : strings)
                out.value(e);
            out.endArray();
        }

        @Override public List<String> read(JsonReader in) {
            JsonElement element = Streams.parse(in);
            Builder<String> list = new Builder<>();
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                for (Entry<String, JsonElement> e : object.entrySet()) {
                    list.add(e.getValue().getAsString());
                }
            } else {
                JsonArray array = element.getAsJsonArray();
                for (JsonElement e : array)
                    list.add(e.getAsString());
            }
            return list.build();
        }
    }

}
