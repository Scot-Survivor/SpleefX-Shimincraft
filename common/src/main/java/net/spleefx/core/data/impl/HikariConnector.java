package net.spleefx.core.data.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.spleefx.SpleefX;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.data.PlayerCacheManager.IConnectable;
import net.spleefx.core.data.database.sql.StatementKey;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple utility class to allow easy establishing of dataSource using HikariCP.
 */
public abstract class HikariConnector implements IConnectable {

    /**
     * A cache of all schemas
     */
    private static final Map<String, Map<StatementKey, String>> SCHEMAS = new HashMap<>();

    /**
     * The constant instance of {@link Connection}.
     */
    protected HikariDataSource dataSource;

    /**
     * The schemas for this database type
     */
    protected final Map<StatementKey, String> schemas;

    /**
     * The JDBC name
     */
    protected final String jdbcName;

    /**
     * The JDBC driver class name
     */
    protected final String driverClass;

    /**
     * Creates a new HikariCP-based dataSource
     *
     * @param name The database type name which will be used to handle database-type-specific queries.
     */
    public HikariConnector(@NotNull String name, @NotNull String driverClass) {
        schemas = SCHEMAS.computeIfAbsent(name, StatementKey::parseSchema);
        jdbcName = name;
        this.driverClass = driverClass;
    }

    protected void setProperties(HikariConfig config) {
    }

    protected void preConnect() {
    }

    protected String createJdbcURL() {
        return String.format("jdbc:" + jdbcName + "://%s/%s?serverTimezone=UTC", SpleefXConfig.DB_HOST.get(), SpleefXConfig.DB_NAME.get());
    }

    protected void setCredentials(HikariConfig config) {
        config.setUsername(SpleefXConfig.DB_USER.get());
        config.setPassword(SpleefXConfig.DB_PASSWORD.get());
    }

    /**
     * Connects to the database and sets the {@link Connection} instance.
     */
    public void connect() {
        preConnect();

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(createJdbcURL());
        setCredentials(config);

        config.setPoolName("SpleefX-Pool");
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(SpleefXConfig.HIKARI_MAX_LIFETIME.get()); // 120 seconds
        config.setMaximumPoolSize(SpleefXConfig.HIKARI_MAX_POOL_SIZE.get()); // 10 connections (including idle connections)

        // statement cache
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);

        config.setDriverClassName(driverClass);

        // other settings which i absolutely have no idea what they do but they're on HikariCP's wiki
        // for best performance soo
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("maintainTimeStats", false);

        setProperties(config);

        dataSource = new HikariDataSource(config);
        execute(StatementKey.CREATE_TABLE);
    }

    /**
     * Prepares a statement from the schema
     *
     * @param statement Statement to create for
     * @return The prepared statement
     * @throws SQLException {@link Connection#prepareStatement(String)}.
     */
    protected PreparedStatement prepare(@NotNull Connection connection, @NotNull StatementKey statement, Object... parameters) throws SQLException {
        PreparedStatement prep = connection.prepareStatement(schemas.get(statement));
        for (int i = 1; i <= parameters.length; i++) {
            prep.setObject(i, parameters[i - 1]);
        }
        return prep;
    }

    /**
     * Executes the specified query
     *
     * @param statementKey to execute
     */
    protected void execute(@NotNull StatementKey statementKey) {
        String query = schemas.get(statementKey);
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            SpleefX.logger().severe("Cannot run query " + query + ".");
            e.printStackTrace();
        }
    }

    /**
     * Returns the Hikari data source responsible for connection pooling
     *
     * @return The data source
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Executes the specified query
     *
     * @param statementKey to execute
     */
    protected ResultSet executeQuery(@NotNull Connection connection, @NotNull StatementKey statementKey) {
        String query = schemas.get(statementKey);
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            SpleefX.logger().severe("Cannot run query " + query + ".");
            throw new IllegalStateException(e);
        }
    }
}