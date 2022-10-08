package net.spleefx.core.data;

import net.spleefx.core.data.PlayerCacheManager.IConnectable;
import net.spleefx.core.data.impl.ForwardingCacheManager;
import net.spleefx.core.data.impl.HikariConnector;
import org.jetbrains.annotations.NotNull;

/**
 * This class <i>requires</i> that {@link StorageMapping} has the same exact enum names.
 * <p>
 * This class is loaded before any dependencies are available. {@link StorageMapping} is simply
 * a post-load alternative to this enum.
 */
public enum StorageType {

    /* Flat files */
    JSON("JSON"),

    /* SQL */
    MYSQL("MySQL"),
    MARIADB("MariaDB"),
    POSTGRESQL("PostgreSQL"),
    H2("H2"),
    SQLITE("SQLite");

    private final String name;

    StorageType(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Sets the delegating writer and loader to this instance
     */
    public void delegate() {
        StorageMapping mapping = StorageMapping.valueOf(name());
        ForwardingCacheManager.delegateTo(mapping.getCacheManager());
        if (mapping.getCacheManager() instanceof IConnectable)
            ((IConnectable) mapping.getCacheManager()).connect();
    }
}