package org.game.eternity2.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for server-client communication.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
class ServerClientIntegrationTest {

    private EternityServer server;

    @BeforeEach
    void setUp() {
        server = new EternityServer(12346); // Different port for testing
    }

    @Test
    void testServerStartStop() {
        assertFalse(server.isRunning());

        server.startServer();
        assertTrue(server.isRunning());

        server.stopServer();
        assertFalse(server.isRunning());
    }

    @Test
    void testJobManagerInitialization() {
        server.startServer();

        JobManager.JobStatistics stats = server.getJobManager().getStatistics();
        assertNotNull(stats);
        assertTrue(stats.getTotalJobs() > 0, "Jobs should be initialized");

        server.stopServer();
    }

    @Test
    void testUserDatabaseIntegration() {
        UserDatabase db = new UserDatabase();

        // Test registration
        boolean registered = db.registerUser("testuser", "password123");
        assertTrue(registered, "Should register new user");

        // Test duplicate registration
        boolean duplicate = db.registerUser("testuser", "password123");
        assertFalse(duplicate, "Should not register duplicate user");

        // Test authentication
        boolean authenticated = db.authenticateUser("testuser", "password123");
        assertTrue(authenticated, "Should authenticate valid user");

        boolean wrongPassword = db.authenticateUser("testuser", "wrongpass");
        assertFalse(wrongPassword, "Should reject wrong password");
    }

    @Test
    void testStatisticsTracking() {
        ServerStatistics stats = new ServerStatistics();

        stats.incrementPacketsSent();
        stats.incrementPacketsReceived();

        assertEquals(1, stats.getPacketsSent());
        assertEquals(1, stats.getPacketsReceived());
    }
}
