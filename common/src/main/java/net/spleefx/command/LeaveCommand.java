package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.core.command.*;
import net.spleefx.extension.MatchExtension;
import net.spleefx.util.message.message.Message;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisteredCommand
public class LeaveCommand extends BaseCommand {

    @NotNull @Override
    protected CommandMeta getCommandMeta() {
        return CommandMeta.of("leave", "quit")
                .permission("spleefx.{ext}.leave", PermissionDefault.TRUE)
                .description("Leave the current arena")
                .extensionCommand()
                .requireArena()
                .checkIfArgsAre(zero())
                .build();
    }

    @NotNull
    @Override
    public Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        ReloadedArenaEngine engine = sender.engine();
        MatchPlayer player = sender.arenaPlayer();
        ArenaStage stage = engine.getStage();
        if (stage == ArenaStage.COUNTDOWN || stage == ArenaStage.WAITING || player.isSpectating()/* && engine.getPlayerTeams().containsKey(player)*/) {
            engine.playerLeave(player, true, false);
            return Response.ok(Message.LEFT_THE_ARENA);
        } else if (engine.getStage() == ArenaStage.ACTIVE) {
            engine.playerLeave(player, true, false);
            return Response.ok(Message.LEFT_THE_ARENA_AND_LOST);
        }
        return Response.ok();
    }
}