package net.spleefx.core.command.internal;

import net.spleefx.SpleefX;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ForwardingCommandExecutor implements CommandExecutor {

    private final SpleefX plugin;

    public static ForwardingCommandExecutor instance;

    public ForwardingCommandExecutor(SpleefX plugin) {
        this.plugin = plugin;
        instance = this;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.getCommandHandler().execute(command, sender, label, args);
        return true;
    }
}
