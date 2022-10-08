package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.spleefx.core.command.tab.TabCompletion.*;

@RegisteredCommand
public class ExtensionsCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("extensions", "extension")
                .permission("spleefx.admin.extensions")
                .checkIfArgsAre(atLeast(1))
                .description("Manage extensions")
                .parameters("help")
                .withHelpMenu(
                        "/{cmd} extensions load <extension> - Load the specified extension",
                        "/{cmd} extensions reload <extension> - Reload the specified extension",
                        "/{cmd} extensions enable <extension> - Enable the specified extension",
                        "/{cmd} extensions disable <extension> - Disable the specified extension",
                        "/{cmd} extensions toggle <extension> - Toggle an extension"
                )
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension e) throws CommandException {
        if (args.get(0).equalsIgnoreCase("help")) return Response.sendHelp();
        String key = args.get(1);
        MatchExtension extension = Extensions.getByKey(key);
        if (extension == null && !args.get(0).equalsIgnoreCase("load")) {
            return Response.error("&cExtension &e%s &cdoes not exist.", key);
        }
        if (extension == null) return Response.error("&cExtension &e%s &cdoes not exist.", key);
        boolean silent = args.flag("silent");
        switch (args.get(0).toLowerCase()) {
            case "load": {
                extension = Extensions.load(key);
                if (extension == null) return Response.error("&cExtension &e%s &cdoes not exist.", key);
                return silent ? Response.ok() : Response.ok(extension, "&aExtension &e" + key + " &ahas been successfully loaded.");
            }
            case "reload": {
                extension.reload();
                sender.reply("&cNote: This command has been deprecated and will be removed in future releases. Use &e/spleefx reload &cinstead.");
                return silent ? Response.ok() : Response.ok(extension, "&aSuccessfully reloaded extension &e%s&a.", extension.getKey());
            }
            case "enable": {
                extension.setEnabled(true);
                return silent ? Response.ok() : Response.ok(extension, "&aExtension &e%s &ahas been enabled.", extension.getKey());
            }
            case "disable": {
                extension.setEnabled(false);
                return silent ? Response.ok() : Response.ok(extension, "&cExtension &e%s &chas been disabled.", extension.getKey());
            }
            case "toggle": {
                extension.setEnabled(!extension.isEnabled());
                return extension.isEnabled() ?
                        silent ? Response.ok() : Response.ok(extension, "&aExtension &e%s &ahas been enabled.", extension.getKey()) :
                        silent ? Response.ok() : Response.ok(extension, "&cExtension &e%s &chas been disabled.", extension.getKey());
            }
        }
        return silent ? Response.ok() : Response.sendHelp();
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion.start()
                .with(literal("help"))
                .then(list("reload", "toggle")
                        .then(TabCompletion.extensions()))
                .then(literal("enable")
                        .then(TabCompletion.extensions(MatchExtension::isDisabled)))
                .then(literal("disable")
                        .then(TabCompletion.extensions(MatchExtension::isEnabled)))
                .and(literal("load")
                        .then(empty()
                                .then(list("standard", "custom"))));
    }
}