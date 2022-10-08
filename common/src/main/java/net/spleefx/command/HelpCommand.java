package net.spleefx.command;

import lombok.AllArgsConstructor;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.compatibility.chat.ChatComponent;
import net.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import net.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import net.spleefx.util.paginate.ListPaginator;
import net.spleefx.util.paginate.ListPaginator.MessageConverter;
import net.spleefx.util.paginate.ListPaginator.MessagePlatform;
import net.spleefx.util.paginate.PageFooter;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static net.spleefx.backend.Schedulers.sneakyThrow;
import static net.spleefx.core.command.tab.TabCompletion.commandList;
import static net.spleefx.core.command.tab.TabCompletion.pages;

@RegisteredCommand
public class HelpCommand extends BaseCommand {

    private static final ComponentJSON JSON = new ComponentJSON();
    private static final MessagePlatform<ChatComponent> sender = (target, message) -> {
        synchronized (JSON) {
            JSON.clear().append(message).send(target);
        }
    };

    private static final MessageConverter<HelpEntry, ChatComponent> converter = entry -> {
        BaseCommand command = entry.command;
        StringBuilder shortName = new StringBuilder("&d/" + entry.bukkitCommand.getName() + " ");
        CommandMeta meta = command.meta;
        shortName.append("&b").append(meta.name);
        if (!meta.parameters.isEmpty())
            shortName.append(" &e").append(meta.parameters);
        shortName.append(" &0- ");
        shortName.append("&c").append(meta.description);

        StringJoiner hover = new StringJoiner("\n");
        hover.add("&e&lDescription&f: " + meta.description);
        hover.add("&e&lAliases&f: " + (meta.aliases.isEmpty() ? "none" : String.join(",", meta.aliases)));
        if (!meta.parameters.isEmpty())
            hover.add("&e&lParameters&f: " + (meta.parameters.equalsIgnoreCase("help") ? "&b&lSee help" : meta.parameters));
        if (meta.permission != null && meta.permission.test(entry.sender.sender, Extensions.getByCommand(entry.bukkitCommand.getName())))
            hover.add("&e&lPermission node&f: " + meta.permission.node().replace("{ext}", "&b<extension>&r"));

        StringBuilder suggestion = new StringBuilder("/" + entry.bukkitCommand.getName());
        suggestion.append(" ").append(meta.name);
        if (!meta.parameters.isEmpty()) suggestion.append(" ");
        return new ChatComponent().setText(shortName.toString())
                .setHoverAction(HoverEvent.SHOW_TEXT, hover.toString())
                .setClickAction(ClickEvent.SUGGEST_COMMAND, suggestion.toString());
    };

    private static final ListPaginator<HelpEntry, ChatComponent> PAGINATOR = new ListPaginator<>(7, sender, converter)
            .setHeader(CommandMeta.HEADER)
            .ifPageIsInvalid((sender, integer) -> sneakyThrow(new CommandException("&cInvalid help page: " + integer)));


    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("help", "?", "-?")
                .permission("spleefx.help")
                .parameters("[command] [help page]")
                .description("List all commands")
                .withHelpMenu("/{cmd} help [command] [help page]")
                .registerBoth()
                .checkIfArgsAre(anything())
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        Map<String, BaseCommand> commandMap = commandHandler.getCommands(extension);
        List<HelpEntry> help = commandMap.values().stream()
                .distinct()
                .filter(c -> !(c instanceof HelpCommand)) // duh
                .filter(c -> c.meta.permission.test(sender.sender, extension))
                .map(c -> new HelpEntry(c, args.getCommand(), sender))
                .collect(Collectors.toList());
        int page = 1;
        switch (args.size()) {
            case 0: { // sends the first help page
                PAGINATOR.sendPage(help, sender.sender, page, new PageFooter(args.getCommand(), "", JSON));
                return Response.ok();
            }
            case 1: { // could either have supplied a page index or a command. check for index first then command.
                try {
                    page = args.integer(0, 1, PAGINATOR.getPageSize(help));
                    PAGINATOR.sendPage(help, sender.sender, page, new PageFooter(args.getCommand(), "", JSON));
                    return Response.ok();
                } catch (CommandException ignored) {
                    String cmd = args.get(0);
                    BaseCommand command = commandMap.get(args.get(0));
                    if (command == null)
                        return Response.error("&cInvalid command: &e" + cmd);
                    return sendHelp(command.meta, 1, sender, args);
                }
            }
            default: {
                String cmd = args.get(0);
                BaseCommand command = commandMap.get(args.get(0));
                if (command == null)
                    return Response.error("&cInvalid command: &e" + cmd);
                page = args.integer(1, 1, PAGINATOR.getPageSize(command.meta.helpMenu));
                return sendHelp(command.meta, page, sender, args);
            }
        }
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        Map<String, BaseCommand> commandMap = commandHandler.getCommands(extension);
        return TabCompletion.of(commandList(commandMap)
                .then(pages(commandMap, args, 0)));
    }

    private Response sendHelp(CommandMeta meta, int page, PromptSender sender, CommandArgs args) throws CommandException {
        List<String> helpM = meta.helpMenu.stream().map(c -> c.replace("/{cmd}", ("&a/" + args.getCommand().getName() + "&d"))).collect(Collectors.toList());
        if (helpM.isEmpty()) {
            return Response.error("&cNo help menu for this command.");
        }
        meta.helpPaginator.sendPage(helpM, sender.sender, page, new PageFooter(args.getCommand(), meta.name, meta.helpJSON));
        return Response.ok();
    }

    @AllArgsConstructor
    private static class HelpEntry {

        private BaseCommand command;
        private Command bukkitCommand;
        private PromptSender sender;
    }

}
