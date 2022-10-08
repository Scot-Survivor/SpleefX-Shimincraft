package net.spleefx.core.data.database.sql;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.zaxxer.hikari.HikariConfig;
import net.spleefx.SpleefX;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.data.PlayerCacheManager;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.core.data.impl.HikariConnector;
import net.spleefx.core.data.impl.SXPlayerProfile;
import net.spleefx.util.JsonBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.Executor;

public class SQLBasedManager extends HikariConnector implements PlayerCacheManager {

    public static final SQLBasedManager MYSQL = new SQLBasedManager("mysql", "com.mysql.cj.jdbc.Driver");
    public static final SQLBasedManager MARIADB = new SQLBasedManager("mariadb", "org.mariadb.jdbc.Driver");
    public static final SQLBasedManager POSTGRESQL = new SQLBasedManager("postgresql", "org.postgresql.Driver");
    public static final SQLBasedManager H2 = new SQLBasedManager("h2", "org.h2.Driver") {
        @Override protected String createJdbcURL() {
            File file = new File(SpleefX.getPlugin().getDataFolder() + File.separator + "players-data",
                    SpleefXConfig.DB_NAME.get());
            return "jdbc:h2:file:" + file.getAbsolutePath() + ";DATABASE_TO_UPPER=false";
        }

        @Override protected void setCredentials(HikariConfig config) {
            // h2 has no credentials. default implementation sets them, so we don't want that.
        }
    };

    public static final SQLBasedManager SQLITE = new SQLBasedManager("sqlite", "org.sqlite.JDBC") {

        @Override public Executor executor() {
            // sqlite cannot be multithreaded.
            return SYNC;
        }

        @Override protected void setCredentials(HikariConfig config) {
            // sqlite has no credentials. default implementation sets them, so we don't want that.
        }

        @Override protected String createJdbcURL() {
            File parent = new File(SpleefX.getPlugin().getDataFolder(), "players-data");
            File file = new File(parent, SpleefXConfig.DB_NAME.get() + ".db");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "jdbc:sqlite:" + file.getAbsolutePath();
        }
    };

    public SQLBasedManager(String name, String driver) {
        super(name, driver);
    }

    @Nullable @Override
    public PlayerProfile load(@NonNull UUID key) {
        try (Connection connection = dataSource.getConnection()) {
            ResultSet set = prepare(connection, StatementKey.SELECT_PLAYER, key.toString()).executeQuery();
            if (set.next()) {
                UUID uuid = UUID.fromString(set.getString("PlayerUUID"));
                return GSON.fromJson(new JsonBuilder()
                        .map("uuid", uuid)
                        .map("coins", set.getInt("coins"))
                        .map("stats", GSON.fromJson(set.getString("GlobalStats"), GLOBAL_STATS_TYPE))
                        .map("modeStats", GSON.fromJson(set.getString("ExtensionStats"), EXT_STATS_TYPE))
                        .map("spleggUpgrades", GSON.fromJson(set.getString("PurchasedSpleggUpgrades"), KEY_TYPE))
                        .map("selectedSpleggUpgrade", set.getString("SpleggUpgrade"))
                        .build(), SXPlayerProfile.class);
            }
            if (!set.isClosed())
                set.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void cacheAll(LoadingCache<UUID, PlayerProfile> cache) {
        try (Connection connection = dataSource.getConnection()) {
            ResultSet set = executeQuery(connection, StatementKey.BULK_SELECT_PLAYERS);

            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("PlayerUUID"));
                cache.put(uuid, GSON.fromJson(new JsonBuilder()
                        .map("uuid", uuid)
                        .map("coins", set.getInt("coins"))
                        .map("stats", GSON.fromJson(set.getString("GlobalStats"), GLOBAL_STATS_TYPE))
                        .map("modeStats", GSON.fromJson(set.getString("ExtensionStats"), EXT_STATS_TYPE))
                        .map("spleggUpgrades", GSON.fromJson(set.getString("PurchasedSpleggUpgrades"), KEY_TYPE))
                        .map("selectedSpleggUpgrade", set.getString("SpleggUpgrade"))
                        .build(), SXPlayerProfile.class));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override public void writeAll(@NotNull Map<UUID, PlayerProfile> map, boolean forcibly) {
        if (map.isEmpty()) return;
        StringJoiner query = new StringJoiner(",").setEmptyValue("");
        for (Entry<UUID, PlayerProfile> entry : map.entrySet()) {
            if (entry.getValue().modified() || forcibly)
                query.add(entry.getValue().asPlaceholders());
        }
        if (query.toString().isEmpty()) return;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(String.format(schemas.get(StatementKey.UPSERT_PLAYER), query));
            int i = 1;
            for (Entry<UUID, PlayerProfile> entry : map.entrySet()) {
                if (entry.getValue().modified() || forcibly)
                    i += entry.getValue().passToStatement(entry.getKey(), statement, i);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override public void write(@NonNull UUID key, @NonNull PlayerProfile value) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(String.format(schemas.get(StatementKey.UPSERT_PLAYER), value.asPlaceholders()));
            value.passToStatement(key, statement, 1);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override public void delete(@NonNull UUID key, @Nullable PlayerProfile value, @NonNull RemovalCause cause) {
        if (cause.wasEvicted()) return;
        try (Connection connection = dataSource.getConnection()) {
            prepare(connection, StatementKey.DELETE_PLAYER, key.toString()).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void shutdown(SpleefX plugin) {
        if (!dataSource.isClosed())
            dataSource.close();
    }
}