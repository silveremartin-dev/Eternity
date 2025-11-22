/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.game.eternity2.server;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks server statistics for monitoring and display.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class ServerStatistics {
    private final AtomicInteger activeClients = new AtomicInteger(0);
    private final AtomicInteger totalClients = new AtomicInteger(0);
    private final AtomicLong packetsSent = new AtomicLong(0);
    private final AtomicLong packetsReceived = new AtomicLong(0);
    private final AtomicLong totalComputeTimeMs = new AtomicLong(0);
    private final AtomicInteger piecesSolved = new AtomicInteger(0);
    private final long startTime;

    public ServerStatistics() {
        this.startTime = System.currentTimeMillis();
    }

    public void incrementActiveClients() {
        activeClients.incrementAndGet();
        totalClients.incrementAndGet();
    }

    public void decrementActiveClients() {
        activeClients.decrementAndGet();
    }

    public void incrementPacketsSent() {
        packetsSent.incrementAndGet();
    }

    public void incrementPacketsReceived() {
        packetsReceived.incrementAndGet();
    }

    public void addComputeTime(long milliseconds) {
        totalComputeTimeMs.addAndGet(milliseconds);
    }

    public void incrementPiecesSolved(int count) {
        piecesSolved.addAndGet(count);
    }

    public int getActiveClients() {
        return activeClients.get();
    }

    public int getTotalClients() {
        return totalClients.get();
    }

    public long getPacketsSent() {
        return packetsSent.get();
    }

    public long getPacketsReceived() {
        return packetsReceived.get();
    }

    public long getTotalComputeTimeMs() {
        return totalComputeTimeMs.get();
    }

    public int getPiecesSolved() {
        return piecesSolved.get();
    }

    public long getUptimeMs() {
        return System.currentTimeMillis() - startTime;
    }

    public double getAverageComputeTimePerClient() {
        int total = totalClients.get();
        return total > 0 ? (double) totalComputeTimeMs.get() / total : 0.0;
    }

    public double getPiecesPerSecond() {
        long uptimeSec = getUptimeMs() / 1000;
        return uptimeSec > 0 ? (double) piecesSolved.get() / uptimeSec : 0.0;
    }

    public int getConnectedClients() {
        return activeClients.get();
    }
}
