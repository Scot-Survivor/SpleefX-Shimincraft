package net.spleefx.arena.type.splegg;

import lombok.Getter;
import lombok.Setter;
import net.spleefx.arena.ArenaType;
import net.spleefx.arena.MatchArena;
import net.spleefx.extension.MatchExtension;
import net.spleefx.json.GsonHook.AfterDeserialization;
import net.spleefx.model.Position;
import org.jetbrains.annotations.Nullable;

import static net.spleefx.extension.StandardExtensions.SPLEGG;

@Setter
@Getter
public class SpleggArena extends MatchArena {

    public SpleggArena(String key, Position origin, ArenaType type, @Nullable MatchExtension extension) {
        super(key, origin, type, SPLEGG);
    }

    public SpleggArena(String key, String displayName, Position origin, ArenaType type, MatchExtension extension) {
        super(key, displayName, origin, type, SPLEGG);
        postInit();
    }

    @AfterDeserialization(priority = 1)
    private void postInit() {
        extension = SPLEGG;
        engine = new SpleggEngine(engine);
    }

    @Override
    public SpleggEngine getEngine() {
        return (SpleggEngine) engine;
    }

}
