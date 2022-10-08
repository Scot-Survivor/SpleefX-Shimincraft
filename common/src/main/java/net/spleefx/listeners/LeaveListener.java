package net.spleefx.listeners;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.event.PlayerArenaInteractionListener;
import net.spleefx.extension.MatchExtension;
import net.spleefx.model.QuitItem;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisteredListener
public class LeaveListener implements PlayerArenaInteractionListener {

    @Override public void handle(@NotNull PlayerInteractEvent event, @NotNull MatchArena arena, @NotNull MatchExtension extension, @NotNull MatchPlayer player, @NotNull ItemStack item, @Nullable Block block, @NotNull Action action, @NotNull SpleefX plugin) {
        QuitItem quitItem = extension.getQuitItem();
        if (!action.name().startsWith("RIGHT")) return;
        if (!quitItem.isGive()) return;
        if (!quitItem.isSimilar(item)) return;
        if (quitItem.leaveArena()) {
            SpleefX.nextTick(() -> arena.getEngine().playerLeave(player, true, false));
        }
        Player pl = player.player();
        quitItem.getRunCommandsByPlayer().forEach(pl::performCommand);
        event.setCancelled(true);
    }
}
