package net.spleefx.arena.type.splegg.extension;

import lombok.Getter;
import org.bukkit.entity.*;

@Getter
public enum ProjectileType {

    ARROW(Arrow.class),
    SNOWBALL(Snowball.class),
    EGG(Egg.class),
    FIREBALL(Fireball.class);

    private final Class<? extends Projectile> type;

    ProjectileType(Class<? extends Projectile> type) {
        this.type = type;
    }
}
