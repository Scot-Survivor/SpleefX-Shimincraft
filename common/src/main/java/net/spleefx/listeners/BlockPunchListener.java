package net.spleefx.listeners;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.player.PlayerState;
import net.spleefx.event.PlayerArenaInteractionListener;
import net.spleefx.event.listen.EventListener;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent.BreakContext;
import net.spleefx.extension.MatchExtension;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisteredListener
public class BlockPunchListener implements PlayerArenaInteractionListener {

    @Override
    public void handle(@NotNull PlayerInteractEvent event, @NotNull MatchArena arena, @NotNull MatchExtension extension, @NotNull MatchPlayer player, @Nullable ItemStack item, @Nullable Block block, @NotNull Action action, @NotNull SpleefX plugin) {
        if (block == null) return;
        if (item != null) return;
        if (player.getState() != PlayerState.PLAYING) return;
        if (arena.getEngine().isGracePeriodActive()) return;
        if (arena.getExtension().getRemoveBlocksWhenPunched().contains(block.getType())) {
            block.setType(Material.AIR);
            EventListener.post(new PlayerDestroyBlockInArenaEvent(player.player(), player.getArena(), block, BreakContext.MINED));
        }
    }

    @Override public boolean requireItem() {
        return false;
    }
}
