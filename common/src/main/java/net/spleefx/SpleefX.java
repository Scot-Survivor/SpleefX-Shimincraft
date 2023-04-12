package net.spleefx;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketListener;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.SneakyThrows;
import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.summary.template.GameSummaryTemplate;
import net.spleefx.arena.type.bowspleef.extension.BowSpleefExtension;
import net.spleefx.arena.type.spleef.extension.SpleefExtension;
import net.spleefx.arena.type.splegg.extension.SpleggExtension;
import net.spleefx.backend.PluginRegistry;
import net.spleefx.backend.Schedulers;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.config.TeamsConfig;
import net.spleefx.config.json.MappedConfiguration;
import net.spleefx.config.json.YamlFileTree;
import net.spleefx.config.json.select.ConfigOpt;
import net.spleefx.config.json.select.ConfigurationPack;
import net.spleefx.config.json.select.SelectableConfiguration;
import net.spleefx.core.command.CommandHandler;
import net.spleefx.core.data.PlayerRepository;
import net.spleefx.core.data.SpleefXPAPI;
import net.spleefx.core.data.menu.StatisticsConfig;
import net.spleefx.core.scoreboard.sidebar.ScoreboardThread;
import net.spleefx.event.PlayerArenaInteractionListener;
import net.spleefx.event.listen.EventListener;
import net.spleefx.event.trigger.StandardCallbacks;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import net.spleefx.gui.JoinGUI;
import net.spleefx.hook.bstats.Metrics;
import net.spleefx.hook.bstats.Metrics.SimplePie;
import net.spleefx.hook.luckperms.LuckPermsHook;
import net.spleefx.hook.vault.VaultHandler;
import net.spleefx.hook.worldedit.SchematicManager;
import net.spleefx.json.SpleefXGson;
import net.spleefx.listeners.ArenaListener;
import net.spleefx.model.ExtensionType;
import net.spleefx.powerup.api.Powerups;
import net.spleefx.spectate.SpectatorListener.PickupListener;
import net.spleefx.spectate.SpectatorSettings;
import net.spleefx.util.FileManager;
import net.spleefx.util.message.message.MessageManager;
import net.spleefx.util.plugin.Protocol;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.io.File.separator;
import static net.spleefx.config.json.select.SelectableConfiguration.of;
import static org.bukkit.Bukkit.getServer;

@Getter
public final class SpleefX extends Schedulers {

    private static final AtomicReference<SpleefX> APP = new AtomicReference<>();
    private static final AtomicReference<JavaPlugin> PLUGIN = new AtomicReference<>();

    private final CommandHandler commandHandler;
    private final JavaPlugin plugin;
    private final File arenasFolder;
    private final FileManager fileManager;
    private final SelectableConfiguration statsFile;
    private final SelectableConfiguration joinGuiFile;
    @ConfigOpt("spectator-settings.yml") private final SpectatorSettings spectatorMenu;
    private final ScoreboardThread scoreboardThread;
    private final MessageManager messageManager;
    private final ConfigurationPack configurationPack;
    private final YamlFileTree<MatchExtension> extensions;
    private final Object worldEdit;
    private final VaultHandler vaultHandler;
    private final MappedConfiguration arenasConfig;
    private final SXBukkitBootstrap bootstrap;
    private boolean arenasError = false;

    public SpleefX(final SXBukkitBootstrap bootstrap) throws Throwable {
        APP.set(this);
        PLUGIN.set(bootstrap);
        this.bootstrap = bootstrap;
        this.plugin = bootstrap;
        this.fileManager = new FileManager(bootstrap);
        Stream.of("extensions/standard/spleef.yml",
                "arenas/arenas.yml",
                "extensions/standard/splegg.yml",
                "extensions/standard/bow_spleef.yml",
                "spectator-settings.yml",
                "extensions/custom/-example-mode.yml").forEach(fileManager::createFile);
        this.fileManager.createFile("config.yml");
        SpleefXConfig.load(true);
        TeamsConfig.load(true);
        PluginCompatibility.load();
        this.commandHandler = new CommandHandler(this);
        this.messageManager = new MessageManager(this);
        this.arenasFolder = fileManager.createDirectory("arenas");
        this.statsFile = of(fileManager.createFile("gui" + separator + "statistics-gui.yml")).register(StatisticsConfig.class).associate();
        this.joinGuiFile = of(fileManager.createFile("gui" + separator + "join-gui.yml")).register(JoinGUI.MenuSettings.class).associate();
        this.spectatorMenu = new SpectatorSettings();
        this.scoreboardThread = new ScoreboardThread();
        this.scoreboardThread.setTicks(SpleefXConfig.SCOREBOARD_UPDATE_INTERVAL.get());
        this.scoreboardThread.start();
        this.configurationPack = new ConfigurationPack(this, bootstrap.getDataFolder(), SpleefXGson.MAIN);
        this.extensions = new YamlFileTree<MatchExtension>(ExtensionType.EXTENSIONS_FOLDER, MatchExtension.class)
                .registerSubType("spleef", SpleefExtension.class)
                .registerSubType("bow_spleef", BowSpleefExtension.class)
                .registerSubType("splegg", SpleggExtension.class)
                .onLoad((k, e) -> Extensions.registerExtension(e));
        this.messageManager.load(false);
        Extensions.registerAll(extensions.scan());
        Powerups.writeEmbeddedTaskFiles(this);
        Powerups.read(this);
        PaperLib.suggestPaper(bootstrap);
        this.worldEdit = getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            throw new IllegalStateException("WorldEdit is missing. Please download WorldEdit (or any of its forks) for the plugin to function.");
        }
        this.arenasConfig = new MappedConfiguration(new File(arenasFolder, "arenas.yml"));
        this.vaultHandler = SpleefXConfig.VAULT_EXISTS ? new VaultHandler() : null;
        this.configurationPack.register();
        registerCommands();
        addHooks();
        setupMetrics();
        finishStartUp();
        nextTick(20, this::loadArenas);
    }

    public void start() throws Throwable {

    }

    public void stop() throws Throwable {
        saveEverything();
    }

    public MappedConfiguration gameSummaryFile;
    public GameSummaryTemplate gameSummary;

    public static CompletableFuture<Void> nextTick(Runnable task) {
        return nextTick(1, task);
    }

    public WorldEditPlugin getWorldEdit() {
        return (WorldEditPlugin) worldEdit;
    }

    public static CompletableFuture<Void> nextTick(int delay, Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskLater(PLUGIN.get(), () -> {
            try {
                task.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            future.complete(null);
        }, delay);
        return future;
    }

    public void sync(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    private void loadArenas() {
        try {
            Arenas.registerAll(arenasConfig.get("Arenas", new TypeToken<Map<String, MatchArena>>() {}.getType()));
            int size = Arenas.size();
            info("Successfully loaded " + size + " arena" + (size == 1 ? "" : "s") + ".");
        } catch (Throwable t) {
            arenasError = true;
            throw t;
        }
    }

    private void addHooks() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            saveArenas();
            PlayerRepository.REPOSITORY.save();
        }, 24000, 24000); // 20 minutes

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            logger().info("Found PlaceholderAPI. Registering expansion");
            new SpleefXPAPI(plugin).register();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            logger().info("Found LuckPerms. Registering contexts");
            LuckPermsHook.register(this);
        }
        gameSummaryFile = new MappedConfiguration(fileManager.createFile("game-summary.yml"));
        gameSummary = gameSummaryFile.getAs(GameSummaryTemplate.class);
    }

    private void setupMetrics() {
        getLogger().info("Establishing connection to bstats.org");
        Metrics metrics = new Metrics(plugin, 7694);
        metrics.addCustomChart(new SimplePie("storage_type", () -> SpleefXConfig.STORAGE_METHOD.get().getName()));
    }

    @SneakyThrows
    private void registerCommands() {
        PluginRegistry registry = (PluginRegistry) Class.forName("net.spleefx.backend._GeneratedPluginRegistry").newInstance();
        try {
            registry.registerCommands(this, plugin);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        getCommandHandler().registerExtensionCommands();
        registry.registerListeners(this, plugin);
        if (Protocol.PROTOCOL != 8)
            addListener(new PickupListener());
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
            addListener(new ArenaListener.WGListener());
        else
            addListener(new ArenaListener.BlockBreakListener());
    }

    private void finishStartUp() {
        try {
            SpleefX.logger().info("Using storage type " + SpleefXConfig.STORAGE_METHOD.get().getName() + ".");
            SpleefXConfig.STORAGE_METHOD.get().delegate();
            PlayerRepository.REPOSITORY.init(this);
            if (SpleefXConfig.LEADERBOARDS.get())
                PlayerRepository.REPOSITORY.cacheAll();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void saveEverything() throws IOException {
        //noinspection CatchMayIgnoreException
        try {
            PlayerRepository.REPOSITORY.saveOnMainThread();
            PlayerRepository.REPOSITORY.shutdown(this);
        } catch (Throwable ex) {
            if (!(ex instanceof NullPointerException))
                ex.printStackTrace();
        }
        try {
            disableArenas();
        } catch (Exception e) {
            logger().warning("Failed to regenerate arenas.");
            e.printStackTrace();
        }
        extensions.save();
        saveArenas();
        messageManager.save();
        statsFile.save();
        configurationPack.save();
        joinGuiFile.save();
    }

    public static SchematicManager newSchematicManager(String name) {
        return SchematicManager.newSchematicManager((WorldEditPlugin) getSpleefX().worldEdit, name, getSpleefX().arenasFolder);
    }

    private void saveArenas() {
        if (arenasError) return;
        arenasConfig.set("Arenas", SpleefXGson.toMap(Arenas.getArenas()));
        arenasConfig.save();
    }

    private void disableArenas() {
        Arenas.getArenas().values().stream().filter(arena -> arena.getEngine().getStage().isEndable())
                .forEach(arena -> arena.getEngine().end(true));
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        SpleefXConfig.load(false);
    }

    public static SpectatorSettings getSpectatorSettings() {
        return getSpleefX().spectatorMenu;
    }

    public static Logger logger() {
        return getPlugin().getLogger();
    }

    public static SpleefX getSpleefX() {
        return APP.get();
    }

    public static JavaPlugin getPlugin() {
        return PLUGIN.get();
    }

    public void addListener(@NotNull Object listener) {
        if (listener instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) listener, plugin);
        if (listener instanceof EventListener)
            EventListener.register((EventListener) listener);
        if (listener instanceof PacketListener)
            ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) listener);
        if (listener instanceof PlayerArenaInteractionListener)
            StandardCallbacks.register(listener);
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }

    public JavaPlugin plugin() {
        return plugin;
    }

    public void info(String... message) {
        for (String m : message)
            logger().info(m);
    }

    public void load() {}


    public void error(String... message) {
        for (String m : message)
            logger().severe(m);
    }

    public void warn(String... message) {
        for (String m : message)
            logger().warning(m);
    }

    public YamlConfiguration getRelativeFile(@NotNull String name) {
        return YamlConfiguration.loadConfiguration(fileManager.createFile(name));
    }
}
