package net.spleefx.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.spleefx.json.SpleefXGson;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A utility to create and build JSON text flexibly
 */
public class JsonBuilder {

    private final Map<String, Object> jsonMap = new LinkedHashMap<>();

    public JsonBuilder map(String key, Object value) {
        jsonMap.put(key, value == null ? JsonNull.INSTANCE : value);
        return this;
    }

    public JsonElement build() {
        return SpleefXGson.DEFAULT.toJsonTree(jsonMap);
    }

}
