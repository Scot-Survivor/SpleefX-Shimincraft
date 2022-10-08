package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.MatchExtension;
import net.spleefx.util.message.message.Message;
import org.bukkit.command.Command;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.spleefx.arena.Arenas.pick;

@RegisteredCommand
public class JoinCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("join")
                .requireNotInArena()
                .extensionCommand()
                .parameters("[arena]")
                .description("Join an arena")
                .permission("spleefx.{ext}.join", PermissionDefault.TRUE)
                .checkIfArgsAre(anything())
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        MatchPlayer player = sender.arenaPlayer();
        if (args.size() == 0) {
            MatchArena arena = pick(Objects.requireNonNull(extension, "extension is null!"));
            if (arena == null) {
                return Response.error(Message.NO_AVAILABLE_ARENA, extension);
            }
            arena.getEngine().playerJoin(player, false).handle(player);
            return Response.ok();
        }
        MatchArena arena = args.arena(0);
        arena.getEngine().playerJoin(player, false).handle(player);
        return Response.ok();
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion.of(TabCompletion.arenas(extension));
    }

}