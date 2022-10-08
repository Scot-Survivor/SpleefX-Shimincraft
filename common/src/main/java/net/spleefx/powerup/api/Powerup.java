package net.spleefx.powerup.api;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.ToString;
import net.spleefx.SpleefX;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.backend.DelayContext;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.model.Item;
import net.spleefx.model.Position;
import net.spleefx.util.Util;
import net.spleefx.util.game.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import static net.spleefx.backend.Schedulers.DELAY;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.bukkit.ChatColor.COLOR_CHAR;

/**
 * Represents a powerup
 */
@Getter
@ToString
public abstract class Powerup {

    public static final boolean HD = Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null;
    private static final AtomicInteger PID = new AtomicInteger();
    private static final Pattern STRIP_COLOR_ONLY = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-F]");

    private static final double PII = 2 * Math.PI;

    protected String name;
    protected int timeout;
    protected @Nullable XSound activatedSound;
    protected Item powerupIcon;
    protected String displayText;
    protected int blinkAt;
    protected String textOnPick;

    /**
     * The reload function of the powerup
     */
    Runnable reload;

    protected void preSpawn(@NotNull ReloadedArenaEngine engine,
                            @NotNull Position position,
                            @NotNull SpleefX plugin) throws Throwable {
    }

    /**
     * Powerup callback when it is taken
     *
     * @param player   Player that took the powerup
     * @param engine   The arena engine
     * @param position The location of the power up
     * @param plugin   The plugin
     */
    public abstract void onActivate(@NotNull MatchPlayer player,
                                    @NotNull ReloadedArenaEngine engine,
                                    @NotNull Position position,
                                    @NotNull SpleefX plugin) throws Throwable;

    /**
     * Reloads the task settings
     */
    public void reload() {
        reload.run();
    }

    /**
     * Spawns the power up at the given location for the arena
     *
     * @param arena    Arena to spawn for
     * @param location Location to spawn the power up in.
     */
    public PowerupLifecycle spawn(@NotNull MatchArena arena, @NotNull Location location) {
        //<editor-fold desc="spawn" defaultstate="collapsed">
        if (!HD) return PowerupLifecycle.failed(arena, this);
        try {
            int id = PID.getAndIncrement();
            ScheduledFuture<?> vortex = vortex(3, 25,
                    ParticleDisplay.colored(location, Color.CYAN, 5));
            ItemStack icon = powerupIcon.createItem();
            Location l = location.clone().add(0.0, 1.2, 0.0);
            Position position = Position.at(l);
            preSpawn(arena.getEngine(), position, SpleefX.getSpleefX());
            final Hologram hologram = HologramsAPI.createHologram(SpleefX.getPlugin(), l);
            String text = Chat.colorize(displayText);
            String stripped = STRIP_COLOR_ONLY.matcher(text).replaceAll("");
            TextLine line = hologram.appendTextLine(text);
            ItemLine itemLine = hologram.appendItemLine(icon);
            AtomicLong timeout = new AtomicLong(getTimeout());
            Runnable destroy = () -> {
                hologram.delete();
                vortex.cancel(false);
            };
            itemLine.setPickupHandler((player) -> {
                try {
                    MatchPlayer mp = MatchPlayer.wrap(player);
                    if (mp.isSpectating()) return;
                    if (mp.getArena() != arena) return;
                    if (DELAY.hasDelay(mp, DelayContext.POWER_UP)) return;
                    DELAY.delay(mp, DelayContext.POWER_UP, SpleefXConfig.DELAY_BETWEEN_TAKING.get(), TimeUnit.SECONDS);
                    if (activatedSound != null)
                        activatedSound.play(player);
                    player.playEffect(hologram.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
                    onActivate(mp, arena.getEngine(), position, SpleefX.getSpleefX());
                    if (isNotEmpty(textOnPick)) {
                        mp.msg(textOnPick, arena.getExtension());
                    }
                } catch (Throwable e) {
                    SpleefX.logger().warning("Couldn't call onActivate() on powerup " + getClass().getSimpleName() + ":");
                    e.printStackTrace();
                }
                PowerupLifecycle des = arena.getEngine().getActivePowerUps().remove(id);
                if (des != null) des.destroy();
            });
            ScheduledFuture<?> timer = SpleefX.SCHEDULED_SERVICE.scheduleAtFixedRate(() -> {
                long n = timeout.decrementAndGet();
                if (n <= blinkAt) {
                    if (n % 2 == 0) {
                        line.setText(text);
                    } else {
                        line.setText(stripped);
                    }
                }
                if (n <= 0) {
                    destroy.run();
                    throw new RuntimeException("Completed");
                }
            }, 0, 1, TimeUnit.SECONDS);
            PowerupLifecycle lifecycle = new PowerupLifecycle() {//@formatter:off
                @Override public int getId() { return id; }
                @Override public @NotNull MatchArena getArena() { return arena; }
                @Override public @NotNull ReloadedArenaEngine getEngine() { return arena.getEngine(); }
                @Override public @NotNull Powerup getPowerup() { return Powerup.this; }
                @Override public long getTimeout() { return timeout.get(); }
                @Override public void setTimeout(long value) { timeout.set(Math.min(value, 0)); }
                @Override public void destroy() { destroy.run(); timer.cancel(false); }
            }; //@formatter:on
            arena.getEngine().getActivePowerUps().put(id, lifecycle);
            return lifecycle;
        } catch (Throwable e) {
            e.printStackTrace();
            return PowerupLifecycle.failed(arena, this);
        }
        //</editor-fold>
    }

    public static ScheduledFuture<?> vortex(int points, double rate, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;
        display.directional();
        AtomicDouble theta = new AtomicDouble(0);

        return SpleefX.SCHEDULED_SERVICE.scheduleAtFixedRate(() -> {
            theta.addAndGet(rateDiv);

            for (int i = 0; i < points; i++) {
                // Calculate our starting point in a circle radius.
                double multiplier = (PII * ((double) i / points));
                double x = Math.cos(theta.get() + multiplier);
                double z = Math.sin(theta.get() + multiplier);

                // Calculate our direction of the spreading particles based on their angle.
                double angle = Math.atan2(z, x);
                double xDirection = Math.cos(angle);
                double zDirection = Math.sin(angle);

                display.offset(xDirection, 0, zDirection);
                display.spawn(x, 0, z);
            }
        }, 0L, 50, TimeUnit.MILLISECONDS);
    }

    protected static <T> T random(@NotNull List<T> t) {
        return Util.random(t);
    }

    protected static <T> T random(@NotNull T[] t) {
        return Util.random(t);
    }
}
