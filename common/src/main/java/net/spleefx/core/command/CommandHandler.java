package net.spleefx.core.command;

import net.spleefx.SpleefX;
import net.spleefx.core.command.internal.ForwardingCommandExecutor;
import net.spleefx.core.command.internal.PluginCommandBuilder;
import net.spleefx.core.command.tab.BukkitTabCompleter;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import net.spleefx.util.Placeholders.CommandEntry;
import net.spleefx.util.Placeholders.InvalidUsageEntry;
import net.spleefx.util.message.message.Message;
import net.spleefx.util.paginate.PageFooter;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler {

    private static final List<String> HELP = Collections.singletonList("help");

    private final SpleefX plugin;
    private final ForwardingCommandExecutor delegate;
    private final Map<String, BaseCommand> spleefxCommands = new HashMap<>();
    private final Map<String, Map<String, BaseCommand>> extensionCommands = new HashMap<>();

    public CommandHandler(SpleefX plugin) {
        this.plugin = plugin;
        delegate = new ForwardingCommandExecutor(plugin);
    }

    public void registerExtensionCommands() {
        BukkitTabCompleter completer = new BukkitTabCompleter(this);
        new PluginCommandBuilder("spleefx", SpleefX.getPlugin())
                .command(delegate)
                .tab(completer)
                .description("Main plugin command")
                .register();
        for (MatchExtension extension : Extensions.getExtensions()) {
            extension.getExtensionCommands().forEach(command -> new PluginCommandBuilder(command, SpleefX.getPlugin())
                    .command(delegate)
                    .tab(completer)
                    .description("Main command for " + extension.getKey())
                    .register());
        }
    }

    public CommandHandler add(BaseCommand command) {
        command.commandHandler = this;
        RegisterStrictlyFor strictly = command.getClass().getAnnotation(RegisterStrictlyFor.class);
        if (command.meta.extensionCommand || command.meta.registerBoth) {
            for (MatchExtension extension : Extensions.getExtensions()) {
                if (strictly != null && !ArrayUtils.contains(strictly.value(), extension.getKey())) continue;
                Map<String, BaseCommand> commandMap = getCommands(extension);
                commandMap.put(command.meta.name, command);
                for (String alias : command.meta.aliases)
                    commandMap.put(alias, command);
            }
            if (command.meta.registerBoth) {
                spleefxCommands.put(command.meta.name, command);
                for (String alias : command.meta.aliases)
                    spleefxCommands.put(alias, command);
            }
        } else {
            spleefxCommands.put(command.meta.name, command);
            for (String alias : command.meta.aliases)
                spleefxCommands.put(alias, command);
        }
        return this;
    }

    public void execute(Command bukkitCommand, CommandSender commandSender, String label, String[] cmdArgs) {
        @Nullable MatchExtension extension = label.equals("spleefx") ? null : Extensions.getByCommand(bukkitCommand.getName());
        Prefixable prefix = extension == null ? Prefixable.PLUGIN : extension;
        PromptSender sender = PromptSender.adapt(commandSender);

        List<String> args = Arrays.asList(cmdArgs);
        if (args.isEmpty()) {
            args = HELP;
        }
        BaseCommand command = getCommands(extension).get(args.get(0));
        try {
            if (command == null)
                throw new CommandException(Message.UNKNOWN_SUBCOMMAND.create(new CommandEntry(bukkitCommand.getName())));
            args = args.subList(1, args.size());
            CommandArgs commandArgs = args.isEmpty() ? CommandArgs.empty() : new CommandArgs(args);
            commandArgs.command = bukkitCommand;
            CommandMeta meta = command.meta;

            // PRE-HANDLE BEGIN
            if (!meta.permission.test(commandSender, extension))
                throw new CommandException(Message.NO_PERMISSION, extension);
            if (meta.requireNotInArena)
                if (sender.arenaPlayer().getArena() != null)
                    throw new CommandException(Message.ALREADY_IN_ARENA, extension);
            if (meta.requireArena)
                sender.arena();
            if (meta.requirePlayer)
                sender.player();
            if (!meta.argumentsValidation.test(commandArgs.size()))
                throw new CommandException(Message.INVALID_USAGE, prefix, new InvalidUsageEntry(bukkitCommand.getName() + " " + meta.name, meta.parameters));
            // PRE-HANDLE END

            Response response = command.execute(plugin, sender, commandArgs, extension);
            switch (response.type) {
                case SEND_USAGE:
                    throw new CommandException(Message.INVALID_USAGE, prefix, new InvalidUsageEntry(bukkitCommand.getName() + " " + meta.name, meta.parameters));
                case SEND_HELP:
                    List<String> helpM = meta.helpMenu.stream().map(c -> c.replace("/{cmd}", ("&a/" + bukkitCommand.getName() + "&d"))).collect(Collectors.toList());
                    meta.helpPaginator.sendPage(helpM, commandSender, 1, new PageFooter(bukkitCommand, meta.name, meta.helpJSON));
                    break;
                case OK:
                    if (response.message != null) {
                        sender.reply(response.prefixable != null ? response.prefixable : prefix, response.message);
                    }
                    break;
            }
        } catch (CommandException e) {
            e.send(extension, sender);
        } catch (Throwable e) {
            sender.reply(prefix, "&cAn error occured while handling this command. Necessary logs have been printed to console.");
            StackTraceElement errorPath = e.getStackTrace()[0];
            plugin.error(
                    "###########################################",
                    "An error occured while processing /" + bukkitCommand.getName() + (cmdArgs.length == 0 ? "" : " ") + String.join(" ", cmdArgs),
                    "Error class: " + e.getClass().getName(),
                    "Error path: " + errorPath.getClassName() + "#" + errorPath.getMethodName() + "() @ line " + errorPath.getLineNumber(),
                    "Error message: " + (e.getMessage() == null ? "none" : "'" + e.getMessage() + "'"));
            e.printStackTrace();
            plugin.error("###########################################");
        }
    }

    public Map<String, BaseCommand> getCommands(@Nullable MatchExtension extension) {
        if (extension == null) return spleefxCommands;
        return extensionCommands.computeIfAbsent(extension.getKey(), k -> new HashMap<>());
    }
}