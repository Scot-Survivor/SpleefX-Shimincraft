package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.extension.MatchExtension;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.spleefx.core.command.tab.TabCompletion.*;

@RegisteredCommand
public class CoinsCommand extends BaseCommand {

    private static final List<String> NUM_COMPLETIONS = IntStream.rangeClosed(100, 901).filter(i -> i % 100 == 0)
            .mapToObj(Integer::toString).collect(Collectors.toList());

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("coins", "coin")
                .parameters("<add / take / set / reset> <player> <value?>")
                .withHelpMenu(
                        "/{cmd} coins reset <player> - Reset a player's coins",
                        "/{cmd} coins add <player> <value> - Give coins to the specified player",
                        "/{cmd} coins set <player> <value> - Set coins of the specified player",
                        "/{cmd} coins take <player> <value> - Take coins from the specified player"
                )
                .permission("spleefx.admin.coins")
                .description("Manage the coins of a specified player")
                .checkIfArgsAre(between(2, 3))
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        if (SpleefXConfig.otherEconomy()) {
            return Response.error("&cSpleefX is not your economy plugin! Modify the coins with your native economy system (&e'%s'&c)", plugin.getVaultHandler().getEconomy().getName());
        }
        OfflinePlayer player = args.offlinePlayer(1);
        PlayerProfile profile = args.profile(player);
        switch (args.size()) {
            case 2: {
                if ("reset".equalsIgnoreCase(args.get(0))) {
                    profile.asBuilder().setCoins(0).push();
                    return Response.ok("&aSuccessfully reset &e%s&a's coins.", player.getName());
                }
                return Response.invalidUsage();
            }
            case 3: {
                int value = args.integer(2);
                switch (args.get(0).toLowerCase()) {
                    case "add":
                    case "give": {
                        profile.asBuilder().addCoins(value).push();
                        return Response.ok("&aSuccessfully given &b%s &ato &e%s&a.", value, player.getName());
                    }
                    case "take":
                    case "remove":
                    case "delete": {
                        profile.asBuilder().subtractCoins(value).push();
                        return Response.ok("&aSuccessfully taken &b%s &afrom &e%s&a.", value, player.getName());
                    }
                    case "set": {
                        profile.asBuilder().setCoins(value).push();
                        return Response.ok("&aSuccessfully set the coins of &e%s &ato &b%s&a.", player.getName(), value);
                    }
                    default:
                        return Response.sendHelp();
                }
            }
        }
        return Response.invalidUsage();
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion.start()
                .with(literal("reset")
                        .then(playerList()))
                .and(list("add", "take", "set")
                        .then(playerList()
                                .then(list("100", "200", "300"))));
    }
}