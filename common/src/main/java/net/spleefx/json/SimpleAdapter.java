package net.spleefx.json;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public abstract class SimpleAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    public static <R> R convert(Object src, Type type) {
        return SpleefXGson.MAIN.fromJson(SpleefXGson.MAIN.toJsonTree(src, type), type);
    }

}