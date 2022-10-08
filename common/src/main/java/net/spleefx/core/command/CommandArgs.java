package net.spleefx.core.command;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.SneakyThrows;
import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.team.MatchTeam;
import net.spleefx.config.TeamsConfig;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.core.data.PlayerRepository;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import net.spleefx.model.Position;
import net.spleefx.util.Placeholders.CommandEntry;
import net.spleefx.util.message.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.IntPredicate;

/**
 * A simple wrapper to lists of strings to allow better access to the arguments list
 */
public class CommandArgs extends ForwardingList<String> {

    private static final CommandArgs EMPTY = new CommandArgs(Collections.emptyList());

    /**
     * Command does not have any validation criteria
     */
    private static final IntPredicate NONE = v -> true;

    /**
     * The backened arguments list
     */
    private final List<String> args;
    @Getter public Command command;

    public CommandArgs(List<String> args) {
        this.args = ImmutableList.copyOf(args);
    }

    public CommandArgs(String[] args) {
        this(Arrays.asList(args));
    }

    public String combine(int start) {
        return combine(start, args.size());
    }

    public String combine(int start, int end) {
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = start; (i < end + 1) && i < args.size(); i++) {
            joiner.add(args.get(i));
        }
        return joiner.toString();
    }

    @NotNull
    public Player player(int index) throws CommandException {
        String name = get(index);
        Player player = Bukkit.getPlayer(name);
        if (player == null)
            throw new CommandException(Message.UNKNOWN_PLAYER.create().replace("{player}", name));
        return player;
    }

    public <E extends Enum<E>> E getEnum(int index, Class<E> type, String err) throws CommandException {
        String provided = get(index);
        try {
            return Enum.valueOf(type, provided);
        } catch (Exception e) {
            throw new CommandException("&cInvalid %s: &e%s", err, provided);
        }
    }

    public <E extends Enum<E>> E getEnum(int index, Class<E> type, E def) {
        try {
            return Enum.valueOf(type, get(index));
        } catch (Exception e) {
            return def;
        }
    }

    @SneakyThrows
    @Override
    public String get(int index) {
        try {
            return super.get(index);
        } catch (IndexOutOfBoundsException e) {
//            throw new CommandException("&cInvalid usage &e(no parameter present in index " + index + ") &7(remember: we start counting from 0!)");
            index += 1;
            throw new CommandException("&cIncorrect arguments count. In fact, I was expecting &oat least &e" + index + " &cargument" + (index == 1 ? "" : "s") + "&r&c.");
        }
    }

    public boolean containsAny(String value) {
        for (String a : args) {
            if (a.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    public boolean flag(String flag) {
        return containsAny("-" + flag);
    }

    @NotNull
    public OfflinePlayer offlinePlayer(int index) throws CommandException {
        OfflinePlayer player = Bukkit.getOfflinePlayer(get(index));
        if (!player.hasPlayedBefore()) {
            throw new CommandException(Message.UNKNOWN_PLAYER, new CommandEntry(null, null, player.getName()));
        }
        return player;
    }

    @NotNull
    public PlayerProfile profile(int index) throws CommandException {
        return profile(Bukkit.getOfflinePlayer(get(index)));
    }

    @NotNull
    public PlayerProfile profile(OfflinePlayer player) throws CommandException {
        PlayerProfile profile = PlayerRepository.REPOSITORY.lookup(player);
        if (profile == null)
            throw new CommandException(Message.UNKNOWN_PLAYER, new CommandEntry(null, null, player.getName()));
        return profile;
    }

    @NotNull
    public <A extends MatchArena> A arena(int index) throws CommandException {
        String name = get(index);
        A arena = (A) MatchArena.getByKey(name); // Get by key
        if (arena == null) {
            name = combine(index);
            arena = (A) Arenas.getArenas().values().stream().filter(i -> i.getDisplayName().equals(combine(index))).findFirst().orElse(null); // Get by display name
        }
        if (arena == null)
            throw new CommandException(Message.INVALID_ARENA.create(new CommandEntry(null, name)));
        return arena;
    }

    public MatchTeam team(int index) throws CommandException {
        String name = get(index);
        MatchTeam team = TeamsConfig.TEAMS.get().values().stream().filter(t -> t.getKey().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (team == null)
            throw new CommandException("&cInvalid team: &e" + name + "&c.");
        return team;
    }

    @NotNull
    public MatchExtension ext(int index) throws CommandException {
        String name = get(index);
        MatchExtension extension = Extensions.getByKey(name);
        if (extension == null)
            throw new CommandException("&cInvalid game extension: %s", name);
        return extension;
    }

    public int integer(int index) throws CommandException {
        String num = get(index);
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            throw new CommandException("&cInvalid number: &e" + num + "&c.");
        }
    }

    public int integerMax(int index, int max) throws CommandException {
        int i = integer(index);
        if (i > max)
            throw new CommandException("&cInvalid value: &e%s&c. Must be maximum &b" + max + "&c.", i);
        return i;
    }

    public int integerMin(int index, int min) throws CommandException {
        int i = integer(index);
        if (i < min)
            throw new CommandException("&cInvalid value: &e%s&c. Must be at least &b" + min + "&c.", i);
        return i;
    }

    public int integer(int index, int min, int max) throws CommandException {
        int i = integer(index);
        if (i > max || i < min)
            throw new CommandException("&cInvalid value: &e%s&c. Must be between &b%s &cand &b%s&c.", i, min, max);
        return i;
    }

    public Position centerize(Location loc) {
        return Position.at(loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch(), loc.getWorld());
    }

    @Override protected List<String> delegate() {
        return args;
    }

    public static CommandArgs empty() {
        return new CommandArgs(Collections.emptyList());
    }

}