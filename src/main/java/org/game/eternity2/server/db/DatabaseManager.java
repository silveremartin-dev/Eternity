package org.game.eternity2.server.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection manager using HikariCP connection pool.
 * Handles connection pooling and Flyway migrations.
 */
public class DatabaseManager implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    private final HikariDataSource dataSource;
    private final boolean enabled;

    private DatabaseManager() {
        String dbUrl = System.getProperty("DB_URL", System.getenv().getOrDefault("DB_URL",
                "jdbc:postgresql://localhost:5432/eternity"));
        String dbEnabled = System.getProperty("DB_ENABLED", System.getenv().getOrDefault("DB_ENABLED", "true"));

        this.enabled = Boolean.parseBoolean(dbEnabled);

        if (!enabled) {
            LOGGER.info("Database is disabled via DB_ENABLED env var");
            this.dataSource = null;
            return;
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(System.getProperty("DB_USER", System.getenv().getOrDefault("DB_USER", "postgres")));
        config.setPassword(System.getProperty("DB_PASSWORD", System.getenv().getOrDefault("DB_PASSWORD", "postgres")));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("EternityPool");

        try {
            this.dataSource = new HikariDataSource(config);
            LOGGER.info("Database connection pool initialized: {}", dbUrl);
            runMigrations();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void runMigrations() {
        LOGGER.info("Running Flyway migrations...");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        var result = flyway.migrate();
        LOGGER.info("Flyway migrations completed: {} migrations applied",
                result.migrationsExecuted);
    }

    /**
     * Get the singleton instance of DatabaseManager.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Reset the singleton instance (For Testing Only).
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    /**
     * Check if database is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get a connection from the pool.
     */
    public Connection getConnection() throws SQLException {
        if (!enabled || dataSource == null) {
            throw new SQLException("Database is not enabled");
        }
        return dataSource.getConnection();
    }

    /**
     * Get the underlying DataSource.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Database connection pool closed");
        }
    }
}
