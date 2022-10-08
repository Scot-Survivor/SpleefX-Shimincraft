package net.spleefx.util.watcher;//package net.spleefx.util.watcher;
//
//import net.spleefx.SpleefX;
//import org.bukkit.Bukkit;
//
//import java.io.File;
//import java.nio.file.Path;
//import java.util.function.Consumer;
//
//import static net.spleefx.util.watcher.FileWatcher.watch;
//
//public class WatchedFiles {
//
//    static {
//        watch(extension("spleef")).onChange(reloadExtension("spleef"));
//        watch(extension("splegg")).onChange(reloadExtension("splegg"));
//        watch(extension("bow_spleef")).onChange(reloadExtension("bow_spleef"));
//
//        watch(file("config.yml")).onChange(reload("config"));
//        watch(file("game-summary.yml")).onChange(reload("game-summary"));
//        watch(file("messages.yml")).onChange(reload("messages"));
//        watch(file("spectator-settings.yml")).onChange(reload("spec"));
//        watch(file("join-gui.yml")).onChange(reload("join"));
//        watch(file("statistics-gui.yml")).onChange(reload("stats"));
//    }
//
//    public static void watchFolder(Path path) {
//        FileWatcher.pollDirectory(path);
//    }
//
//    private static File file(String name) {
//        return new File(SpleefX.getPlugin().getDataFolder(), name.replace('/', File.separatorChar));
//    }
//
//    private static File extension(String name) {
//        return file("extensions/standard/" + name + ".yml");
//    }
//
//    private static Consumer<Path> reloadExtension(String name) {
//        return p -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spleefx extension reload " + name + " -silent");
//    }
//
//    private static Consumer<Path> reload(String key) {
//        return p -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spleefx reload " + key + " -silent");
//    }
//
//}
