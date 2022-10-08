package net.spleefx.spectate;

import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.player.PlayerState;
import net.spleefx.compatibility.PluginCompatibility;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import static net.spleefx.SpleefX.getSpectatorSettings;

public final class Spectating {

    public static void on(@NotNull MatchPlayer player) {
        player.setSpectating(true);
        player.setState(PlayerState.SPECTATING);
        player.allowFlying(true);
        player.gamemode(getSpectatorSettings().getGameMode());
        player.clearInventory();
        player.clearPotions();
        player.armor(new ItemStack[4]);
        player.health(20);
        player.foodLevel(20);
        player.setCollidable(false);
        if (player.player().getGameMode() != GameMode.SPECTATOR) {
            getSpectatorSettings().getSpectateItem().give(player.player());
            getSpectatorSettings().getExitSpectatingItem().give(player.player());
        }
        for (PotionEffect effect : getSpectatorSettings().getGivePotionEffects())
            player.addPotions(effect);
        for (Player p : Bukkit.getOnlinePlayers())
            PluginCompatibility.hidePlayer(p, player.player());

    }

    public static void disable(@NotNull MatchPlayer player) {
        player.setSpectating(false);
        player.setState(PlayerState.NOT_IN_GAME);
        player.setCollidable(true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            PluginCompatibility.showPlayer(p, player.player());
        }
    }

    public enum SpectatingCause {
        JOINED,
        DIED
    }

}
