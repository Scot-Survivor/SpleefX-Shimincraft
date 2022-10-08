package net.spleefx.core.data;

import net.spleefx.core.data.database.flat.JsonBasedManager;
import net.spleefx.core.data.database.sql.SQLBasedManager;
import org.jetbrains.annotations.NotNull;

public enum StorageMapping {

    JSON(JsonBasedManager.JSON),
    MYSQL(SQLBasedManager.MYSQL),
    POSTGRESQL(SQLBasedManager.POSTGRESQL),
    MARIADB(SQLBasedManager.MARIADB),
    H2(SQLBasedManager.H2),
    SQLITE(SQLBasedManager.SQLITE);

    private final PlayerCacheManager cacheManager;

    StorageMapping(@NotNull PlayerCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public PlayerCacheManager getCacheManager() {
        return cacheManager;
    }

}