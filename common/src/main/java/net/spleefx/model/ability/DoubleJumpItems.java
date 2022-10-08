package net.spleefx.model.ability;

import com.google.gson.annotations.Expose;
import net.spleefx.model.Item;

public class DoubleJumpItems {

    @Expose
    private boolean enabled;

    @Expose
    private int slot;

    @Expose
    private Item onAvailable;

    @Expose
    private Item onUnavailable;

    public DoubleJumpItems(boolean enabled, int slot, Item onAvailable, Item onUnavailable) {
        this.enabled = enabled;
        this.slot = slot;
        this.onAvailable = onAvailable;
        this.onUnavailable = onUnavailable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSlot() {
        return slot;
    }

    public Item getAvailable() {
        return onAvailable;
    }

    public Item getUnavailable() {
        return onUnavailable;
    }

}
