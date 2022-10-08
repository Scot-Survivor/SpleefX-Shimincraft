package net.spleefx.util.menu;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Preconditions;
import net.spleefx.model.Item;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import static net.spleefx.util.Util.coerce;
import static net.spleefx.util.Util.n;

public class NumberButton {

    private static final Item INCREASE = Item.builder()
            .type(XMaterial.LIME_STAINED_GLASS_PANE)
            .name("&aIncrease")
            .lore("", "&eLeft click &7-> &a+1", "&eRight click &7-> &a+5", "&eShift click &7-> &a+10")
            .build();

    private static final Item DECREASE = Item.builder()
            .type(XMaterial.RED_STAINED_GLASS_PANE)
            .name("&cDecrease")
            .lore("", "&eLeft click &7-> &c-1", "&eRight click &7-> &c-5", "&eShift click &7-> &c-10")
            .build();

    private final IntConsumer valueChange;
    private final Item item;
    private final int minimum, maximum;

    private NumberButton(IntConsumer valueChange, Item item, int minimum, int maximum) {
        this.valueChange = valueChange;
        this.item = item;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public void addTo(@NotNull InventoryUI menu, int slot, int initialValue) {
        addTo(menu, slot, initialValue, valueChange);
    }

    public void addTo(@NotNull InventoryUI menu, int slot, @NotNull IntSupplier initialValue) {
        addTo(menu, slot, initialValue, valueChange);
    }

    public void addTo(@NotNull InventoryUI menu, int slot, @NotNull IntSupplier initialValue, @NotNull IntConsumer valueChange) {
        addTo(menu, slot, n(initialValue, "initialValue is null!").getAsInt(), valueChange);
    }

    public void addTo(@NotNull InventoryUI menu, int slot, int initialValue, @NotNull IntConsumer valueChange) {
        AtomicInteger value = new AtomicInteger(initialValue);
        Button decrement = Button.builder()
                .item(DECREASE)
                .handle(e -> {
                    int delta = e.isShiftClick() ? 10 : e.isRightClick() ? 5 : 1;
                    valueChange.accept(value.updateAndGet(m -> coerce(m - delta, minimum, maximum)));
                })
                .and(e -> e.getInventory().setItem(slot, item.withPlaceholdersAndAmount(value.get(), value.get())))
                .cancelClick()
                .build();
        Button increment = Button.builder()
                .item(INCREASE)
                .handle(e -> {
                    int delta = e.isShiftClick() ? 10 : e.isRightClick() ? 5 : 1;
                    valueChange.accept(value.updateAndGet(m -> coerce(m + delta, minimum, maximum)));
                })
                .and(e -> e.getInventory().setItem(slot, item.withPlaceholdersAndAmount(value.get(), value.get())))
                .cancelClick()
                .build();
        increment.addTo(menu, slot - 9);
        decrement.addTo(menu, slot + 9);

        Button description = Button.builder().item(item.withPlaceholdersAndAmount(value.get(), value.get())).cancelClick().build();
        description.addTo(menu, slot);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private IntConsumer valueChange = i -> {
        };
        private int minimum = 1, maximum = Integer.MAX_VALUE;
        private Item item;

        private Builder() {
        }

        public Builder minimum(int min) {
            this.minimum = min;
            return this;
        }

        public Builder maximum(int maximum) {
            this.maximum = maximum;
            return this;
        }

        public Builder whenValueChanges(@NotNull IntConsumer task) {
            this.valueChange = n(task, "task is null!");
            return this;
        }

        public Builder item(@NotNull Item item) {
            this.item = n(item, "item is null!");
            return this;
        }

        public NumberButton build() {
            Preconditions.checkArgument(minimum < maximum, "minimum (" + minimum + ") is not less than the maximum (" + maximum + ")");
            return new NumberButton(valueChange, item, minimum, maximum);
        }

    }

}
