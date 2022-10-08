package net.spleefx.extension;

import net.spleefx.SpleefX;
import net.spleefx.core.command.internal.ForwardingCommandExecutor;
import net.spleefx.core.command.internal.PluginCommandBuilder;
import net.spleefx.model.ExtensionType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class Extensions {

    private static final Map<String, MatchExtension> EXTENSIONS = new HashMap<>();
    private static final Map<String, MatchExtension> byCommand = new HashMap<>();

    public static MatchExtension getByKey(@NotNull String key) {
        return EXTENSIONS.get(key);
    }

    public static MatchExtension getByCommand(@NotNull String command) {
        return byCommand.get(command);
    }

    public static void registerAll(@NotNull Map<String, MatchExtension> extensionMap) {
        EXTENSIONS.putAll(extensionMap);
    }

    public static Collection<MatchExtension> getExtensions() {
        return EXTENSIONS.values();
    }

    public static MatchExtension load(@NotNull String key) {
        MatchExtension extension = SpleefX.getSpleefX().getExtensions().getOrParse(key, ExtensionType.CUSTOM.of(key));
        if (extension == null) return null;
        registerExtension(extension);
        if (!extension.isStandard()) {
            extension.getExtensionCommands()
                    .forEach(command -> new PluginCommandBuilder(command, SpleefX.getPlugin())
                            .command(ForwardingCommandExecutor.instance)
                            .register());
        }
        return extension;
    }

    public static boolean registerExtension(@NotNull MatchExtension extension) {
        extension.getExtensionCommands().forEach(c -> byCommand.put(c, extension));
        boolean mapped = EXTENSIONS.put(extension.getKey(), extension) == null;
        if (mapped) {
            SpleefX.logger().info("Extension \"" + extension.getKey() + "\" successfully loaded!");
            extension.getGameTitles().remove(GameEvent.LOSE);
        }
        return mapped;
    }
}
