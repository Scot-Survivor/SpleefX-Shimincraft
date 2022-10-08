package net.spleefx.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.spleefx.SpleefX;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class PotionAdapter extends TypeAdapter<PotionEffect> {

    public static final PotionAdapter INSTANCE = new PotionAdapter();

    @Override public void write(JsonWriter out, PotionEffect potion) throws IOException {
        out.beginArray();
        out.value(potion.getType().getName());
        out.value(potion.getDuration());
        out.value(potion.getAmplifier());
        out.endArray();
    }

    @Override public PotionEffect read(JsonReader in) throws IOException {
        JsonElement element = Streams.parse(in);
        if (element.isJsonPrimitive()) {
            String[] data = element.getAsString().split(":");
            PotionEffectType type = getPotionType(data[0]);
            return new PotionEffect(type, Integer.parseInt(data[1]), Integer.parseInt(data[2]));
        }
        JsonArray array = element.getAsJsonArray();
        return new PotionEffect(getPotionType(array.get(0).getAsString()), array.get(1).getAsInt(), array.get(2).getAsInt());
    }

    private static PotionEffectType getPotionType(@NotNull String name) {
        PotionEffectType type = PotionEffectType.getByName(name);
        if (type == null) {
            SpleefX.logger().warning("Unrecognizable potion effect: " + name);
            type = PotionEffectType.FIRE_RESISTANCE;
        }
        return type;
    }
}
