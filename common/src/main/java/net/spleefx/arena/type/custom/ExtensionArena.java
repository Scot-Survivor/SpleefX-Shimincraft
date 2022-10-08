package net.spleefx.arena.type.custom;

import com.google.gson.annotations.JsonAdapter;
import net.spleefx.arena.ArenaType;
import net.spleefx.arena.MatchArena;
import net.spleefx.extension.MatchExtension;
import net.spleefx.json.GsonHook;
import net.spleefx.json.GsonHook.AfterDeserialization;
import net.spleefx.json.KeyedAdapters.ToStringAdapter;
import net.spleefx.model.Position;

@GsonHook
public class ExtensionArena extends MatchArena {

    @JsonAdapter(ToStringAdapter.class)
    protected MatchExtension extension;

    public ExtensionArena(String key, Position origin, ArenaType type, MatchExtension extension) {
        super(key, origin, type, extension);
        this.extension = extension;
        postInit();
    }

    public ExtensionArena(String key, String displayName, Position origin, ArenaType type, MatchExtension extension) {
        super(key, displayName, origin, type, extension);
        this.extension = extension;
        postInit();
    }

    @AfterDeserialization(priority = -1)
    private void postInit() {
        super.extension = extension;
    }

    @AfterDeserialization(priority = 1)
    private void postInit2() {
        engine.setExtension(extension);
    }

    @Override public MatchExtension getExtension() {
        return extension;
    }
}
