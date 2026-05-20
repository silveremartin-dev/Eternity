/*
 * Copyright 2022-2024 Silvere Martin-Michiellot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.game.eternity2.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ServerStatistics.
 */
class ServerStatisticsTest {

    @Test
    void testInitialStatistics() {
        ServerStatistics stats = new ServerStatistics();
        assertEquals(0, stats.getActiveClients());
        assertEquals(0, stats.getTotalClients());
        assertEquals(0, stats.getPiecesSolved());
    }

    @Test
    void testIncrementActiveClients() {
        ServerStatistics stats = new ServerStatistics();
        stats.incrementActiveClients();
        stats.incrementActiveClients();
        assertEquals(2, stats.getActiveClients());
        assertEquals(2, stats.getTotalClients());
    }

    @Test
    void testDecrementActiveClients() {
        ServerStatistics stats = new ServerStatistics();
        stats.incrementActiveClients();
        stats.incrementActiveClients();
        stats.decrementActiveClients();
        assertEquals(1, stats.getActiveClients());
        assertEquals(2, stats.getTotalClients()); // Total stays the same
    }

    @Test
    void testPacketTracking() {
        ServerStatistics stats = new ServerStatistics();
        stats.incrementPacketsSent(false);
        stats.incrementPacketsSent(true);
        stats.incrementPacketsReceived(false);
        assertEquals(1, stats.getDataPacketsSent());
        assertEquals(1, stats.getStatPacketsSent());
        assertEquals(1, stats.getDataPacketsReceived());
        assertEquals(0, stats.getStatPacketsReceived());
    }

    @Test
    void testComputeTimeTracking() {
        ServerStatistics stats = new ServerStatistics();
        stats.addComputeTime(100);
        stats.addComputeTime(50);
        assertEquals(150, stats.getTotalComputeTimeMs());
    }

    @Test
    void testPiecesSolved() {
        ServerStatistics stats = new ServerStatistics();
        stats.incrementPiecesSolved(10);
        stats.incrementPiecesSolved(5);
        assertEquals(15, stats.getPiecesSolved());
    }

    @Test
    void testUptime() {
        ServerStatistics stats = new ServerStatistics();
        // Uptime should be very small since we just created it
        assertTrue(stats.getUptimeMs() >= 0);
        assertTrue(stats.getUptimeMs() < 1000);
    }

    @Test
    void testConnectedClients() {
        ServerStatistics stats = new ServerStatistics();
        stats.incrementActiveClients();
        assertEquals(stats.getActiveClients(), stats.getConnectedClients());
    }
}
