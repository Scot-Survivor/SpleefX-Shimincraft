package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.core.data.PlayerRepository;
import net.spleefx.extension.MatchExtension;
import net.spleefx.util.Placeholders;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.spleefx.core.command.tab.TabCompletion.playerList;

@RegisteredCommand
public class BalanceCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("balance", "bal", "money")
                .requirePlayer()
                .description("Get your balance")
                .permission("spleefx.balance", PermissionDefault.TRUE)
                .checkIfArgsAre(lessThan(2))
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        if (args.size() == 0) {
            return Response.ok("&eYour money: &a$" + Placeholders.formatNumber(PlayerRepository.REPOSITORY.lookup(sender.player()).getCoins()));
        }
        OfflinePlayer p = args.offlinePlayer(0);
        return Response.ok("&e" + p.getName() + "&a's money: &e$" + Placeholders.formatNumber(args.profile(p).getCoins()));
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion.of(playerList());
    }
}