package net.spleefx.core.data.database.flat;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import net.spleefx.SpleefX;
import net.spleefx.core.data.PlayerCacheManager;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.core.data.impl.SXPlayerProfile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("UnstableApiUsage")
public class JsonBasedManager implements PlayerCacheManager {

    public static final JsonBasedManager JSON = new JsonBasedManager();

    private File directory;

    @Override public void init(SpleefX plugin) {
        directory = plugin.getFileManager().createDirectory("players-data");
    }

    @Override public void cacheAll(LoadingCache<UUID, PlayerProfile> cache) {
        File[] files = directory.listFiles();
        if (files == null) return;
        onEach(directory, dataFile -> {
            try {
                cache.put(
                        UUID.fromString(getBaseName(dataFile)),
                        GSON.fromJson(new String(Files.readAllBytes(dataFile.toPath())), SXPlayerProfile.class)
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, file -> com.google.common.io.Files.getFileExtension(file.getName()).equals("json"));
    }

    public static String getBaseName(File file) {
        checkNotNull(file);
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    private void onEach(File directory, Consumer<File> task, @Nullable Predicate<File> filter) {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                onEach(file, task, filter);
            } else {
                if (filter != null && !filter.test(file)) continue;
                task.accept(file);
            }
        }
    }

    @Override public void writeAll(@NotNull Map<UUID, PlayerProfile> map, boolean modifiedOnly) {
        for (Entry<UUID, PlayerProfile> entry : map.entrySet()) {
            try {
                File file = new File(directory, entry.getKey().toString() + ".json");
                file.createNewFile();
                Files.write(file.toPath(), GSON.toJson(entry.getValue()).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable @Override public PlayerProfile load(@NonNull UUID key) throws Exception {
        File file = new File(directory, key.toString() + ".json");
        if (file.exists())
            return GSON.fromJson(new String(Files.readAllBytes(file.toPath())), SXPlayerProfile.class);
        return PlayerProfile.blankProfile(key);
    }

    @Override public void write(@NonNull UUID key, @NonNull PlayerProfile value) {
        try {
            File file = new File(directory, key.toString() + ".json");
            file.createNewFile();
            Files.write(file.toPath(), GSON.toJson(value).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public void delete(@NonNull UUID key, @Nullable PlayerProfile value, @NonNull RemovalCause cause) {
        if (cause.wasEvicted()) return;
        new File(directory, key.toString() + ".json").delete();
    }
}