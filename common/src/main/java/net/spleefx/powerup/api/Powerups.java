package net.spleefx.powerup.api;

import com.google.common.collect.ImmutableSet;
import lombok.SneakyThrows;
import net.spleefx.SpleefX;
import net.spleefx.config.json.MappedConfiguration;
import net.spleefx.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static net.spleefx.util.Util.n;

public final class Powerups {

    private static final Map<String, Powerup> POWERUPS = new ConcurrentHashMap<>();
    private static final Collection<Powerup> powerupsView = Collections.unmodifiableCollection(POWERUPS.values());
    private static final Collection<String> powerupsKeys = Collections.unmodifiableCollection(POWERUPS.keySet());

    @SneakyThrows
    public static void writeEmbeddedTaskFiles(@NotNull SpleefX plugin) {
        Class<?> fileList = Class.forName("net.spleefx.powerup.api._GeneratedPowerupsFilesList", true, Powerups.class.getClassLoader());
        Field list = fileList.getDeclaredField("FILES");
        ImmutableSet<String> filesList = (ImmutableSet<String>) list.get(null);
        for (String file : filesList) {
            plugin.getFileManager().createFile(file);
        }
    }

    public static void read(@NotNull SpleefX plugin) {
        POWERUPS.clear();
        read(plugin.getFileManager().createDirectory("power-ups"));
    }

    public static void read(File folder) {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) read(file);
            else {
                MappedConfiguration config = new MappedConfiguration(file);
                String id = config.get("PowerupType", String.class);
                if (id == null) {
                    SpleefX.logger().warning("Powerup file '" + file.getName() + "' does not define a PowerupType value! It should be the class name of the powerup.");
                    continue;
                }
                id = remapID(id);
                try {
                    Class<? extends Powerup> cl = Class.forName(id).asSubclass(Powerup.class);
                    Powerup powerup = config.getAs(cl);
                    powerup.reload = () -> {
                        config.load();
                        ReflectionUtil.merge(powerup, config.getAs(cl));
                    };
                    POWERUPS.put(powerup.getName(), powerup);
                    if (powerup instanceof Listener)
                        Bukkit.getPluginManager().registerEvents((Listener) powerup, SpleefX.getPlugin());
                } catch (ClassNotFoundException e) {
                    SpleefX.logger().warning("No such class: '" + id + "'");
                } catch (ClassCastException e) {
                    SpleefX.logger().warning("Class '" + id + "' does not extend " + Powerup.class + ".");
                } catch (Throwable e) {
                    SpleefX.logger().warning("Couldn't load power-up from file " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    private static String remapID(@NotNull String id) {
        return id.indexOf('.') == -1 ? "net.spleefx.powerup." + id : id;
    }

    public static Powerup getPowerup(@NotNull String name) {
        n(name, "name");
        return POWERUPS.get(name);
    }

    public static @UnmodifiableView Collection<Powerup> getPowerups() {
        return powerupsView;
    }

    public static @UnmodifiableView Collection<String> getKeys() {
        return powerupsKeys;
    }

}
