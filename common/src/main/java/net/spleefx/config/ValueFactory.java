package net.spleefx.config;

import com.cryptomorin.xseries.XMaterial;
import net.spleefx.json.SpleefXGson;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A simple "adapter" to convert types from configuration nodes to POJOs.
 * <p>
 * Inspired by LuckPerms's "ConfigFactory".
 *
 * @param <V> The POJO type
 */
@SuppressWarnings({"unused"})
public interface ValueFactory<V> {

    ValueFactory<String> STRING = FileConfiguration::getString;
    ValueFactory<Integer> INTEGER = FileConfiguration::getInt;
    ValueFactory<Double> DOUBLE = FileConfiguration::getDouble;
    ValueFactory<Boolean> BOOLEAN = FileConfiguration::getBoolean;

    ValueFactory<? extends Enum<?>> ENUM = (config, path, def) -> {
        requireNonNull(def, "Default value");
        requireNonNull(path, "Path");
        return Enum.valueOf(
                def.getDeclaringClass(),
                requireNonNull(config.getString(path, def.name()), "config.getString(" + path + ")").toUpperCase()
        );
    };

    ValueFactory<List<XMaterial>> MATERIAL_LIST = (config, path, def) -> config.getStringList(path).stream().map(e -> XMaterial.matchXMaterial(e.toUpperCase()).orElseThrow(() -> new IllegalArgumentException("Invalid material: " + e)))
            .collect(Collectors.toList());

    ValueFactory<Map<String, String>> STRING_MAP = new MapFactory<>(s -> s, Object::toString);
    ValueFactory<Map<Integer, String>> INT_MAP = new MapFactory<>(Integer::parseInt, Object::toString);
    ValueFactory<List<Integer>> INT_LIST = (config, path, def) -> config.getIntegerList(requireNonNull(path, "Path"));

    V resolve(@NotNull FileConfiguration config, @Nullable String path, @Nullable V def);

    /* we love conjunctions. */
    //<editor-fold desc="Default options adapters" defaultstate="collapsed">
    static ConfigOption<String> stringKey(@NotNull String path, String def) {
        return new ConfigOption<>(path, def, STRING);
    }

    static ConfigOption<String> stringKey(@NotNull String path) {
        return stringKey(path, null);
    }

    static ConfigOption<Integer> integerKey(@NotNull String path, int def) {
        return new ConfigOption<>(path, def, INTEGER);
    }

    static ConfigOption<Integer> integerKey(@NotNull String path) {
        return integerKey(path, 0);
    }

    static ConfigOption<Double> doubleKey(@NotNull String path, double def) {
        return new ConfigOption<>(path, def, DOUBLE);
    }

    static ConfigOption<Double> doubleKey(@NotNull String path) {
        return new ConfigOption<>(path, 0D, DOUBLE);
    }

    static ConfigOption<Boolean> booleanKey(@NotNull String path, boolean def) {
        return new ConfigOption<>(path, def, BOOLEAN);
    }

    static ConfigOption<Boolean> booleanKey(@NotNull String path) {
        return new ConfigOption<>(path, false, BOOLEAN);
    }

    static <V extends Enum<V>> ConfigOption<V> enumKey(@NotNull String path, V def) {
        return new ConfigOption<>(path, def, ((ValueFactory<V>) ENUM));
    }

    static ConfigOption<List<Integer>> intList(@NotNull String path) {
        return new ConfigOption<>(path, Collections.emptyList(), INT_LIST);
    }

    static ConfigOption<List<XMaterial>> materialList(@NotNull String path) {
        return new ConfigOption<>(path, null, MATERIAL_LIST);
    }

    static ConfigOption<Map<String, String>> stringMap(@NotNull String path, Map<String, String> def) {
        return new ConfigOption<>(path, def, STRING_MAP);
    }

    static ConfigOption<Map<String, String>> stringMap(@NotNull String path) {
        return new ConfigOption<>(path, Collections.emptyMap(), STRING_MAP);
    }

    static ConfigOption<Map<Integer, String>> integerMap(@NotNull String path, Map<Integer, String> def) {
        return new ConfigOption<>(path, def, INT_MAP);
    }

    static ConfigOption<Map<Integer, String>> integerMap(@NotNull String path) {
        return integerMap(path, Collections.emptyMap());
    }

    static <V> ConfigOption<V> option(@NotNull String path, @NotNull V def, @NotNull ValueFactory<V> factory) {
        return new ConfigOption<>(path, def, factory);
    }

    static <V> ConfigOption<V> option(@NotNull V def, @NotNull ValueFactory<V> factory) {
        return new ConfigOption<>(null, def, factory);
    }

    static <V> ConfigOption<V> option(@NotNull ValueFactory<V> factory) {
        return new ConfigOption<>(null, null, factory);
    }

    static <T> ConfigOption<T> complex(@NotNull String path, @NotNull T def, Type type) {
        return new ConfigOption<>(path, def, new ObjectFactory<>(type));
    }

    static <T> ConfigOption<T> complex(@NotNull String path, @NotNull T def) {
        return new ConfigOption<>(path, def, new ObjectFactory<>(def.getClass()));
    }

    static <T> ConfigOption<T> complex(@NotNull String path, Type type) {
        return new ConfigOption<>(path, null, new ObjectFactory<>(type));
    }
    //</editor-fold>

    static <V> ConfigOption<V> notReloadable(ConfigOption<V> option) {
        option.reloadable = false;
        return option;
    }

    static <V> ConfigOption<V> redact(ConfigOption<V> option) {
        option.redacted = true;
        return option;
    }

    class MapFactory<K, V> implements ValueFactory<Map<K, V>> {

        private final Function<String, K> keyFunction;
        private final Function<Object, V> valueFunction;

        public MapFactory(Function<String, K> keyFunction, Function<Object, V> valueFunction) {
            this.keyFunction = keyFunction;
            this.valueFunction = valueFunction;
        }

        @Override public Map<K, V> resolve(@NotNull FileConfiguration config, String path, Map<K, V> def) {
            Map<K, V> map = new HashMap<>();
            ConfigurationSection section = config.getConfigurationSection(path);
            if (section == null) {
                return def;
            }

            for (String key : section.getKeys(false)) {
                map.put(keyFunction.apply(key), valueFunction.apply(section.get(key)));
            }
            return map;
        }
    }

    class ObjectFactory<T> implements ValueFactory<T> {

        private final Type type;

        public ObjectFactory(Type type) {
            this.type = type;
        }

        private Map<String, Object> convert(ConfigurationSection section) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Entry<String, Object> entry : section.getValues(true).entrySet()) {
                if (entry.getKey().contains(".")) continue;
                if (entry.getValue() instanceof ConfigurationSection) {
                    map.put(entry.getKey(), convert((ConfigurationSection) entry.getValue()));
                    continue;
                }
                map.put(entry.getKey(), entry.getValue());
            }
            return map;
        }

        @Override public T resolve(@NotNull FileConfiguration config, @Nullable String path, @Nullable T def) {
            if (path == null) return null;
            try {
                return SpleefXGson.from(convert(requireNonNull(config.getConfigurationSection(path))), type);
            } catch (NullPointerException e) {
                return def;
            }
        }
    }

}
