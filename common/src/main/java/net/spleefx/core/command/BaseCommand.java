package net.spleefx.core.command;

import net.spleefx.SpleefX;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.extension.MatchExtension;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseCommand extends ArgumentPredicates {

    public final CommandMeta meta = getCommandMeta();

    protected CommandHandler commandHandler;

    @NotNull
    protected abstract CommandMeta getCommandMeta();

    @NotNull
    public abstract Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException;

    @Nullable
    public RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return null;
    }

}