package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.MatchExtension;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

import static net.spleefx.core.command.tab.TabCompletion.playerList;

@RegisteredCommand
public class DebugPlayerCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("debugplayer")
                .description("Debug a player")
                .checkIfArgsAre(atLeast(1))
                .registerBoth()
                .permission("spleefx.debugplayer")
                .parameters("<player>")
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        MatchPlayer player = MatchPlayer.wrap(args.player(0));
        sender.reply("&aState: &e" + player.getState().name().toLowerCase().replace('_', ' '));
        sender.reply("&aArena: &e" + (player.getArena() == null ? "None" : player.getArena().getKey()));
        sender.reply("&aSpectating: &e" + (player.isSpectating()));
        if (player.getArena() != null) {
            sender.reply("&aArena state: &e" + (player.getArena().getEngine().getStage().getState()));
            sender.reply("&aArena players: &e" + (player.getArena().getEngine().getPlayers().stream().map(MatchPlayer::name).collect(Collectors.joining(", "))));
            sender.reply("&aArena spectators: &e" + (player.getArena().getEngine().getSpectators().stream().map(MatchPlayer::name).collect(Collectors.joining(", "))));
        }
        return Response.ok();
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion.of(playerList());
    }
}
