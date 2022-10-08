package net.spleefx.arena.ability;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.player.PlayerState;
import net.spleefx.backend.DelayContext;
import net.spleefx.backend.Schedulers;
import net.spleefx.event.PlayerArenaInteractionListener;
import net.spleefx.event.ability.PlayerDoubleJumpEvent;
import net.spleefx.event.listen.EventListener;
import net.spleefx.extension.MatchExtension;
import net.spleefx.extension.ability.GameAbility;
import net.spleefx.model.ability.DoubleJumpOptions;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

@RegisteredListener
public class DoubleJump implements Listener, PlayerArenaInteractionListener {

    /**
     * Fired when a player toggles their flight in a game
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
        if (player.getArena() == null) return;
        if (player.getArena().getExtension().getDoubleJump().isEnabled() && event.isFlying()) {
            doubleJump(player.getArena(), event.getPlayer());
            event.setCancelled(true);
        }
    }

    /**
     * Double-jumps the player in the specified arena
     *
     * @param arena  Arena to double jump in
     * @param player Player to double jump for
     */
    public void doubleJump(MatchArena arena, Player player) {
        MatchPlayer ap = MatchPlayer.wrap(player);
        ReloadedArenaEngine engine = arena.getEngine();
        if (ap.isSpectating()) {
            player.setAllowFlight(true);
            player.setFlying(true);
            return;
        }
        if (Schedulers.DELAY.getTimeLeft(player, DelayContext.DOUBLE_JUMP) > 0) return;
        int v = ap.isSpectating() ? engine.getAbilities().get(player, GameAbility.DOUBLE_JUMP) : engine.getAbilities().consume(player, GameAbility.DOUBLE_JUMP);
        if (v < 0) return;
        PlayerDoubleJumpEvent event = new PlayerDoubleJumpEvent(player, v, arena);
        if (EventListener.post(event)) return;
        player.setVelocity(arena.getExtension().getDoubleJump().getLaunchVelocity().getVector(player));
        if (!ap.isSpectating())
            player.setAllowFlight(false);
        engine.addDoubleJumpItems(ap, false);
        if (arena.getExtension().getDoubleJump().getPlaySoundOnJump() != null)
            player.playSound(player.getLocation(), arena.getExtension().getDoubleJump().getPlaySoundOnJump(), 1, 1);
        player.setFallDistance(-500);
        Schedulers.DELAY.delay(player, DelayContext.DOUBLE_JUMP, arena.getExtension().getDoubleJump().getCooldownBetween(), TimeUnit.SECONDS).thenRun(() -> {
            try {
                if (v > 0) {
                    MatchPlayer arenaPlayer = MatchPlayer.wrap(player);
                    arenaPlayer.sync((mp, pl) -> {
                        if (mp.getState() == PlayerState.PLAYING) {
                            mp.allowFlying(true);
                            engine.addDoubleJumpItems(arenaPlayer, true);
                        } else if (arenaPlayer.isSpectating()) {
                            pl.setAllowFlight(true);
                        }
                    });
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override public void handle(@NotNull PlayerInteractEvent event, @NotNull MatchArena arena, @NotNull MatchExtension extension, @NotNull MatchPlayer player, @NotNull ItemStack item, @Nullable Block block, @NotNull Action action, @NotNull SpleefX plugin) {
        if (arena.getEngine().getStage() != ArenaStage.ACTIVE) return;
        DoubleJumpOptions settings = extension.getDoubleJump();
        if (settings.isEnabled() && settings.getDoubleJumpItems().isEnabled() && settings.getDoubleJumpItems().getAvailable().isSimilar(event.getItem())) {
            doubleJump(player.getArena(), event.getPlayer());
            event.setCancelled(true);
        }
    }
}
