package net.spleefx.listeners.interact;//package net.spleefx.listeners.interact;
//
//import com.google.common.collect.ImmutableSet;
//import net.spleefx.model.Item;
//import org.bukkit.block.Block;
//import org.bukkit.entity.Player;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.inventory.ItemStack;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import static net.spleefx.util.Util.n;
//
//public class InteractableItem {
//
//    private static final Map<ItemStack, InteractableItem> ITEMS = new ConcurrentHashMap<>();
//
//    private final Item item;
//    private final boolean requireBlock, consumeOnUse, allowDropping, cancels;
//    private final ImmutableSet<InteractCallback> onRightClick, onLeftClick;
//
//    public static Builder builder() {
//        return new Builder();
//    }
//
//    public interface InteractCallback {
//
//        void handle(@NotNull Player p, Block block, @NotNull ItemStack item, @NotNull PlayerInteractEvent event);
//    }
//
//    public static class Builder {
//
//        private Item item;
//        private boolean requireBlock, consumeOnUse, cancel, allowDropping = true;
//        private ImmutableSet.Builder<InteractCallback> onRightClick = ImmutableSet.builder();
//        private ImmutableSet.Builder<InteractCallback> onLeftClick = ImmutableSet.builder();
//
//        public Builder item(@NotNull Item item) {
//            this.item = n(item, "item");
//            return this;
//        }
//
//        public Builder requireBlock() {
//            requireBlock = true;
//            return this;
//        }
//
//        public Builder consumeOnUse() {
//            consumeOnUse = true;
//            return this;
//        }
//
//        public Builder cancelsEvent() {
//            cancel = true;
//            return this;
//        }
//
//        public Builder allowDropping(boolean allowDropping) {
//            this.allowDropping = allowDropping;
//            return this;
//        }
//
//        public Builder rightClicked(@NotNull InteractCallback callback) {
//            onRightClick.add(n(callback, "callback"));
//            return this;
//        }
//
//        public Builder leftClicked(@NotNull InteractCallback callback) {
//            onLeftClick.add(n(callback, "callback"));
//            return this;
//        }
//
//        public InteractableItem build(){
//            return new InteractableItem(n(item, "You must set an item with Builder.item()!"),
//                    requireBlock,
//                    consumeOnUse,
//                    allowDropping,
//                    cancel,
//                    onRightClick.build(),
//                    onLeftClick.build()
//            );
//        }
//
//    }
//
//    public InteractableItem(Item item, boolean requireBlock, boolean consumeOnUse, boolean allowDropping, boolean cancels, ImmutableSet<InteractCallback> onRightClick, ImmutableSet<InteractCallback> onLeftClick) {
//        this.item = item;
//        this.requireBlock = requireBlock;
//        this.consumeOnUse = consumeOnUse;
//        this.allowDropping = allowDropping;
//        this.cancels = cancels;
//        this.onRightClick = onRightClick;
//        this.onLeftClick = onLeftClick;
//        ITEMS.put(item.createItem(), this);
//    }
//}