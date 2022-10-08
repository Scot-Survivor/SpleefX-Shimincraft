package net.spleefx.spectate;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.player.PlayerState;
import net.spleefx.event.listen.EventListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;

/**
 * Used as a workaround for a bug in 1.8 where {@link TeleportCause#SPECTATE} is not fired.
 */
@RegisteredListener(parameters = RegisteredListener.PLUGIN)
public class ProtocolLibSpectatorAdapter extends PacketAdapter {

    public ProtocolLibSpectatorAdapter(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, Server.CAMERA);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Entity spectatorTarget = event.getPlayer().getSpectatorTarget();
        if (spectatorTarget == null) {
            EventListener.post(new PlayerExitSpectateEvent(event.getPlayer()));
        } else {
            MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
            if (player.isSpectating()) {
                if (!(spectatorTarget instanceof Player)) {
                    event.setCancelled(true);
                } else {
                    MatchPlayer target = MatchPlayer.wrap(spectatorTarget);
                    if (target.getArena() != player.getArena() || target.getState() != PlayerState.PLAYING) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}