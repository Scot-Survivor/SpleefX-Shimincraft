package net.spleefx.event.trigger;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.event.PlayerArenaInteractionListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class StandardCallbacks {

    private static final Set<PlayerArenaInteractionListener> interact = new HashSet<>();

    public static void register(@NotNull Object listener) {
        if (listener instanceof PlayerArenaInteractionListener)
            interact.add((PlayerArenaInteractionListener) listener);
    }

    @RegisteredListener
    public static class PIListener implements Listener {

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
            if (player.getArena() == null) return;
            for (PlayerArenaInteractionListener listener : interact) {
                try {
                    listener.accept(event, player.getArena(), player.getArena().getExtension(), player, event.getItem(), event.getClickedBlock(), event.getAction(), SpleefX.getSpleefX());
                } catch (Throwable t) {
                    SpleefX.logger().warning("Failed to dispatch PlayerInteractEvent to listener " + listener);
                    t.printStackTrace();
                }
            }

        }
    }

}
