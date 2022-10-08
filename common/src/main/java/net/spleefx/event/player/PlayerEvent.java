package net.spleefx.event.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.spleefx.event.SpleefXEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public abstract class PlayerEvent extends SpleefXEvent {

    @NotNull
    protected final Player player;

}
