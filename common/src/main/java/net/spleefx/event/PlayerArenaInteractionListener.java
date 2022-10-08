package net.spleefx.event;

import net.spleefx.SpleefX;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.extension.MatchExtension;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface PlayerArenaInteractionListener {

    default void accept(@NotNull PlayerInteractEvent event,
                        @NotNull MatchArena arena,
                        @NotNull MatchExtension extension,
                        @NotNull MatchPlayer player,
                        @Nullable ItemStack item,
                        @Nullable Block block,
                        @NotNull Action action,
                        @NotNull SpleefX plugin) {
        try {
            if (requireItem() && item == null) return;
            handle(event, arena, extension, player, item, block, action, plugin);
        } catch (Throwable t) {
            SpleefX.logger().warning("Failed to dispatch PlayerInteractEvent to listener " + getClass().getName() + ":");
            t.printStackTrace();
        }
    }

    void handle(
            @NotNull PlayerInteractEvent event,
            @NotNull MatchArena arena,
            @NotNull MatchExtension extension,
            @NotNull MatchPlayer player,
            ItemStack item,
            @Nullable Block block,
            @NotNull Action action,
            @NotNull SpleefX plugin
    );

    default boolean requireItem() {
        return true;
    }

}
