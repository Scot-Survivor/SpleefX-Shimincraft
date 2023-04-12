package net.spleefx.command;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.backend.Schedulers;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import net.spleefx.hook.worldedit.SchematicManager;
import net.spleefx.json.SpleefXGson;
import net.spleefx.util.JsonBuilder;
import net.spleefx.util.plugin.Protocol;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RegisteredCommand
public class DebugCommand extends BaseCommand {

    private static final List<String> REDACTED_PATHS = ImmutableList.of(
            "PlayerGameStatistics.Database.Host",
            "PlayerGameStatistics.Database.DatabaseName",
            "PlayerGameStatistics.Database.Username",
            "PlayerGameStatistics.Database.Password"
    );

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("debug", "dump")
                .checkIfArgsAre(anything())
                .description("Build a report that contains useful information for debugging")
                .permission("spleefx.admin.debug")
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX spleefx, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        CompletableFuture<JsonObject> report = new CompletableFuture<>();
        Schedulers.POOL.submit(() -> {
            JsonBuilder debug = new JsonBuilder();

            debug.map("Generated", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:SS"));

            // map server info
            JsonObject serverInfo = new JsonBuilder()
                    .map("Version", Protocol.VERSION + " (" + Bukkit.getBukkitVersion() + ") - [" + Bukkit.getVersion() + "]")
                    .map("Platform", Bukkit.getName())
                    .map("SpleefX Version", SpleefX.getPlugin().getDescription().getVersion())
                    .map("Java Version", System.getProperty("java.version"))
                    .map("WorldEdit Adapter", SchematicManager.factoryName)
                    .map("NMS Adapter", PluginCompatibility.getNMS().getClass().getName())
                    .build().getAsJsonObject();
            if (SpleefXConfig.VAULT_EXISTS)
                serverInfo.addProperty("Vault", Objects.toString(spleefx.getVaultHandler().getEconomy()));

            // map plugins list
            JsonObject plugins = new JsonObject();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                PluginDescriptionFile pdf = plugin.getDescription();
                plugins.addProperty(pdf.getName(), pdf.getVersion());
            }

            // map plugin configuration
            JsonBuilder config = new JsonBuilder();
            for (Entry<String, Object> option : SpleefX.getPlugin().getConfig().getValues(true).entrySet()) {
                if (option.getValue() instanceof MemorySection) continue;
                if (REDACTED_PATHS.contains(option.getKey()))
                    config.map(option.getKey(), "REDACTED");
                else
                    config.map(option.getKey(), option.getValue());
            }

            JsonObject extensions = new JsonObject();

            // map extensions
            for (MatchExtension mode : Extensions.getExtensions()) {
                extensions.add(mode.getKey(), SpleefXGson.MAIN.toJsonTree(mode));
            }

            // spectator settings
            JsonElement spectator = SpleefXGson.MAIN.toJsonTree(SpleefX.getSpectatorSettings());

            debug.map("Server Info", serverInfo)
                    .map("Config", config.build().getAsJsonObject())
                    .map("Extensions", extensions)
                    .map("Spectator settings", spectator)
                    .map("Plugin list", plugins);
            report.complete(debug.build().getAsJsonObject());
        });

        report.thenAccept(content -> {
            if (args.flag("local")) {
                try {
                    File file = new File(SpleefX.getPlugin().getDataFolder(), "debug.json");
                    file.createNewFile();
                    Files.write(file.toPath(), SpleefXGson.MAIN.toJson(content).getBytes(), StandardOpenOption.WRITE);
                    sender.reply("&aReport saved to &e" + file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return Response.ok("&aGenerating a full dump report. Please wait...");
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion.of(TabCompletion.literal("-local"));
    }
}