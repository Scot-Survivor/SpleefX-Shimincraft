package net.spleefx.util.menu;

import com.cryptomorin.xseries.XMaterial;
import net.spleefx.model.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static net.spleefx.util.Util.n;

public class BooleanButton {

    private static final Item ENABLED = Item.builder()
            .type(XMaterial.REDSTONE_TORCH)
            .name("&cClick to disable")
            .build();

    private static final Item DISABLED = Item.builder()
            .type(XMaterial.LEVER)
            .name("&aClick to enable")
            .build();

    private final Item item;
    private final Consumer<Boolean> valueChange;

    public BooleanButton(Item item) {
        this(item, e -> {
        });
    }

    public BooleanButton(Item item, Consumer<Boolean> valueChange) {
        this.item = item;
        this.valueChange = valueChange;
    }

    public void addTo(@NotNull InventoryUI menu, int slot, @NotNull BooleanSupplier initialValue) {
        addTo(menu, slot, n(initialValue, "initialValue is null!").getAsBoolean(), valueChange);
    }

    public void addTo(@NotNull InventoryUI menu, int slot, @NotNull BooleanSupplier initialValue, Consumer<Boolean> valueChange) {
        addTo(menu, slot, n(initialValue, "initialValue is null!").getAsBoolean(), valueChange);
    }

    public void addTo(@NotNull InventoryUI menu, int slot, boolean initialValue, Consumer<Boolean> valueChange) {
        AtomicBoolean value = new AtomicBoolean(initialValue);
        int toggleSlot = slot + 9;
        Button toggle = Button.builder()
                .cancelClick()
                .item(getItem(value))
                .handle(e -> {
                    value.set(!value.get());
                    valueChange.accept(value.get());
                    e.getInventory().setItem(toggleSlot, getItem(value).createItem());
                })
                .build();
        toggle.addTo(menu, toggleSlot);

        Button description = Button.builder().cancelClick().item(item).build();
        description.addTo(menu, slot);
    }

    private static Item getItem(AtomicBoolean atomicBoolean) {
        return atomicBoolean.get() ? ENABLED : DISABLED;
    }

    public static <T> Consumer<Boolean> sync(@NotNull Collection<T> collection, T element) {
        return b -> {
            if (b) collection.add(element);
            else collection.remove(element);
        };
    }

}
