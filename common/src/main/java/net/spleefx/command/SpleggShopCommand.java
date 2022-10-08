package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.type.splegg.extension.SpleggShop;
import net.spleefx.core.command.*;
import net.spleefx.extension.MatchExtension;
import net.spleefx.extension.StandardExtensions;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisteredCommand
@RegisterStrictlyFor("splegg")
public class SpleggShopCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("shop")
                .permission("spleefx.splegg.shop", PermissionDefault.TRUE)
                .extensionCommand()
                .checkIfArgsAre(zero())
                .requirePlayer()
                .description("Display the Splegg shop GUI")
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        new SpleggShop.SpleggMenu(StandardExtensions.SPLEGG.getSpleggShop(), sender.player());
        return Response.ok();
    }
}