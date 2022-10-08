package net.spleefx.util;

import net.spleefx.SpleefX;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Metadata<V> {

    private final String key;

    private Metadata(String key) {
        this.key = key;
    }

    public @Nullable V get(@NotNull Metadatable metadatable) {
        try {
            return (V) metadatable.getMetadata(key).get(0).value();
        } catch (Throwable t) {
            return null;
        }
    }

    public @NotNull V require(@NotNull Metadatable metadatable) {
        try {
            return (V) metadatable.getMetadata(key).get(0).value();
        } catch (Throwable t) {
            throw new IllegalStateException("Entity " + metadatable + " does not have metadata key '" + key + "'");
        }
    }

    public @Nullable V remove(@NotNull Metadatable metadatable) {
        V v = get(metadatable);
        if (v != null)
            metadatable.removeMetadata(key, SpleefX.getPlugin());
        return v;
    }

    @Contract("null -> false")
    public boolean has(@Nullable Metadatable metadatable) {
        if (metadatable == null) return false;
        return metadatable.hasMetadata(key);
    }

    @Contract("_, null -> null; _, !null -> !null")
    public V set(@NotNull Metadatable metadatable, V value) {
        metadatable.setMetadata(key, new FixedMetadataValue(SpleefX.getPlugin(), value));
        return value;
    }

    public static <V> Metadata<V> of(@NotNull String key) {
        return new Metadata<>(SpleefX.getPlugin().getName().toLowerCase() + "." + key);
    }

}
