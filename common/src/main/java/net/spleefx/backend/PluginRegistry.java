package net.spleefx.backend;

import net.spleefx.SpleefX;
import org.bukkit.plugin.java.JavaPlugin;

public interface PluginRegistry {

    void registerCommands(SpleefX app, JavaPlugin plugin);

    void registerListeners(SpleefX app, JavaPlugin plugin);

}