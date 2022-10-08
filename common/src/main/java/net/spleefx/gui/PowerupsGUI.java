package net.spleefx.gui;

import net.spleefx.arena.MatchArena;
import net.spleefx.powerup.api.Powerup;
import net.spleefx.powerup.api.Powerups;
import net.spleefx.util.menu.InventoryUI;
import net.spleefx.util.menu.ToggleButton;
import org.jetbrains.annotations.NotNull;

public final class PowerupsGUI extends InventoryUI {

    public PowerupsGUI(@NotNull MatchArena arena) {
        super("Power-ups for &5" + arena.getKey(), getAppropriateSize(Powerups.getPowerups().size()));
        int i = 0;
        for (Powerup powerup : Powerups.getPowerups()) {
            ToggleButton button = new ToggleButton(powerup.getPowerupIcon().asBuilder()
                    .name(powerup.getDisplayText())
                    .loreE()
                    .build());
            button.addTo(this,
                    i++,
                    arena.getPowerups().contains(powerup),
                    ToggleButton.sync(arena.getPowerups(), powerup)
            );
        }
    }
}
