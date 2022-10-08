package net.spleefx.powerup.api;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import static net.spleefx.util.Util.n;

public final class PowerupTypeAdapterFactory implements TypeAdapterFactory {

    public static final PowerupTypeAdapterFactory INSTANCE = new PowerupTypeAdapterFactory();

    @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<?> rawType = type.getRawType();
        if (!Powerup.class.isAssignableFrom(rawType)) return null;
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        return new TypeAdapter<T>() {
            @Override public void write(JsonWriter out, T t) throws IOException {
                out.value(((Powerup) t).getName());
            }

            @Override public T read(JsonReader in) throws IOException {
                JsonElement element = Streams.parse(in);
                if (element instanceof JsonPrimitive) {
                    String name = element.getAsString();
                    return (T) n(Powerups.getPowerup(name), "Invalid power-up: " + name);
                } else {
                    return delegate.read(new JsonTreeReader(element));
                }
            }
        };
    }
}
