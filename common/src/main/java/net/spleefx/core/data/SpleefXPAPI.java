/*
 * * Copyright 2019 github.com/ReflxctionDev
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
package net.spleefx.core.data;

import lombok.AllArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import net.spleefx.util.Placeholders;
import net.spleefx.util.game.Chat;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * PlaceholderAPI expansion for SpleefX
 */
@AllArgsConstructor
public class SpleefXPAPI extends PlaceholderExpansion {

    private static final List<String> INTS = Arrays.asList("1234567890".split(""));

    /**
     * The number formatter
     */
    private static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.US);

    /**
     * The SpleefX plugin
     */
    private final JavaPlugin plugin;

    @Override public String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase();
    }

    @Override public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override public boolean persist() {
        return true;
    }

    @Override public boolean canRegister() {
        return true;
    }

    /**
     * called when a placeholder value is requested from this hook
     *
     * @param player     {@link OfflinePlayer} to request the placeholder value for, null if not needed for a
     *                   player
     * @param identifier String passed to the hook to determine what value to return
     * @return value for the requested player and params
     */
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        switch (identifier.toLowerCase()) {
            case "total_arenas_playercount":
            case "total_arenas_alive": {
                return format(Arenas.getArenas().values().stream()
                        .map(MatchArena::getEngine)
                        .map(ReloadedArenaEngine::getPlayers)
                        .mapToInt(Collection::size).sum());
            }
        }
        if (identifier.startsWith("arena_")) { // arena_test_arena_key
            String[] parts = identifier.split("_", 3);
            String key = parts[1];
            MatchArena arena = MatchArena.getByKey(key);
            return Placeholders.on("{" + parts[parts.length - 1] + "}", arena);
        }
        if (INTS.stream().anyMatch(identifier::contains)) {
            String[] requested = identifier.split(":");
            String[] split = requested[0].split("_");
            int pos = Integer.parseInt(split[split.length - 1]);
            MatchExtension extension = Extensions.getByKey(requested[1]);
            String request = requested[2];
            GameStatType stat = GameStatType.fromName(requested[0].substring(0, requested[0].lastIndexOf("_")));
            LeaderboardTopper topper;
            List<LeaderboardTopper> toppers = PlayerRepository.REPOSITORY.getTopPlayers(stat, extension);
            try {
                topper = toppers.get(pos - 1);
            } catch (IndexOutOfBoundsException e) {
                try {
                    topper = toppers.get(toppers.size() - 1);
                } catch (Throwable t) {
                    return "None";
                }
            }
            CompletableFuture<String> playerFuture = topper.getPlayer();
            if (!playerFuture.isDone())
                return "Player not resolved yet";
            String lbPlayer = playerFuture.join();
            switch (request.toLowerCase()) {
                case "name":
                    return lbPlayer;
                case "pos":
                    return format(pos);
                case "stat":
                case "count":
                case "number":
                case "score":
                case "statistic":
                    return format(topper.getScore());
                case "format": {
                    String format = SpleefXConfig.LEADERBOARDS_FORMAT.get();
                    return Chat.colorize(format)
                            .replace("{player}", Objects.requireNonNull(lbPlayer, "Player name is null!"))
                            .replace("{pos}", format(pos))
                            .replace("{score}", format(topper.getScore()));
                }
                default:
                    return "Invalid request: " + request;
            }
        }
        if (player == null) return format(0);
        PlayerProfile stats = PlayerRepository.REPOSITORY.lookup(player);
        GameStatType statType = GameStatType.fromName(identifier);
        if (statType != null) {
            return format(stats.getGameStats().get(statType));
        } else {
            String[] split = identifier.split("_");
            String key = split[split.length - 1];
            MatchExtension mode = Extensions.getByKey(key);
            statType = GameStatType.fromName(identifier.substring(0, identifier.indexOf(split[split.length - 1]) - 1).toLowerCase());
            return statType == null ? format(0) : format(stats.getExtensionStatistics(mode).get(statType));
        }
    }

    private static String format(int number) {
        return FORMAT.format(number);
    }

}
