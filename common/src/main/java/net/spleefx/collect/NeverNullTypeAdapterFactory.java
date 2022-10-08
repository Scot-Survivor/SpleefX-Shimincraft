package net.spleefx.collect;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class NeverNullTypeAdapterFactory implements TypeAdapterFactory {

    public static final NeverNullTypeAdapterFactory INSTANCE = new NeverNullTypeAdapterFactory();

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> token) {
        Class<?> rawType = token.getRawType();
        if (!NeverNullCollection.class.isAssignableFrom(rawType)) return null;
        TypeAdapter<T> delegate = gson.getAdapter(token);
        if (NeverNullList.class.isAssignableFrom(rawType)) {
            return new TypeAdapter<T>() {
                @Override public void write(JsonWriter out, T t) throws IOException {
                    delegate.write(out, t);
                }

                @Override public T read(JsonReader in) throws IOException {
                    List<?> list = (List<?>) delegate.read(in);
                    return (T) new NeverNullList<>(list);
                }
            };
        } else {
            return new TypeAdapter<T>() {
                @Override public void write(JsonWriter out, T t) throws IOException {
                    delegate.write(out, t);
                }

                @Override public T read(JsonReader in) throws IOException {
                    Set<?> list = (Set<?>) delegate.read(in);
                    return (T) new NeverNullSet<>(list);
                }
            };
        }
    }
}
