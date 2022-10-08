package net.spleefx.extension;

import net.spleefx.arena.type.bowspleef.extension.BowSpleefExtension;
import net.spleefx.arena.type.spleef.extension.SpleefExtension;
import net.spleefx.arena.type.splegg.extension.SpleggExtension;

public final class StandardExtensions {

    public static final SpleggExtension SPLEGG = (SpleggExtension) Extensions.getByKey("splegg");
    public static final SpleefExtension SPLEEF = (SpleefExtension) Extensions.getByKey("spleef");
    public static final BowSpleefExtension BOW_SPLEEF = (BowSpleefExtension) Extensions.getByKey("bow_spleef");

    private StandardExtensions() {
        throw new UnsupportedOperationException("Cannot instantiate " + getClass().getName() + "!");
    }
}
