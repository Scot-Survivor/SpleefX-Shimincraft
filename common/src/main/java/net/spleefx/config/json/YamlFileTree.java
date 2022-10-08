package net.spleefx.config.json;

import com.google.common.collect.ForwardingMap;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import net.spleefx.json.SpleefXGson;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class YamlFileTree<T> extends ForwardingMap<String, T> {

    private final File directory;

    private final Map<String, T> data = new LinkedHashMap<>();

    private final Map<String, File> files = new LinkedHashMap<>();

    private final Type type; // = new TypeToken<T>() {}.getType()

    private final Map<String, Type> subTypes = new HashMap<>();

    private BiConsumer<String, T> onLoad = (a, b) -> {
    };

    public YamlFileTree(File directory, Type type) {
        this.directory = directory;
        this.type = type;
    }

    public YamlFileTree<T> registerSubType(String name, Type type) {
        subTypes.put(name, type);
        return this;
    }

    public YamlFileTree<T> onLoad(BiConsumer<String, T> onLoad) {
        this.onLoad = onLoad;
        return this;
    }

    private void scan(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.getName().startsWith("-")) continue;
            if (file.isDirectory()) {
                scan(file);
                continue;
            }
            if (!file.getName().endsWith(".yml")) continue;
            parse(file);
        }
    }

    public YamlFileTree<T> scan() {
        scan(directory);
        return this;
    }

    public T parse(File file) {
        JsonObject content = new MappedConfiguration(file).getData();
        String key = Files.getNameWithoutExtension(file.getName());
        T value = SpleefXGson.MAIN.fromJson(content, subTypes.getOrDefault(key, type));
        files.put(key, file);
        if (onLoad != null && data.put(key, value) == null)
            onLoad.accept(key, value);
        return value;
    }

    public T getOrParse(String key, File file) {
        T value = get(key);
        return value == null ? parse(file) : value;
    }

    public T parseSneaky(File file) {
        JsonObject content = new MappedConfiguration(file).getData();
        String key = Files.getNameWithoutExtension(file.getName());
        return SpleefXGson.MAIN.fromJson(content, subTypes.getOrDefault(key, type));
    }

    public void save() {
        for (Entry<String, T> data : entrySet()) {
            JsonObject o = (JsonObject) SpleefXGson.MAIN.toJsonTree(data.getValue());
            MappedConfiguration config = new MappedConfiguration(files.get(data.getKey()));
            config.setData(o);
            config.save();
        }
    }

    @Override protected @NotNull Map<String, T> delegate() {
        return data;
    }
}