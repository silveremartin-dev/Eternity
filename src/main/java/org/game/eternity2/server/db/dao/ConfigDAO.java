package org.game.eternity2.server.db.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.server.db.DatabaseManager;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for configuration stored in the database.
 * Provides methods to get and set configuration values.
 */
public class ConfigDAO {

    private static final Logger LOGGER = LogManager.getLogger(ConfigDAO.class);
    private final DatabaseManager dbManager;

    public ConfigDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Get a configuration value by key.
     * 
     * @param key          Configuration key
     * @param defaultValue Default value if key not found
     * @return Configuration value or default
     */
    public String get(String key, String defaultValue) {
        if (!dbManager.isEnabled()) {
            return defaultValue;
        }

        String sql = "SELECT value FROM config WHERE key = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to get config key: {}", key, e);
        }
        return defaultValue;
    }

    /**
     * Get a configuration value as integer.
     */
    public int getInt(String key, int defaultValue) {
        String value = get(key, null);
        if (value == null)
            return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get a configuration value as boolean.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, null);
        if (value == null)
            return defaultValue;
        return Boolean.parseBoolean(value);
    }

    /**
     * Set a configuration value.
     * 
     * @param key         Configuration key
     * @param value       Configuration value
     * @param description Optional description
     */
    public void set(String key, String value, String description) {
        if (!dbManager.isEnabled()) {
            LOGGER.warn("Database disabled, cannot set config: {}", key);
            return;
        }

        String sql = """
                INSERT INTO config (key, value, description) VALUES (?, ?, ?)
                ON CONFLICT (key) DO UPDATE SET value = ?, updated_at = CURRENT_TIMESTAMP
                """;

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, description);
            stmt.setString(4, value);
            stmt.executeUpdate();
            LOGGER.debug("Config set: {} = {}", key, value);
        } catch (SQLException e) {
            LOGGER.error("Failed to set config: {} = {}", key, value, e);
        }
    }

    /**
     * Get all configuration as a map.
     */
    public Map<String, String> getAll() {
        Map<String, String> config = new HashMap<>();

        if (!dbManager.isEnabled()) {
            return config;
        }

        String sql = "SELECT key, value FROM config";
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                config.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to get all config", e);
        }
        return config;
    }

    /**
     * Delete a configuration key.
     */
    public boolean delete(String key) {
        if (!dbManager.isEnabled()) {
            return false;
        }

        String sql = "DELETE FROM config WHERE key = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to delete config: {}", key, e);
            return false;
        }
    }
}
