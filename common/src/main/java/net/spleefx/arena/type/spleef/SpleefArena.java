package net.spleefx.arena.type.spleef;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.spleefx.arena.ArenaType;
import net.spleefx.arena.MatchArena;
import net.spleefx.extension.MatchExtension;
import net.spleefx.extension.StandardExtensions;
import net.spleefx.json.GsonHook;
import net.spleefx.json.GsonHook.AfterDeserialization;
import net.spleefx.model.Position;

@Getter
@Setter
@Accessors(chain = true)
@GsonHook
public class SpleefArena extends MatchArena {

    private boolean melt;

    public SpleefArena(String key, Position origin, ArenaType type) {
        super(key, origin, type, StandardExtensions.SPLEEF);
    }

    public SpleefArena(String key, Position origin, ArenaType type, MatchExtension extension) {
        super(key, origin, type, StandardExtensions.SPLEEF);
    }

    public SpleefArena(String key, String displayName, Position origin, ArenaType type, MatchExtension extension) {
        super(key, displayName, origin, type, StandardExtensions.SPLEEF);
        postInit();
    }

    @AfterDeserialization(priority = 1)
    private void postInit() {
        extension = StandardExtensions.SPLEEF;
        engine = new SpleefEngine(engine);
    }

    @Override public SpleefEngine getEngine() {
        return (SpleefEngine) engine;
    }
}
