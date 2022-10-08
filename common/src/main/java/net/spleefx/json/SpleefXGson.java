package net.spleefx.json;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.spleefx.SpleefX;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.type.bowspleef.BowSpleefArena;
import net.spleefx.arena.type.custom.ExtensionArena;
import net.spleefx.arena.type.spleef.SpleefArena;
import net.spleefx.arena.type.splegg.SpleggArena;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.config.json.XSeriesTypeAdapterFactory;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import net.spleefx.model.PotionAdapter;
import net.spleefx.powerup.api.PowerupTypeAdapterFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SpleefXGson {

    /* Type Tokens */
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    private static final JsonParser PARSER = new JsonParser();

    /* Enum adapters */

    private static final TypeAdapter<TimeUnit> TIME_UNIT = new EnumAdapter<>(TimeUnit.class).from(e -> {
        e = e.toUpperCase();
        return TimeUnit.valueOf(e.endsWith("S") ? e : e + "S");
    }).nullSafe();

    private static final TypeAdapter<Material> MATERIAL = new EnumAdapter<>(Material.class)
            .from(Material::matchMaterial).nullSafe();

    private static final TypeAdapter<XMaterial> XMATERIAL = new EnumAdapter<>(XMaterial.class)
            .from(string -> XMaterial.matchXMaterial(string).orElse(null)).nullSafe();

    public static final RuntimeTypeAdapterFactory<MatchArena> ARENAS =
            RuntimeTypeAdapterFactory.of(MatchArena.class, "ModeType")
                    .registerSubtype(SpleggArena.class)
                    .registerSubtype(SpleefArena.class)
                    .registerSubtype(BowSpleefArena.class)
                    .registerSubtype(ExtensionArena.class);

    public static final Gson DEFAULT = new GsonBuilder().disableHtmlEscaping().create();

    private static final JsonDeserializer<MatchExtension> EXTENSION_ADAPTER = (json, typeOfT, context) -> {
        if (json.isJsonPrimitive()) {
            return Extensions.getByKey(json.getAsString());
        }
        MatchExtension mode = null;
        JsonObject o = json.getAsJsonObject();
        try {
            mode = helper().fromJson(json, typeOfT);
            Extensions.registerExtension(mode);
        } catch (Exception e) {
            SpleefX.logger().severe("Failed to load extension \"" + o.get("Key").getAsString() + "\".");
            SpleefX.logger().severe("If the error below is a JsonSyntaxException, then it's most likely a problem in your JSON syntax. If you're unable to spot the problem, or if it's not a JsonSyntaxException,");
            SpleefX.logger().severe("then drop by the Discord (on SpigotMC page) and ask for support. MAKE SURE TO INCLUDE THE ERROR BELOW!");
            e.printStackTrace();
            PluginCompatibility.DISABLE.set(true);
            Bukkit.getPluginManager().disablePlugin(SpleefX.getPlugin());
        }
        return mode;
    };

    public static final Gson MAIN;

    static {
        GsonBuilder main = new GsonBuilder()
                .disableHtmlEscaping()
                // enums
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapterFactory(new HooksTypeAdapterFactory())
                .registerTypeAdapterFactory(XSeriesTypeAdapterFactory.getInstance())
                .registerTypeAdapterFactory(PowerupTypeAdapterFactory.INSTANCE)
                .registerTypeAdapter(TimeUnit.class, TIME_UNIT)
                .registerTypeAdapter(Material.class, MATERIAL)
                .registerTypeAdapter(XMaterial.class, XMATERIAL)

                // type factories
                .registerTypeAdapterFactory(ARENAS)

                // other custom types
                .registerTypeAdapter(EnchantmentsAdapter.TYPE, EnchantmentsAdapter.INSTANCE)
                .registerTypeAdapter(PotionEffect.class, PotionAdapter.INSTANCE)

                .setFieldNamingStrategy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                        return fieldAttributes.getDeclaredClass() == Runnable.class;
                    }

                    @Override public boolean shouldSkipClass(Class<?> aClass) {
                        return false;
                    }
                })
                .serializeNulls()
                .setPrettyPrinting();
        HELPER = main.create();
        MAIN = main.registerTypeAdapter(MatchExtension.class, EXTENSION_ADAPTER).create();
    }

    private static final Gson HELPER;

    private static Gson helper() {
        return HELPER;
    }

    public static Map<String, Object> toMap(Object obj) {
        return MAIN.fromJson(MAIN.toJsonTree(obj), MAP_TYPE);
    }

    public static JsonElement getElement(@NotNull String string) {
        return PARSER.parse(string);
    }

    public static <T> T from(Object src, Type type) {
        return MAIN.fromJson(MAIN.toJsonTree(src), type);
    }

}
