package net.spleefx.powerup.api;

import net.spleefx.arena.MatchArena;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import org.jetbrains.annotations.NotNull;

public interface PowerupLifecycle {

    int getId();

    @NotNull MatchArena getArena();

    @NotNull ReloadedArenaEngine getEngine();

    @NotNull Powerup getPowerup();

    long getTimeout();

    void setTimeout(long value);

    void destroy();

    static @NotNull PowerupLifecycle failed(@NotNull MatchArena arena, @NotNull Powerup powerup) {
        return new PowerupLifecycle() {//@formatter:off
            @Override public int getId() { return -1; }
            @Override public @NotNull MatchArena getArena() { return arena; }
            @Override public @NotNull ReloadedArenaEngine getEngine() { return arena.getEngine(); }
            @Override public @NotNull Powerup getPowerup() { return powerup; }
            @Override public long getTimeout() { return 0; }
            @Override public void setTimeout(long value) { }
            @Override public void destroy() { }
        };//@formatter:on
    }

}
