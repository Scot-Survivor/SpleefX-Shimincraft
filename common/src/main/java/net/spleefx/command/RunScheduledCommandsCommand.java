package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.MatchArena;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.MatchExtension;
import net.spleefx.model.ScheduledCommand;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.spleefx.core.command.tab.TabCompletion.*;

@RegisteredCommand
public class RunScheduledCommandsCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("schedule")
                .permission("spleefx.{ext}.schedule")
                .parameters("<add | list> <arena> <seconds to wait> <command>")
                .description("Add a scheduled command to arenas")
                .extensionCommand()
                .checkIfArgsAre(atLeast(1))
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        MatchArena arena = args.arena(1);
        switch (args.get(0).toLowerCase()) {
            case "add": {
                int seconds = args.integerMin(2, 1);
                String command = args.combine(3);
                if (command.startsWith("/")) {
                    sender.reply(extension, "&c&lNote: &7Commands do not need to be prefixed with &e/&7.");
                }
                arena.getScheduledCommands().add(new ScheduledCommand(command, seconds));
                return Response.ok("&aCommand successfully scheduled.");
            }
            case "remove": {
                int index = args.integerMin(2, 1) - 1;
                try {
                    if (arena.getScheduledCommands().remove(index) != null) {
                        return Response.ok("&aCommand successfully removed.");
                    } else {
                        return Response.error("&cThere was no command at this index!");
                    }
                } catch (IndexOutOfBoundsException e) {
                    return Response.error("&cThere was no command at this index!");
                }
            }
            case "list": {
                if (!arena.getScheduledCommands().isEmpty()) {
                    for (int i = 0; i < arena.getScheduledCommands().size(); i++) {
                        ScheduledCommand sc = arena.getScheduledCommands().get(i);
                        Mson mson = Mson
                                .of(extension.getChatPrefix() + "/" + sc.getCommand())
                                .execute("/" + args.command.getName() + " schedule remove " + arena.getKey() + " " + (i + 1))
                                .tooltip("&aClick to remove");
                        sender.reply(new ComponentJSON().append(mson));
                    }
                    return Response.ok();
                } else {
                    return Response.error("&cArena &e%s &chas no scheduled commands.", arena.getKey());
                }
            }
        }
        return Response.invalidUsage();
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion
                .start()
                .with(literal("add")
                        .then(arenas(extension)
                                .then(list("60", "120", "180", "240", "300"))))
                .and(literal("remove")
                        .then(arenas(extension)
                                .then(listScheduledCommands(args, 1))))
                .and(literal("list")
                        .then(arenas(extension)))
                .end();
    }
}
