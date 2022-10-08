package net.spleefx.model;

import net.spleefx.SpleefX;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum ExtensionType {

    STANDARD("standard"),
    CUSTOM("custom");

    public static final File EXTENSIONS_FOLDER = new File(SpleefX.getPlugin().getDataFolder(), "extensions");
    public static final File CUSTOM_FOLDER = new File(EXTENSIONS_FOLDER, "custom");
    public static final File STANDARD_FOLDER = new File(EXTENSIONS_FOLDER, "standard");

    private static final Map<String, ExtensionType> TYPES = new HashMap<>();
    private final File file;

    ExtensionType(String folder) {
        this.file = new File(getExtensionsFolder(), folder);
    }

    private static File getExtensionsFolder() {
        return EXTENSIONS_FOLDER;
    }

    public File of(String key) {
        return new File(this == STANDARD ? STANDARD_FOLDER : CUSTOM_FOLDER, key + ".yml");
    }

    public static ExtensionType from(String value) {
        return TYPES.get(value.toUpperCase());
    }

    static {
        Arrays.stream(values()).forEach(c -> TYPES.put(c.name(), c));
    }

}