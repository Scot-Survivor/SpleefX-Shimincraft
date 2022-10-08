package net.spleefx.model;

import com.cryptomorin.xseries.XPotion;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.json.GsonHook;
import net.spleefx.json.GsonHook.AfterDeserialization;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import static net.spleefx.util.Util.coerceAtLeast;
import static net.spleefx.util.Util.n;

@ToString
@EqualsAndHashCode
@GsonHook
@Getter
public final class Potion {

    private final XPotion type;
    private final int duration;
    private final int amplifier;

    private transient PotionEffect effect;

    private Potion(XPotion type, int duration, int amplifier) {
        this.type = type;
        this.duration = duration == -1 ? Integer.MAX_VALUE : duration;
        this.amplifier = coerceAtLeast(amplifier == -1 ? Integer.MAX_VALUE : amplifier, 0);
        cacheEffect();
    }

    @AfterDeserialization
    private void cacheEffect() {
        effect = type.buildPotionEffect(duration, amplifier);
    }

    public PotionEffect asPotionEffect() {
        return effect;
    }

    public void give(@NotNull LivingEntity entity) {
        entity.addPotionEffect(effect);
    }

    public void give(@NotNull MatchPlayer player) {
        give(player.player());
    }

    public static Potion of(@NotNull XPotion type, int duration, int amplifier) {
        return new Potion(n(type, "type is null!"), duration, amplifier);
    }

    public static Potion max(@NotNull XPotion type) {
        return new Potion(type, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static Potion neverExpires(@NotNull XPotion type, int amplifier) {
        return new Potion(type, Integer.MAX_VALUE, amplifier);
    }

}
