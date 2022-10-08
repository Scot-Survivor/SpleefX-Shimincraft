package net.spleefx.listeners;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.event.PlayerArenaInteractionListener;
import net.spleefx.extension.MatchExtension;
import net.spleefx.model.Item.CommandItem;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@RegisteredListener
public class CommandItemListener implements PlayerArenaInteractionListener {

    @Override
    public void handle(@NotNull PlayerInteractEvent event, @NotNull MatchArena arena, @NotNull MatchExtension extension, @NotNull MatchPlayer player, ItemStack item, @Nullable Block block, @NotNull Action action, @NotNull SpleefX plugin) {
        switch (arena.getEngine().getStage()) {
            case WAITING:
            case COUNTDOWN: {
                Optional<CommandItem> commandItem = extension.getWaitingCmdItems()
                        .values().stream().filter(i -> i.isSimilar(item)).findFirst();
                commandItem.ifPresent(e -> {
                    if (e.getTriggers() == null || e.getTriggers().contains(action))
                        e.getCommands().forEach(c -> c.execute(player.player(), arena));
                });
            }
            case ACTIVE: {
                Optional<CommandItem> commandItem = extension.getIngameCmdItems()
                        .values().stream().filter(i -> i.isSimilar(item)).findFirst();
                commandItem.ifPresent(e -> {
                    if (e.getTriggers() == null || e.getTriggers().contains(action))
                        e.getCommands().forEach(c -> c.execute(player.player(), arena));
                });
            }
        }
    }
}
