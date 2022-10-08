package net.spleefx.core.command.permission;

import net.spleefx.extension.MatchExtension;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

/**
 * A fixed implementation of {@link PermissionSupplier} optimized to handle a single, static
 * permission.
 */
public class StaticPermission implements PermissionSupplier {

    static final StaticPermission NONE = new StaticPermission(null, PermissionDefault.TRUE);

    private Permission permission;
    private final String node;

    public StaticPermission(String permission, PermissionDefault access) {
        if (permission != null) {
            this.permission = new Permission(permission, access);
        }
        node = permission;
    }

    @Override
    public @NotNull Permission getPermission(MatchExtension extension) {
        return permission;
    }

    @Override public boolean test(CommandSender sender, MatchExtension extension) {
        return permission == null || sender.hasPermission(permission);
    }

    @Override public String node() {
        return node;
    }

    @Override public boolean isDefault() {
        return permission == null || permission.getDefault() == PermissionDefault.TRUE;
    }
}
