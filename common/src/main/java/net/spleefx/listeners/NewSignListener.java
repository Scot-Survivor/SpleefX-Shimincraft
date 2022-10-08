package net.spleefx.listeners;

import com.google.common.collect.ImmutableList;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.model.Position;
import net.spleefx.util.game.Chat;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@RegisteredListener
public class NewSignListener implements Listener {

    private static final ImmutableList<String> LINES = ImmutableList.of("[spleefx]", "[spleef]", "[splegg]", "[bow_spleef]", "[bowspleef]");
    private static final String INVALID = ChatColor.RED + "Invalid arena";
    private static final String HEADER = Chat.colorize("&7[&cSpleefX&7]");
    private static final String FOOTER = Chat.colorize("&4Fetching arena...");

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!event.getPlayer().hasPermission("spleefx.create_arena_sign")) return;
        String header = event.getLine(0);
        if (header == null || !LINES.contains(header.toLowerCase())) return;
        event.setLine(0, HEADER);
        String arenaKey = event.getLine(1);

        if (arenaKey == null) {
            event.setLine(1, INVALID);
            return;
        }

        MatchArena arena = MatchArena.getByKey(arenaKey);
        if (arena == null) {
            event.setLine(1, INVALID);
            return;
        }

        arena.getSigns().add(Position.at(event.getBlock()));
        arena.getSignHandler().updateSign(event);
    }

    @SuppressWarnings("ConstantConditions")
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && player.getGameMode() == GameMode.CREATIVE)
            return;
        BlockState state = event.getClickedBlock().getState();
        if (!(state instanceof Sign)) return;
        Position position = Position.at(event.getClickedBlock());
        MatchArena arena = Arenas.find(e -> e.getSigns().contains(position));
        if (arena == null) return;
        event.setCancelled(true);
        SpleefX.nextTick(() -> arena.getEngine().playerJoin(MatchPlayer.wrap(player), false).handle(player));
    }
}
