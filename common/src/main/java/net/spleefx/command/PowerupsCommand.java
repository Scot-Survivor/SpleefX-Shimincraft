package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.extension.MatchExtension;
import net.spleefx.gui.PowerupsGUI;
import net.spleefx.powerup.api.Powerup;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.spleefx.core.command.tab.TabCompletion.arenas;
import static net.spleefx.core.command.tab.TabCompletion.of;

@RegisteredCommand
public class PowerupsCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("powerups", "powerup")
                .extensionCommand()
                .requirePlayer()
                .permission("spleefx.{ext}.powerups")
                .parameters("<arena>")
                .description("Display the power-ups GUI for the arena")
                .checkIfArgsAre(atLeast(1))
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        if (!Powerup.HD) {
            sender.reply(extension, "&cYou do not have Holographic Displays. Power-ups will not work.");
            sender.reply(extension, "&eGet Holographic Displays at &bhttps://dev.bukkit.org/projects/holographic-displays.");
        } else {
            sender.gui(new PowerupsGUI(args.arena(0)));
        }
        return Response.ok();
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return of(arenas(extension));
    }
}
