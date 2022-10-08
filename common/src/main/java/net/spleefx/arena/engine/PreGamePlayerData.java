package net.spleefx.arena.engine;

import net.spleefx.SpleefX;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.model.Position;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static net.spleefx.util.Placeholders.firstNotNull;

public class PreGamePlayerData {

    private final ItemStack[] armor, items;
    private final Collection<PotionEffect> effects;

    private final int xp;
    private final float exp;

    private final double health;
    private final double maxHealth;
    private final int hunger;

    private final int fireTicks;
    private final Position position;
    private GameMode gameMode;

    private boolean allowFlight;
    private final boolean flying;

    public PreGamePlayerData(Player player) {
        position = Position.at(player.getLocation());
        gameMode = player.getGameMode();
        items = player.getInventory().getContents();
        armor = player.getInventory().getArmorContents();
        effects = player.getActivePotionEffects();
        xp = player.getLevel();
        exp = player.getExp();
        health = player.getHealth();
        maxHealth = PluginCompatibility.getMaxHealth(player);
        hunger = player.getFoodLevel();
        fireTicks = player.getFireTicks();
        allowFlight = player.getAllowFlight();
        flying = player.isFlying();
    }

    public void capture(@NotNull MatchPlayer player) {
        player.clearPotions()
                .clearInventory()
                .armor(EMPTY_ARMOR)
                .expLevel(0)
                .exp(0)
                .foodLevel(20)
                .health(20)
                .fireTicks(0)
                .flying(false);
    }

    public CompletableFuture<Void> load(MatchPlayer pl, MatchArena arena) {
        return load(pl, arena, false);
    }

    public CompletableFuture<Void> load(MatchPlayer pl, MatchArena arena, boolean now) {
        pl.clearInventory();
        pl.gamemode(gameMode);
        Location targetLocation = firstNotNull(arena.getFinishingLocation(), position).centeredLoc();

        Runnable teleport = () -> pl.teleport(targetLocation);
        Runnable load = () -> {
            Player player = pl.player();
            player.getInventory().setContents(items);
            player.getInventory().setArmorContents(armor);

            player.getActivePotionEffects().forEach(p -> player.removePotionEffect(p.getType()));
            effects.forEach(player::addPotionEffect);

            player.setLevel(xp);
            player.setExp(exp);

            if (!player.getWorld().equals(targetLocation.getWorld()))
                SpleefX.getSpleefX().getScoreboardThread().getBoards().remove(player.getUniqueId());

            try {
                player.setHealth(health >= 24 ? 24 : health < 0 ? 1 : health);
            } catch (Exception e) {
                try {
                    player.setHealth(20);
                } catch (Exception ex) {
                    player.setHealth(Math.max(0, Math.min(maxHealth, 20)));
                }
            }
            player.setFoodLevel(hunger);
            player.setFireTicks(fireTicks);
            player.setAllowFlight(allowFlight);
            player.setFlying(flying);
        };
        if (now) {
            teleport.run();
            load.run();
            return CompletableFuture.completedFuture(null);
        }
        return SpleefX.nextTick(teleport).thenRun(() -> SpleefX.nextTick(load));
    }

    private static final ItemStack[] EMPTY_ARMOR = new ItemStack[4];
}