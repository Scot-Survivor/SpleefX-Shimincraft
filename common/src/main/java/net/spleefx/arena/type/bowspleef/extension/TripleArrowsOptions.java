package net.spleefx.arena.type.bowspleef.extension;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.event.block.Action;

import java.util.HashSet;
import java.util.Set;

public class TripleArrowsOptions {

    private static final Set<Material> MATERIALS = new HashSet<>();
    private static final Set<Action> CLICKS = ImmutableSet.of(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK);

    private boolean enabled = true;

    private int defaultAmount = 5;

    private Set<Action> actionsToTrigger = CLICKS;
    private Set<Material> requiredMaterials = new HashSet<>(MATERIALS);

    private int cooldown = 3;

    static {
        MATERIALS.add(Material.BOW);
        Material cross = Material.matchMaterial("CROSSBOW");
        if (cross != null) MATERIALS.add(cross);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getDefaultAmount() {
        return defaultAmount;
    }

    public Set<Action> getActionsToTrigger() {
        return actionsToTrigger;
    }

    public Set<Material> getRequiredMaterials() {
        return requiredMaterials;
    }

    public int getCooldown() {
        return cooldown;
    }
}
