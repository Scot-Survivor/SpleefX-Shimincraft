package net.spleefx.core.command.permission;

import net.spleefx.extension.MatchExtension;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public interface PermissionSupplier {

    static PermissionSupplier fromStatic(String node, PermissionDefault def) {
        return new StaticPermission(node, def);
    }

    static PermissionSupplier fromDynamic(String node, PermissionDefault def) {
        return new PermissionMap(node, def);
    }

    static PermissionSupplier none() {
        return StaticPermission.NONE;
    }

    @NotNull
    Permission getPermission(MatchExtension extension);

    String node();

    boolean isDefault();

    default boolean test(CommandSender sender, MatchExtension extension) {
        return sender.hasPermission(getPermission(extension));
    }

}
