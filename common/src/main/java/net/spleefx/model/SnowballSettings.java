package net.spleefx.model;

import net.spleefx.util.Percentage;
import org.bukkit.Material;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SnowballSettings {

    public final boolean removeSnowballsGraduallyOnMelting = true;
    public final Percentage removalChance = new Percentage(50);
    public final Set<Material> thrownSnowballsRemoveHitBlocks = new HashSet<>(Collections.singletonList(Material.SNOW_BLOCK));
    public final int removedAmount = 1;
    public final boolean allowThrowing = true;

}
