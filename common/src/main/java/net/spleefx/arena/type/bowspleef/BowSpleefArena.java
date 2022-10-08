package net.spleefx.arena.type.bowspleef;

import net.spleefx.arena.ArenaType;
import net.spleefx.arena.MatchArena;
import net.spleefx.extension.MatchExtension;
import net.spleefx.extension.StandardExtensions;
import net.spleefx.json.GsonHook.AfterDeserialization;
import net.spleefx.model.Position;

public class BowSpleefArena extends MatchArena {

    public BowSpleefArena(String key, Position origin, ArenaType type, MatchExtension extension) {
        super(key, origin, type, StandardExtensions.BOW_SPLEEF);
    }

    public BowSpleefArena(String key, String displayName, Position origin, ArenaType type, MatchExtension extension) {
        super(key, displayName, origin, type, StandardExtensions.BOW_SPLEEF);
        postInit();
    }

    public BowSpleefArena(String key, String displayName, Position origin, ArenaType type) {
        super(key, displayName, origin, type, StandardExtensions.BOW_SPLEEF);
    }

    @AfterDeserialization(priority = 1)
    private void postInit() {
        extension = StandardExtensions.BOW_SPLEEF;
        engine = new BowSpleefEngine(engine);
    }

    @Override public BowSpleefEngine getEngine() {
        return (BowSpleefEngine) engine;
    }
}
