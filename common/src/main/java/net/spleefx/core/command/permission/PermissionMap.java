package net.spleefx.core.command.permission;

import com.google.common.collect.ForwardingMap;
import net.spleefx.extension.MatchExtension;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PermissionMap extends ForwardingMap<MatchExtension, Permission> implements PermissionSupplier {

    private final Map<MatchExtension, Permission> map = new HashMap<>();

    private final PermissionDefault access;
    private final String node;

    public PermissionMap(String node, PermissionDefault access) {
        this.access = access;
        this.node = node;
    }

    @Override protected Map<MatchExtension, Permission> delegate() {
        return map;
    }

    @Override public @NotNull Permission getPermission(MatchExtension extension) {
        return computeIfAbsent(extension, c -> new Permission(node.replace("{ext}", extension.getKey()), access));
    }

    @Override public String node() {
        return node;
    }

    @Override public boolean isDefault() {
        return access == PermissionDefault.TRUE;
    }
}