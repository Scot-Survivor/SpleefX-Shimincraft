package net.spleefx;

import lombok.Getter;
import lombok.SneakyThrows;
import net.spleefx.classpath.URLClassLoaderAccess;
import net.spleefx.core.data.StorageType;
import net.spleefx.core.dependency.DependencyManager;
import net.spleefx.util.FileManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.EnumSet;
import java.util.Objects;

import static net.spleefx.core.dependency.Dependency.*;

/**
 * Bootstrap plugin for LuckPerms running on Bukkit.
 */
public class SXBukkitBootstrap extends JavaPlugin {

    private final URLClassLoaderAccess classPathAppender;
    @Getter private DependencyManager dependencyManager;

    public SXBukkitBootstrap() {
        this.classPathAppender = URLClassLoaderAccess.create((URLClassLoader) getClassLoader());
    }

    private SpleefX app;

    // lifecycle

    @SneakyThrows @Override
    public void onLoad() {
        FileManager.forceMkdir(getDataFolder());
        download(
                "ProtocolLib",
                "https://github.com/dmulloy2/ProtocolLib/releases/download/4.7.0/ProtocolLib.jar"
        );
        dependencyManager = new DependencyManager(this, classPathAppender);
        dependencyManager.loadDependencies(EnumSet.of(
                CAFFEINE, OKIO, OKHTTP, GSON, XSERIES, HIKARI, PAPERLIB, SNAKEYAML
        ));
        dependencyManager.loadStorageDependencies(StorageType.valueOf(getConfig().getString("PlayerGameStatistics.StorageMethod", "JSON").toUpperCase()));
    }

    @SneakyThrows
    protected boolean download(String plugin, String url) {
        if (Bukkit.getPluginManager().getPlugin(plugin) == null) {
            getLogger().info(StringUtils.capitalize(plugin) + " plugin not found. Downloading...");
            File pluginJAR = new File(getDataFolder().getParentFile(), plugin + ".jar");
            pluginJAR.createNewFile();
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            connection.connect();

            ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
            FileOutputStream fos = new FileOutputStream(pluginJAR);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            Objects.requireNonNull(Bukkit.getPluginManager().loadPlugin(pluginJAR), "Plugin " + plugin + " not found.").onLoad();
            return true;
        }
        return false;
    }

    @Override
    public void onEnable() {
        try {
            app = new SpleefX(this);
            app.start();
        } catch (Throwable e) {
            getLogger().severe("Failed to start SpleefX");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            if (app != null)
                app.stop();
        } catch (Throwable e) {
            getLogger().severe("Failed to stop SpleefX");
            e.printStackTrace();
        }
    }

}
