package net.spleefx.listeners.interact;//package net.spleefx.listeners.interact;
//
//import net.spleefx.annotation.RegisteredListener;
//import net.spleefx.arena.player.MatchPlayer;
//import net.spleefx.model.Item;
//import net.spleefx.model.Position;
//import net.spleefx.util.Metadata;
//import org.bukkit.block.Block;
//import org.bukkit.entity.Projectile;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.event.entity.ProjectileHitEvent;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.util.BlockIterator;
//import org.bukkit.util.Vector;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//
//import static org.bukkit.Material.*;
//
//public class SimpleProjectile {
//
//    private static final Metadata<SimpleProjectile> DATA = Metadata.of("simple-projectile");
//
//    private static final Map<ItemHash, SimpleProjectile> PROJECTILES = new HashMap<>();
//
//    private final Class<? extends Projectile> projectileType;
//    private final Vector velocity;
//    private final BiConsumer<Projectile, MatchPlayer> onLaunch;
//    private final Consumer<Position> onLand;
//    private final Consumer<EntityDamageByEntityEvent> onHitEntity;
//
//    @RegisteredListener
//    public static class ProjectileListener implements Listener {
//
//        @EventHandler
//        public void onPlayerInteract(PlayerInteractEvent event) {
//            ItemStack itemStack = event.getItem();
//            if (itemStack == null) return;
//            ItemHash hash = new ItemHash(itemStack);
//            SimpleProjectile p = PROJECTILES.get(hash);
//            if (p == null) return; // not a custom projectile
//            Projectile projectile = event.getPlayer().launchProjectile(p.projectileType, p.velocity);
//            DATA.set(projectile, p);
//            p.onLaunch.accept(projectile, MatchPlayer.wrap(event.getPlayer()));
//        }
//
//        @EventHandler(ignoreCancelled = true)
//        public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
//            if (!(event.getDamager() instanceof Projectile)) return;
//            Projectile projectile = (Projectile) event.getDamager();
//            SimpleProjectile p = DATA.get(projectile);
//            if (p == null) return;
//            p.onHitEntity.accept(event);
//        }
//
//        @EventHandler(ignoreCancelled = true)
//        public void onProjectileHit(ProjectileHitEvent event) {
//            Projectile projectile = event.getEntity();
//            SimpleProjectile p = DATA.get(projectile);
//            if (p == null) return;
//            Position                      position = Position.at(projectile.getLocation());
//            ;
//            try {
//                if (event.getHitBlock() != null)
//                    position = Position.at(event.getHitBlock());
//            } catch (Throwable t) { // getHitBlock() is not present in old versions
//                BlockIterator iterator = new BlockIterator(event.getEntity().getWorld(), event.getEntity().getLocation().toVector(), event.getEntity().getVelocity().normalize(), 0.0D, 4);
//                Block hitBlock = null;
//                while (iterator.hasNext()) {
//                    hitBlock = iterator.next();
//                    if (hitBlock.getType() != AIR) {
//                        break;
//                    }
//                }
//                if (hitBlock != null)
//                    position = Position.at(hitBlock);
//            }
//            p.onLand.accept(position);
//        }
//    }
//
//    private static class ItemHash {
//
//        private final ItemStack item;
//
//        public ItemHash(ItemStack item) {
//            this.item = item;
//        }
//
//        @Override public boolean equals(Object o) {
//            if (this == o) return true;
//            if (!(o instanceof ItemHash)) return false;
//            ItemHash that = (ItemHash) o;
//            return item.isSimilar(that.item);
//        }
//
//        public int hashCode() {
//            return (item.hashCode() - item.getAmount()) / 31;
//        }
//    }
//
//    public SimpleProjectile(Item item,
//                            Class<? extends Projectile> projectileType,
//                            Vector velocity,
//                            BiConsumer<Projectile, MatchPlayer> onLaunch,
//                            Consumer<Position> onLand, Consumer<EntityDamageByEntityEvent> onHitEntity) {
//        this.projectileType = projectileType;
//        this.velocity = velocity;
//        this.onLaunch = onLaunch;
//        this.onLand = onLand;
//        this.onHitEntity = onHitEntity;
//        PROJECTILES.put(new ItemHash(item.createItem()), this);
//    }
//}
