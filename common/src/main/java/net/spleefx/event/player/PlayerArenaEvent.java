package net.spleefx.event.player;

import lombok.Getter;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.extension.MatchExtension;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class PlayerArenaEvent extends PlayerEvent {

    @NotNull
    protected final MatchArena arena;

    @NotNull
    protected final MatchExtension extension;

    @NotNull
    protected final ReloadedArenaEngine engine;

    @NotNull
    protected final MatchPlayer arenaPlayer;

    public PlayerArenaEvent(Player player, @NotNull MatchArena arena) {
        super(player);
        this.arenaPlayer = MatchPlayer.wrap(player);
        this.arena = arena;
        this.extension = arena.getExtension();
        this.engine = arena.getEngine();
    }
}
