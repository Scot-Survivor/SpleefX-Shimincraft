package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.core.data.menu.StatisticsConfig;
import net.spleefx.extension.MatchExtension;
import net.spleefx.util.Placeholders.CommandEntry;
import net.spleefx.util.message.message.Message;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.spleefx.core.command.tab.TabCompletion.literal;
import static net.spleefx.core.command.tab.TabCompletion.playerList;

@RegisteredCommand
public class StatsCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("stats", "statistics")
                .extensionCommand()
                .checkIfArgsAre(anything())
                .permission("spleefx.{ext}.stats", PermissionDefault.TRUE)
                .description("Display the statistics of a player (or yourself).")
                .parameters("[player]")
                .requirePlayer()
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        Player player = sender.player();
        switch (args.size()) {
            case 0: {
                sender.gui(StatisticsConfig.MENU.get().asInventory(player, extension));
                return Response.ok();
            }
            case 1: {
                String name = args.get(0);
                if (name.equalsIgnoreCase("global")) {
                    sender.gui(StatisticsConfig.MENU.get().asInventory(player, args.containsAny("global") ? null : extension));
                    return Response.ok();
                }

                OfflinePlayer target = args.offlinePlayer(0);
                if (!target.hasPlayedBefore())
                    return Response.error(Message.UNKNOWN_PLAYER, extension, new CommandEntry(null, null, target.getName()));
                sender.gui(StatisticsConfig.MENU.get().asInventory(target, args.containsAny("global") ? null : extension));
                return Response.ok();
            }
        }
        return Response.ok();
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion.start()
                .with(playerList()
                        .then("global"))
                .and(literal("global"));
    }
}
