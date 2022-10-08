package net.spleefx.arena.summary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.spleefx.SpleefX;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.summary.template.RemoteSummaryTemplate;
import net.spleefx.arena.summary.template.RemoteSummaryTemplate.RemoteGameTemplate;
import net.spleefx.extension.MatchExtension;
import net.spleefx.json.SpleefXGson;
import net.spleefx.util.Placeholders;
import net.spleefx.util.Placeholders.GameSummaryEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A thread-safe, immutable data object that contains a summary of the game
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class GameSummary {

    private static final String MODE_NAME = "modeName";
    private static final String HEADER = "header";
    private static final String ROWS = "rows";
    private static final String BACKGROUND = "bg";
    private static final String SERVER = "svr";
    private static final String IP = "ip";
    private static final String VICTORY = "victory";
    private static final String ELIMINATED = "eliminated";
    private static final String CARRIED = "carried";

    private final Map<UUID, Long> survivalTimes;
    private final Map<UUID, PlayerMatchStats> playerStats;
    private final List<UUID> winners;
    private final MatchArena arena;
    private final long length;

    public static Builder start(MatchArena arena) {
        return new Builder(arena).started();
    }

    public static class Builder {

        private Map<UUID, Long> survivalTimes = new HashMap<>();
        private Map<UUID, PlayerMatchStats> playerStats = new HashMap<>();
        private List<UUID> winners = new ArrayList<>();

        private long length;

        Builder(MatchArena arena) {
            this.arena = arena;
        }

        private final MatchArena arena;

        public Builder survive(@NotNull MatchPlayer player) {
            survivalTimes.putIfAbsent(player.uuid(), System.currentTimeMillis() - arena.getEngine().started());
            return this;
        }

        public Builder stat(@NotNull MatchPlayer player, @NotNull PlayerMatchStats stats) {
            playerStats.putIfAbsent(player.uuid(), stats);
            return this;
        }

        public Builder stat(@NotNull Player player, @NotNull PlayerMatchStats stats) {
//            Thread.dumpStack();
            playerStats.putIfAbsent(player.getUniqueId(), stats);
            return this;
        }

        public Builder win(@NotNull MatchPlayer player) {
            winners.add(player.uuid());
            return this;
        }

        public Builder started() {
            length = System.currentTimeMillis();
            return this;
        }

        public GameSummary build() {
            return new GameSummary(
                    survivalTimes,
                    ImmutableMap.copyOf(playerStats),
                    ImmutableList.copyOf(winners),
                    arena,
                    System.currentTimeMillis() - length
            );
        }

    }

    @Nullable
    public JsonObject asJson(@NotNull MatchExtension extension) throws IOException {
        RemoteSummaryTemplate remoteSummaryTemplate = SpleefX.getSpleefX().gameSummary.getRemoteSummary();
        RemoteGameTemplate template = remoteSummaryTemplate.getModes().get(extension);
        if (template == null) return null;

        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        out.beginObject();
        //
        out.name(MODE_NAME).value(ChatColor.stripColor(arena.getDisplayName()));
        out.name(HEADER).jsonValue(jsonify(template.getHeader()));
        out.name(ROWS);
        out.beginArray();
        for (Entry<UUID, PlayerMatchStats> entry : playerStats.entrySet()) {
            out.beginArray();
            UUID player = entry.getKey();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
            PlayerMatchStats stats = entry.getValue();
            GameSummaryEntry summaryEntry = new GameSummaryEntry(survivalTimes.getOrDefault(player, length), winners.contains(player));
            for (String format : template.getPlayerFormat()) {
                out.value(Placeholders.on(format, offlinePlayer, stats, summaryEntry));
            }
            out.endArray();
        }
        out.endArray();
        out.name(BACKGROUND).value(pickRandom(template.getBackgroundURLs(), ""));
        out.name(SERVER).value(remoteSummaryTemplate.getServerName());
        out.name(IP).value(remoteSummaryTemplate.getServerIP());
        out.name(VICTORY).value(remoteSummaryTemplate.getVictoryText());
        out.name(ELIMINATED).value(remoteSummaryTemplate.getEliminatedText());
        out.name(CARRIED).value(remoteSummaryTemplate.getCarriedText());
        //
        out.endObject();
        return SpleefXGson.getElement(sw.toString()).getAsJsonObject();
    }

    private static String jsonify(@NotNull Object src) {
        return SpleefXGson.MAIN.toJson(src);
    }

    private static <T> T pickRandom(@NotNull List<T> list, T def) {
        if (list.isEmpty()) return def;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

}
