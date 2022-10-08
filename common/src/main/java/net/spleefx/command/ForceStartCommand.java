package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.core.command.*;
import net.spleefx.extension.MatchExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisteredCommand
public class ForceStartCommand extends BaseCommand {

    @NotNull @Override protected CommandMeta getCommandMeta() {
        return CommandMeta.of("forcestart")
                .permission("spleefx.admin.forcestart")
                .description("Forcibly start the arena")
                .extensionCommand()
                .requirePlayer()
                .requireArena()
                .checkIfArgsAre(zero())
                .build();
    }

    @Override
    public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        ReloadedArenaEngine engine = sender.engine();
        if (engine.getStage() != ArenaStage.COUNTDOWN) {
            return Response.error("&cThe arena hasn't started countdown yet!");
        }
        sender.engine().setCountdown(0);
        return Response.ok("&aForcibly starting arena!");
    }

}
