package net.spleefx.core.command.tab;

import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.team.MatchTeam;
import net.spleefx.arena.type.splegg.SpleggArena;
import net.spleefx.config.TeamsConfig;
import net.spleefx.core.command.BaseCommand;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import net.spleefx.powerup.api.Powerups;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A utility class for supplying shortcut methods to tab completions
 */
public final class TabCompletion {

    private TabCompletion() {
    }

    public static RootNode start() {
        return new RootNode();
    }

    public static RootNode of(TabNode<?> node) {
        return start().with(node);
    }

    public static TabString literal(String value) {
        return new TabString(value);
    }

    public static TabArguments empty() {
        return new TabArguments();
    }

    public static TabStringList list(String... values) {
        return list(Arrays.asList(values));
    }

    public static TabStringList list(List<String> values) {
        return new TabStringList(values);
    }

    public static TabArguments range(int begin, int end) {
        return arguments(IntStream.rangeClosed(begin, end).mapToObj(Integer::toString).collect(Collectors.toList()));
    }

    public static TabArguments arguments(Function<String, List<String>> completions) {
        return new TabArguments(completions);
    }

    public static TabArguments arguments(List<String> completions) {
        return arguments(c -> completions);
    }

    public static TabArguments arguments(Stream<String> completions) {
        return arguments(completions.collect(Collectors.toList()));
    }

    public static TabArguments indexesAndTeams(String[] args, int arenaIndex) {
        try {
            MatchArena arena = MatchArena.getByKey(args[arenaIndex]);
            if (arena == null) return empty();
            if (arena.isTeams())
                return arguments(arena.getTeams().stream().map(MatchTeam::getKey));
            List<String> list = IntStream.rangeClosed(1, arena.getMaxPlayerCount()).mapToObj(Integer::toString).collect(Collectors.toCollection(LinkedList::new));
            list.add("scan");
            return arguments(list);
        } catch (Exception e) {
            return empty();
        }
    }

    public static TabArguments listMaterials(String[] args, int arenaIndex) {
        try {
            SpleggArena arena = (SpleggArena) MatchArena.getByKey(args[arenaIndex]);
            if (arena == null) return empty();
            return arguments(arena.getMaterials().stream().map(c -> c.name().toLowerCase()).collect(Collectors.toList()));
        } catch (Exception e) {
            return empty();
        }
    }

    public static TabArguments playerList() {
        return arguments(c -> Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(c.toLowerCase())).collect(Collectors.toList()));
    }

    public static TabArguments powerups() {
        return arguments(Powerups.getKeys().stream());
    }

    public static TabArguments teams(String[] args, int arenaIndex) {
        try {
            MatchArena arena = MatchArena.getByKey(args[arenaIndex]);
            if (arena == null) return empty();
            return arguments(arena.getTeams().stream().map(MatchTeam::getKey));
        } catch (Exception e) {
            return empty();
        }
    }

    public static TabArguments notTeams(String[] args, int arenaIndex) {
        try {
            MatchArena arena = MatchArena.getByKey(args[arenaIndex]);
            if (arena == null) return empty();
            return arguments(TeamsConfig.TEAMS.get().values().stream().filter(t -> !arena.getTeams().contains(t)).map(MatchTeam::getKey));
        } catch (Exception e) {
            return empty();
        }
    }

    public static TabArguments arenas(MatchExtension extension) {
        return arguments(Arenas.getArenas().values().stream()
                .filter(ex -> ex.getExtension().getKey().equalsIgnoreCase(extension.getKey()))
                .map(MatchArena::getKey));
    }

    public static TabArguments commandList(Map<String, BaseCommand> commands) {
        return arguments(commands.values().stream().distinct().map(c -> c.meta.name)
                .collect(Collectors.toList()));
    }

    public static TabArguments listScheduledCommands(String[] args, int index) {
        try {
            MatchArena arena = MatchArena.getByKey(args[index]);
            return arguments(IntStream.rangeClosed(1, arena.getScheduledCommands().size())
                    .mapToObj(Integer::toString));
        } catch (Throwable t) {
            return empty();
        }
    }

    public static TabArguments extensions() {
        return arguments(Extensions.getExtensions().stream().map(MatchExtension::getKey).collect(Collectors.toList()));
    }

    public static TabArguments extensions(Predicate<MatchExtension> filter) {
        return arguments(Extensions.getExtensions().stream().filter(filter)
                .map(MatchExtension::getKey).collect(Collectors.toList()));
    }

    public static TabNode<?> pages(Map<String, BaseCommand> commands, String[] args, int commandIndex) {
        try {
            BaseCommand command = commands.get(args[commandIndex]);
            return range(1, command.meta.helpPaginator.getPageSize(command.meta.helpMenu));
        } catch (Exception e) {
            return empty();
        }
    }
}