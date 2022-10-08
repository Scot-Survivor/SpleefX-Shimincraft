package net.spleefx.listeners.interact;

import com.google.common.base.Preconditions;
import net.spleefx.model.Position;
import net.spleefx.util.game.BukkitEvents;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Projection {

    private Projection() {
    }

    public static @NotNull Builder make(@NotNull Player source,
                                        @NotNull Class<? extends Projectile> type) {
        return make(source, type, null);
    }

    public static @NotNull Builder make(@NotNull Player source,
                                        @NotNull Class<? extends Projectile> type,
                                        @Nullable Vector velocity) {
        Projectile projectile = source.launchProjectile(type, velocity);
        if (projectile instanceof Egg)
            BukkitEvents.nextEvent(PlayerEggThrowEvent.class, e -> e.getEgg().getUniqueId().equals(projectile.getUniqueId()))
                    .thenAccept(e -> e.setHatching(false));
        return new Builder(projectile);
    }

    public static @NotNull Builder track(@NotNull Projectile projectile) {
        return new Builder(projectile);
    }

    public static class Builder {

        private final Projectile projectile;

        public Builder(Projectile projectile) {
            this.projectile = projectile;
        }

        public Builder onLaunch(@NotNull BiConsumer<Player, Projectile> callback) {
            callback.accept((Player) projectile.getShooter(), projectile);
            return this;
        }

        public Builder onLaunch(@NotNull Consumer<Projectile> callback) {
            callback.accept(projectile);
            return this;
        }

        public Builder onHitEntity(@NotNull BiConsumer<Projectile, EntityDamageByEntityEvent> callback) {
            BukkitEvents.nextEvent(EntityDamageByEntityEvent.class, event -> {
                if (!(event.getDamager() instanceof Projectile)) return false;
                Projectile projectile = (Projectile) event.getDamager();
                return projectile.getUniqueId().equals(this.projectile.getUniqueId());
            }).thenAccept(e -> callback.accept(projectile, e));
            return this;
        }

        public Builder onLand(@NotNull BiConsumer<Projectile, Position> callback) {
            BukkitEvents.nextEvent(ProjectileHitEvent.class, event -> {
                Projectile projectile = event.getEntity();
                return projectile.getUniqueId().equals(this.projectile.getUniqueId());
            }).thenAccept(event -> {
                Position position = Position.at(projectile.getLocation());
                try {
                    if (event.getHitBlock() != null) {
                        Preconditions.checkState(event.getHitBlock().getType() != Material.AIR);
                        position = Position.at(event.getHitBlock());
                    }
                } catch (Throwable t) { // getHitBlock() is not present in old versions
                    BlockIterator iterator = new BlockIterator(event.getEntity().getWorld(), event.getEntity().getLocation().toVector(), event.getEntity().getVelocity().normalize(), 0.0D, 4);
                    Block hitBlock = null;
                    while (iterator.hasNext()) {
                        hitBlock = iterator.next();
                        if (hitBlock.getType() != Material.AIR) {
                            break;
                        }
                    }
                    if (hitBlock != null)
                        position = Position.at(hitBlock);
                }
                callback.accept(projectile, position);
            });
            return this;
        }

    }

}
