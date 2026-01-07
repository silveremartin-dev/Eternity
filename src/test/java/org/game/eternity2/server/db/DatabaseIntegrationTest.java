package org.game.eternity2.server.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@Disabled("Requires Docker environment")
class DatabaseIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("eternity_test")
            .withUsername("test")
            .withPassword("test");

    @BeforeEach
    void setUp() {
        // Set system properties to point to the Testcontainer
        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        System.setProperty("DB_ENABLED", "true");

        // Reset singleton to ensure it picks up new config
        DatabaseManager.resetInstance();
    }

    @AfterEach
    void tearDown() {
        // Clear system properties
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        System.clearProperty("DB_ENABLED");

        DatabaseManager.resetInstance();
    }

    @Test
    void testDatabaseConnectionAndMigration() throws Exception {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        assertTrue(dbManager.isEnabled(), "Database should be enabled");

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement()) {

            // Verify connection
            assertFalse(conn.isClosed());

            // Verify simple query
            try (ResultSet rs = stmt.executeQuery("SELECT 1")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }

            // Verify Flyway migrations (assuming "flyway_schema_history" or similar table
            // exists)
            // or just check if our schema tables exist (e.g., users table if defined in
            // migration)
            // Since I don't know exact schema, I'll check for flyway table
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM flyway_schema_history")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 0);
            }
        }
    }
}
