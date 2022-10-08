package net.spleefx.spectate;

import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.player.PlayerState;
import net.spleefx.event.ability.PlayerDoubleJumpEvent;
import net.spleefx.event.listen.EventListener;
import net.spleefx.event.listen.EventListenerAdapter;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.spleefx.SpleefX.getSpectatorSettings;

@RegisteredListener
public class SpectatorListener extends EventListenerAdapter implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (isSpectating(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (isSpectating(event.getTarget())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isSpectating(event.getDamager())) return;
        if (event.getEntityType() != EntityType.PLAYER || isSpectating(event.getEntity())) {
            event.setCancelled(true);
            return;
        }
        MatchPlayer target = MatchPlayer.wrap(event.getEntity());
        if (target.getState() == PlayerState.PLAYING) {
            if (getSpectatorSettings().isCanGetInsidePlayers()) {
                Player spectator = ((Player) event.getDamager());
                spectator.setGameMode(GameMode.SPECTATOR);
                spectator.setSpectatorTarget(event.getEntity());
                EventListener.post(new PlayerSpectateAnotherEvent(spectator, target.player(), target.getArena()));
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true) public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != TeleportCause.SPECTATE) return;
        MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
        Entity spectatorTarget = player.player().getSpectatorTarget();
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        MatchPlayer mPlayer = getSpectatingState(event.getPlayer());
        if (mPlayer == null) return;
        event.setCancelled(true);
        if (event.getItem() == null) return;
        MatchArena arena = mPlayer.getArena();
        ItemStack item = event.getItem();
        if (getSpectatorSettings().getSpectateItem().isSimilar(item)) {
            if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                event.getPlayer().setSpectatorTarget(null);
            }
            new SpectatePlayerMenu(arena).display(event.getPlayer());
        } else if (getSpectatorSettings().getExitSpectatingItem().isSimilar(item)) {
            mPlayer.getArena().getEngine().playerLeave(mPlayer, true, false);
        }
    }

    @EventHandler(ignoreCancelled = true) public void onInventoryClick(InventoryClickEvent event) {
        if (!isSpectating(event.getWhoClicked())) return;
        if (!(event.getClickedInventory() instanceof PlayerInventory)) return;
        if (!getSpectatorSettings().getExitSpectatingItem().isSimilar(event.getCurrentItem())) return;
        event.setCancelled(true);
        MatchPlayer mPlayer = MatchPlayer.wrap(event.getWhoClicked());
        mPlayer.getArena().getEngine().playerLeave(mPlayer, true, false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (isSpectating(event.getWhoClicked())) event.setCancelled(true);
    }

    @Override
    public void onPlayerSpectateAnother(@NotNull PlayerSpectateAnotherEvent event) {
        if (getSpectatorSettings().isEnabled()) {
            getSpectatorSettings().getSpectatingActionBar().display(event.getSpectator(), event.getTarget());
        }
    }

    @Override
    public void onPlayerDoubleJump(@NotNull PlayerDoubleJumpEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().setAllowFlight(true);
            event.getPlayer().setFlying(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (isSpectating(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (isSpectating(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isSpectating(event.getPlayer())) event.setCancelled(true);
    }

    @Override
    public void onPlayerExitSpectate(PlayerExitSpectateEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.getPlayer().setGameMode(getSpectatorSettings().getGameMode());
            event.getPlayer().setAllowFlight(true);
            event.getPlayer().setFlying(true);
        }
    }

    private static boolean isSpectating(Entity e) {
        return getSpectatingState(e) != null;
    }

    @Nullable
    private static MatchPlayer getSpectatingState(@Nullable Entity pl) {
        if (pl == null) return null;
        if (!(pl instanceof Player)) return null;
        if (!getSpectatorSettings().isEnabled()) return null;
        MatchPlayer player = MatchPlayer.wrap((Player) pl);
        if (player.getArena() == null) return null;
        if (!player.isSpectating()) return null;
        return player;
    }

    public static class PickupListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
            if (isSpectating(event.getPlayer())) event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void onEntityPickupItem(EntityPickupItemEvent event) {
            if (isSpectating(event.getEntity())) event.setCancelled(true);
        }
    }
}
