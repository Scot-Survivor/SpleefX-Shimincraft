package net.spleefx.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import lombok.AllArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.summary.PlayerMatchStats;
import net.spleefx.arena.team.ArenaTeam;
import net.spleefx.arena.team.MatchTeam;
import net.spleefx.arena.type.splegg.extension.SpleggUpgrade;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.data.GameStatType;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.extension.MatchExtension;
import net.spleefx.extension.ability.GameAbility;
import net.spleefx.model.Position;
import net.spleefx.util.game.Chat;
import net.spleefx.util.message.message.Message;
import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static net.spleefx.util.Placeholders.PlaceholderFiller.p;

@SuppressWarnings({"rawtypes", "CodeBlock2Expr", "unused", "RedundantSuppression"})
public class Placeholders {

    private static final boolean PAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    /**
     * Placeholder filler for offline players
     */
    private static final PlaceholderFiller<OfflinePlayer> OFFLINE_PLAYER = (player, b) -> {
        p(b, "player", player.getName() == null ? "NoName" : player.getName());
        p(b, "player_name", player.getName() == null ? "NoName" : player.getName());
        if (PAPI) {
            String created = PlaceholderAPI.setPlaceholders(player, b.toString());
            b.clear().append(created);
        }
    };

    /**
     * Placeholder filler for players
     */
    private static final PlaceholderFiller<Player> PLAYER = (player, b) -> {
        p(b, "player_displayname", player.getDisplayName());
        p(b, "player_health", (int) player.getHealth());
        if (PAPI) {
            String built = b.toString();
            String created = PlaceholderAPI.setPlaceholders(player, built);
            b.clear().append(created);
        }
        OFFLINE_PLAYER.apply(player, b);
    };

    /**
     * Placeholder filler for teams
     */
    private static final PlaceholderFiller<MatchTeam> MATCH_TEAM = (team, b) -> {
        p(b, "team", team.getChatColor() + team.getDisplayName());
        p(b, "team_color", team.getChatColor());
    };

    /**
     * Placeholder filler for teams
     */
    private static final PlaceholderFiller<ArenaTeam> ARENA_TEAM = (team, b) -> MATCH_TEAM.apply(team.team, b);

    /**
     * Placeholder filler for locations
     */
    private static final PlaceholderFiller<Location> LOCATION = (loc, b) -> {
        p(b, "x", loc.getX());
        p(b, "y", loc.getY());
        p(b, "z", loc.getZ());
        p(b, "world", Objects.requireNonNull(loc.getWorld(), "location#getWorld() is null!").getName());
    };

    private static final PlaceholderFiller<PEntry> PENTRY = (pentry, b) -> {
        for (Entry<String, String> entry : pentry.placeholders.entrySet()) {
            p(b, entry.getKey(), entry.getValue());
        }
    };

    private static final PlaceholderFiller<Position> POSITION = (pos, b) -> {
        p(b, "x", pos.x);
        p(b, "y", pos.y);
        p(b, "z", pos.z);
        p(b, "world", pos.world.getName());
    };

    /**
     * Placeholder filler for command parameters
     */
    private static final PlaceholderFiller<String[]> COMMAND_ARGS = (args, b) -> {
        for (int i = 0; i < args.length; i++) {
            p(b, "args-" + (i + 1), args[i]);
        }
    };

    private static final PlaceholderFiller<CommandEntry> COMMAND_ENTRY = (e, b) -> {
        if (e.command != null)
            p(b, "command", e.command);
        if (e.arena != null)
            p(b, "arena", e.arena);
        if (e.player != null)
            p(b, "player", e.player);
    };

    /**
     * Placeholder filler for game extensions
     */
    private static final PlaceholderFiller<MatchExtension> EXTENSION = (extension, b) -> {
        p(b, "extension", extension.getDisplayName());
        p(b, "extension_key", extension.getKey());
        p(b, "extension_chat_prefix", extension.getChatPrefix());
        p(b, "extension_displayname", extension.getDisplayName());
        p(b, "extension_name", extension.getDisplayName());
        p(b, "extension_without_colors", ChatColor.stripColor(Chat.colorize(extension.getDisplayName())));
        p(b, "extension_key", extension.getKey());
    };

    /**
     * Placeholder filler for arenas
     */
    private static final PlaceholderFiller<MatchArena> ARENA = (arena, b) -> {
        p(b, "arena", arena.getDisplayName());
        p(b, "arena_key", arena.getKey());
        p(b, "arena_displayname", Chat.colorize(arena.getDisplayName()));
        p(b, "arena_playercount", arena.getEngine().getPlayers().size());
        p(b, "countdown", formatTime(arena.getEngine().getCountdown()));
        int countdown = arena.getEngine().getCountdown();
        p(b, "countdown_chat", SpleefXConfig.TITLE_ON_COUNTDOWN_NUMBERS.get().getOrDefault(countdown, countdown + ""));
        p(b, "countdown_value", countdown);
        p(b, "arena_time_left", formatTime(arena.getEngine().getTimeLeft()));
        p(b, "arena_minimum", arena.getMinimum());
        p(b, "arena_maximum", arena.getMaximum());
        p(b, "arena_players_per_team", arena.getMembersPerTeam());
        p(b, "arena_stage", arena.getEngine().getStage().getState());
        p(b, "arena_alive", arena.getEngine().getPlayers().size());
        EXTENSION.apply(arena.getExtension(), b);
    };

    private static final PlaceholderFiller<SpleggUpgrade> SPLEGG_UPGRADE = (upgrade, b) -> {
        p(b, "upgrade_key", upgrade.getKey());
        p(b, "upgrade_displayname", upgrade.getDisplayName());
        p(b, "upgrade_price", formatNumber(upgrade.getPrice()));
        p(b, "upgrade_delay", Double.toString(upgrade.getDelay()));
    };

    private static final PlaceholderFiller<SpleggPurchaseEntry> SPLEGG_PURCHASE_ENTRY = (p, b) -> {
        PlayerProfile stats = p.profile;
        SpleggUpgrade upgrade = p.upgrade;
        SPLEGG_UPGRADE.apply(upgrade, b);
        p(b, "upgrade_purchased",
                (stats.upgradeKeys().contains(upgrade.getKey()) ?
                        stats.isSpleggUpgradeSelected(upgrade) ? Message.SPLEGG_GUI_SELECTED : Message.SPLEGG_GUI_CLICK_TO_SELECT
                        : (stats.getCoins() >= upgrade.getPrice() ? Message.SPLEGG_GUI_CLICK_TO_PURCHASE : Message.SPLEGG_GUI_NOT_ENOUGH_COINS)).getValue());
    };

    private static final PlaceholderFiller<StatsEntry> STATS = (p, b) -> {
        for (GameStatType type : GameStatType.values) {
            p(b, type.name().toLowerCase(), formatNumber(p.stats.getOrDefault(type, 0)));
        }
    };

    private static final PlaceholderFiller<FancyTimeEntry> TIME_SURVIVED = (time, b) -> {
        p(b, time.placeholder, time.timePhrase);
    };

    private static final PlaceholderFiller<GameSummaryEntry> SHORT_TIME_SURVIVED = (time, b) -> {
        p(b, "time_survived", time.timeSurvived);
        p(b, "victory_or_loss", time.won ? "$WIN" : "$LOSS");
    };

    private static final PlaceholderFiller<AbilitiesEntry> ABILITIES = (a, b) -> {
        for (Entry<GameAbility, Integer> entry : a.abilities.entrySet()) {
            for (String placeholder : entry.getKey().placeholders) {
                p(b, placeholder, formatNumber(entry.getValue()));
            }
        }
    };

    private static final PlaceholderFiller<Integer> INTEGER = (value, b) -> {
        p(b, "value", value);
        p(b, "value_formatted", formatNumber(value));
        p(b, "plural", value != 1 ? "s" : "");
    };

    private static final PlaceholderFiller<ToggleEntry> TOGGLE = (toggle, b) -> {
        p(b, "enabled", toggle.enabled ? "&cClick to disable" : "&aClick to enable");
        p(b, "activated", toggle.enabled ? "&a\u2713 Enabled" : "&c\u2717 Disabled");
        p(b, "yesno", toggle.enabled ? "&aYes" : "&cNo");
    };

    private static final PlaceholderFiller<ColoredNumberEntry> COLORED_NUMBER = (number, b) -> {
        p(b, "colored_number", number.value);
    };

    private static final PlaceholderFiller<PlayerProfile> PLAYER_PROFILE = (profile, b) -> {
        Map<GameStatType, Integer> stats = profile.getGameStats();
        for (GameStatType type : GameStatType.values) {
            p(b, type.name().toLowerCase(), formatNumber(stats.getOrDefault(type, 0)));
        }
    };

    private static final PlaceholderFiller<MatchPlayer> MATCH_PLAYER = (player, b) -> {
        PLAYER.apply(player.player(), b);
        OFFLINE_PLAYER.apply(player.player(), b);
        if (player.getArena() != null)
            ARENA.apply(player.getArena(), b);
    };

    private static final PlaceholderFiller<PlayerMatchStats> STATS_TRACKER = (profile, b) -> {
        Map<GameStatType, Integer> stats = profile.stats;
        for (GameStatType type : GameStatType.values) {
            p(b, type.name().toLowerCase(), formatNumber(stats.getOrDefault(type, 0)));
        }
    };

    private static final PlaceholderFiller<InvalidUsageEntry> INVALID_USAGE = (usage, b) -> {
        p(b, "command", usage.command);
        p(b, "command_usage", usage.usage);
    };

    private static final PlaceholderFiller<PortionEntry> PORTION = (portion, b) -> {
        p(b, "portion", formatNumber(portion.portion));
    };

    @FunctionalInterface
    public interface PlaceholderFiller<T> {

        void apply(T value, StrBuilder builder);

        static void p(StrBuilder builder, String placeholder, Object value) {
            builder.replaceAll("{" + placeholder + "}", String.valueOf(value));
        }
    }

    @NotNull
    @SafeVarargs
    public static <T> T firstNotNull(T... values) {
        for (T v : values) {
            if (v != null) return v;
        }
        throw new NullPointerException("All inputted values are null!");
    }

    public static String on(String original, Object... formats) {
        StrBuilder builder = new StrBuilder(original);
        for (Entry<Class<?>, PlaceholderFiller> filler : fillers.entrySet())
            for (Object o : formats) {
                if (o != null)
                    if (filler.getKey().isAssignableFrom(o.getClass()))
                        filler.getValue().apply(filler.getKey().cast(o), builder);
            }
        if (PAPI) {
            OfflinePlayer player = null;
            for (Object t : formats) {
                if (t == null) continue;
                if (t instanceof OfflinePlayer || t instanceof MatchPlayer) {
                    player = t instanceof MatchPlayer ? ((MatchPlayer) t).player() : ((OfflinePlayer) t);
                    break;
                }
            }
            String created = PlaceholderAPI.setPlaceholders(player, builder.toString());
            builder.clear().append(created);
        }
        return Chat.colorize(builder.toString());
    }


    public static String formatTimeMillis(int milliseconds) {
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        int secondsLeft = seconds % 3600 % 60;
        int minutes = (int) Math.floor((float) seconds % 3600 / 60);
        int hours = (int) Math.floor((float) seconds / 3600);

        String hoursFormat = ((hours < 10) ? "0" : "") + hours;
        String minutesFormat = ((minutes < 10) ? "0" : "") + minutes;
        String secondsFormat = ((secondsLeft < 10) ? "0" : "") + secondsLeft;
        if (hours <= 0)
            return minutesFormat + ":" + secondsFormat;
        return hoursFormat + ":" + minutesFormat + ":" + secondsFormat;
    }

    public static String formatTime(int seconds) {
        int secondsLeft = seconds % 3600 % 60;
        int minutes = (int) Math.floor((float) seconds % 3600 / 60);
        int hours = (int) Math.floor((float) seconds / 3600);

        String hoursFormat = ((hours < 10) ? "0" : "") + hours;
        String minutesFormat = ((minutes < 10) ? "0" : "") + minutes;
        String secondsFormat = ((secondsLeft < 10) ? "0" : "") + secondsLeft;
        if (hours <= 0)
            return minutesFormat + ":" + secondsFormat;
        return hoursFormat + ":" + minutesFormat + ":" + secondsFormat;
    }


    public static String formatNumber(int number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumber(Number number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatTimeFancy(long time) {
        Duration d = Duration.ofMillis(time);
        long hours = d.toHours();
        long minutes = d.minusHours(hours).getSeconds() / 60;
        long seconds = d.minusMinutes(minutes).minusHours(hours).getSeconds();
        List<String> words = new ArrayList<>();
        if (hours != 0)
            words.add(hours + plural(hours, " hour"));
        if (minutes != 0)
            words.add(minutes + plural(minutes, " minute"));
        if (seconds != 0)
            words.add(seconds + plural(seconds, " second"));
        return toFancyString(words);
    }

    public static <T> String toFancyString(List<T> list) {
        StringJoiner builder = new StringJoiner(", ");
        if (list.isEmpty()) return "";
        if (list.size() == 1) return list.get(0).toString();
        for (int i = 0; i < list.size(); i++) {
            T el = list.get(i);
            if (i + 1 == list.size())
                return builder + " and " + el.toString();
            else
                builder.add(el.toString());
        }
        return builder.toString();
    }

    public static String plural(Number count, String thing) {
        if (count.intValue() == 1) return thing;
        if (thing.endsWith("y"))
            return thing.substring(0, thing.length() - 1) + "ies";
        return thing + "s";
    }

    public static PEntry make(Object... args) {
        return PEntry.of(args);
    }

    private static final ImmutableMap<Class<?>, PlaceholderFiller> fillers;

    static {
        Builder<Class<?>, PlaceholderFiller> builder = new Builder<>();
        for (Field field : Placeholders.class.getDeclaredFields()) {
            if (!PlaceholderFiller.class.isAssignableFrom(field.getType())) continue;
            Class<?> type = ((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
            try {
                builder.put(type, (PlaceholderFiller) field.get(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        fillers = builder.build();
    }

    public static class CommandEntry {

        @Nullable private final String command;
        @Nullable private String arena;
        @Nullable private String player;

        public CommandEntry(@Nullable String command) {
            this.command = command;
        }

        public CommandEntry(@Nullable String command, @Nullable String arena) {
            this.command = command;
            this.arena = arena;
        }

        public CommandEntry(@Nullable String command, @Nullable String arena, @Nullable String player) {
            this.command = command;
            this.arena = arena;
            this.player = player;
        }
    }

    @AllArgsConstructor
    public static class ColoredNumberEntry {

        private final String value;
    }

    @AllArgsConstructor
    public static class InvalidUsageEntry {

        private final String command;
        private final String usage;
    }

    @AllArgsConstructor
    public static class PortionEntry {

        private final int portion;
    }

    @AllArgsConstructor
    public static class SpleggPurchaseEntry {

        private final PlayerProfile profile;
        private final SpleggUpgrade upgrade;
    }

    public static class FancyTimeEntry {

        private final String placeholder;
        private final String timePhrase;

        public FancyTimeEntry(String placeholder, long time) {
            this.placeholder = placeholder;
            timePhrase = formatTimeFancy(time);
        }
    }

    public static class GameSummaryEntry {

        private final String timeSurvived;
        private final boolean won;

        public GameSummaryEntry(long time, boolean won) {
            this.timeSurvived = formatTimeMillis((int) time);
            this.won = won;
        }
    }

    @AllArgsConstructor
    public static class StatsEntry {

        private final Map<GameStatType, Integer> stats;
    }

    @AllArgsConstructor
    public static class AbilitiesEntry {

        private final Map<GameAbility, Integer> abilities;
    }

    @AllArgsConstructor
    public static class ToggleEntry {

        private final boolean enabled;
    }

    @AllArgsConstructor
    private static class PEntry {

        private final Map<String, String> placeholders;

        public static PEntry of(Object... replacements) {
            Map<String, String> map = new LinkedHashMap<>();
            if (replacements != null && replacements.length != 0 && replacements.length % 2 == 0) {
                for (int i = 0; i < replacements.length; i += 2) {
                    String key = (String) replacements[i];
                    String value = String.valueOf(replacements[i + 1]);
                    if (value == null) value = "";
                    map.put(key, value);
                }
            }
            return new PEntry(map);
        }
    }

}
