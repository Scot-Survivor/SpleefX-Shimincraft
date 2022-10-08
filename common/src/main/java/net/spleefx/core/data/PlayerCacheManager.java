package net.spleefx.core.data;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.spleefx.SpleefX;
import net.spleefx.arena.type.splegg.extension.SpleggUpgrade;
import net.spleefx.core.data.impl.SXPlayerProfile;
import net.spleefx.core.data.impl.SXPlayerProfile.ImmutableMapAdapter;
import net.spleefx.json.KeyedAdapters;
import net.spleefx.json.RestrictedAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

public interface PlayerCacheManager extends CacheLoader<UUID, PlayerProfile>, CacheWriter<UUID, PlayerProfile> {

    Executor SYNC = Runnable::run;

    Type GLOBAL_STATS_TYPE = new TypeToken<Map<GameStatType, Integer>>() {
    }.getType();

    Type UPGRADES_TYPE = new TypeToken<Set<SpleggUpgrade>>() {
    }.getType();

    Type KEY_TYPE = new TypeToken<Set<String>>() {
    }.getType();

    Type EXT_STATS_TYPE = new TypeToken<Map<String, Map<GameStatType, Integer>>>() {
    }.getType();

    /**
     * The GSON used for handling complex columns in tables
     */
    Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(GLOBAL_STATS_TYPE, new ImmutableMapAdapter<GameStatType, Integer>())
            .registerTypeAdapter(EXT_STATS_TYPE, new ImmutableMapAdapter<String, Map<GameStatType, Integer>>())
            .registerTypeAdapterFactory(RestrictedAdapter.wraps(KeyedAdapters.COLLECTION).restrict(UPGRADES_TYPE).build())
            .registerTypeAdapterFactory(new SXPlayerProfile.FastAdapter())
            .create();

    default void init(SpleefX plugin) {
    }

    /**
     * Caches all the data from the underlying resource. This method will be invoked asynchronously.
     *
     * @param cache Cache to input into
     */
    void cacheAll(LoadingCache<UUID, PlayerProfile> cache);

    /**
     * Saves this cache
     *
     * @param map Map to save
     */
    void writeAll(@NotNull Map<UUID, PlayerProfile> map, boolean modifiedOnly);

    /**
     * Whether is this cache manager safe to use in asynchronous context.
     *
     * @return Can be used in asynchronous context or not
     */
    default Executor executor() {
        return SpleefX.POOL;
    }

    default void shutdown(SpleefX plugin) {
    }

    /**
     * Represents a cache manager that must create a connection beforehand.
     */
    interface IConnectable {

        /**
         * Connects to the data source.
         */
        void connect();
    }

}