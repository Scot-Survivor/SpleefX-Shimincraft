package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.core.command.*;
import net.spleefx.extension.MatchExtension;
import net.spleefx.gui.JoinGUI;
import net.spleefx.gui.JoinGUI.MenuSettings;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisteredCommand
public class JoinGUICommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("joingui", "games")
                .requirePlayer()
                .requireNotInArena()
                .permission("spleefx.{ext}.joingui", PermissionDefault.TRUE)
                .checkIfArgsAre(zero())
                .description("Display the join GUI")
                .extensionCommand()
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        MenuSettings menu = MenuSettings.MENU.get();
        return Response.ignore(new JoinGUI(menu, sender.player(), extension));
    }
}