package net.spleefx.model.ability;

import lombok.Getter;
import lombok.ToString;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.model.VectorHolder;
import org.bukkit.Sound;

@Getter
@ToString
public class DoubleJumpOptions {

    private boolean enabled = true;
    private int defaultAmount = 5;
    private int cooldownBetween = 2;
    private Sound playSoundOnJump = PluginCompatibility.attempt(() -> Sound.valueOf("ENTITY_WITHER_SHOOT"), () -> Sound.valueOf("WITHER_SHOOT"));
    private DoubleJumpItems doubleJumpItems;
    private VectorHolder launchVelocity;
}
