package net.spleefx.core.command.tab;

import lombok.AllArgsConstructor;
import net.spleefx.core.command.BaseCommand;
import net.spleefx.core.command.CommandHandler;
import net.spleefx.core.command.PromptSender;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@AllArgsConstructor
public class BukkitTabCompleter implements TabCompleter {

    private final CommandHandler commandHandler;

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command bukkitCommand, @NotNull String alias, @NotNull String[] args) {
        MatchExtension extension = Extensions.getByCommand(bukkitCommand.getName());
        Map<String, BaseCommand> commandMap = commandHandler.getCommands(extension);
        if (args.length == 0) return emptyList();
        if (args.length == 1) {
            @NotNull String[] finalArgs = args;
            return commandMap.values().stream().distinct()
                    .filter(c -> {
                        if (c.meta.permission != null)
                            return c.meta.permission.test(commandSender, extension);
                        return true;
                    })
                    .map(c -> c.meta.name).filter(c -> c.startsWith(finalArgs[0]))
                    .collect(Collectors.toList());
        }
        BaseCommand command = commandHandler.getCommands(Extensions.getByCommand(bukkitCommand.getName())).get(args[0]);
        if (command == null) return emptyList();
        args = (String[]) ArrayUtils.subarray(args, 1, args.length);
        return computeTabs(extension, command, commandSender, bukkitCommand, args);
    }

    private List<String> computeTabs(MatchExtension extension, BaseCommand command, CommandSender commandSender, Command bukkitCommand, String[] args) {
        if (command.meta.permission == null || command.meta.permission.test(commandSender, extension)) {
            RootNode node = command.onTab(extension, bukkitCommand, PromptSender.adapt(commandSender), args);
            if (node == null) return emptyList();
            if (!node.isBuilt()) node.end();
            List<String> completions = new ArrayList<>();

            for (TabNode<?> c : node.getByLevel(args.length)) {
                if (c.parent instanceof RootNode || c.parent.match(args[c.level - 2], args, c.level - 2))
                    completions.addAll(c.supply(args[args.length - 1]));
            }

            return completions.stream().filter(c -> c.startsWith(args[args.length - 1])).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}