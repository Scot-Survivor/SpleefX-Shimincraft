package net.spleefx.util.menu;

import net.spleefx.model.Item;
import net.spleefx.util.Placeholders.ToggleEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static net.spleefx.util.Util.n;

public class ToggleButton {

    private final Item item;
    private final Consumer<Boolean> valueChange;

    public ToggleButton(Item item) {
        this(item, e -> {
        });
    }

    public ToggleButton(Item item, Consumer<Boolean> valueChange) {
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
        Button description = Button.builder().cancelClick().item(item.withPlaceholders(new ToggleEntry(value.get())))
                .handle(e -> {
                    value.set(!value.get());
                    valueChange.accept(value.get());
                    e.getInventory().setItem(slot, item.withPlaceholders(new ToggleEntry(value.get())));
                }).build();
        description.addTo(menu, slot);
    }

    public static <T> Consumer<Boolean> sync(@NotNull Collection<T> collection, T element) {
        return b -> {
            if (b) collection.add(element);
            else collection.remove(element);
        };
    }

}
