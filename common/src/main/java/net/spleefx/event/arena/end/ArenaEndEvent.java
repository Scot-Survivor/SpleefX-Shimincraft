package net.spleefx.event.arena.end;

import net.spleefx.arena.MatchArena;
import net.spleefx.event.arena.ArenaEvent;

/**
 * Fired when a game ends
 */
public abstract class ArenaEndEvent extends ArenaEvent {

    private final boolean forcibly;

    public ArenaEndEvent(MatchArena arena, boolean forcibly) {
        super(arena);
        this.forcibly = forcibly;
    }

    public boolean isForcibly() {
        return forcibly;
    }
}
