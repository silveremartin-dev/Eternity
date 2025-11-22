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

import java.io.Serializable;

/**
 * Server status information sent to clients for monitoring.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class ServerStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean serverUp;
    private final int activeClients;
    private final int totalClients;
    private final long totalComputeTimeMs;
    private final long uptimeMs;
    private final double averageComputeTimePerClient;
    private final double piecesPerSecond;
    private final int bestScore;
    private final long packetsSent;
    private final long packetsReceived;

    public ServerStatus(boolean serverUp, int activeClients, int totalClients,
            long totalComputeTimeMs, long uptimeMs,
            double averageComputeTimePerClient, double piecesPerSecond,
            int bestScore, long packetsSent, long packetsReceived) {
        this.serverUp = serverUp;
        this.activeClients = activeClients;
        this.totalClients = totalClients;
        this.totalComputeTimeMs = totalComputeTimeMs;
        this.uptimeMs = uptimeMs;
        this.averageComputeTimePerClient = averageComputeTimePerClient;
        this.piecesPerSecond = piecesPerSecond;
        this.bestScore = bestScore;
        this.packetsSent = packetsSent;
        this.packetsReceived = packetsReceived;
    }

    public boolean isServerUp() {
        return serverUp;
    }

    public int getActiveClients() {
        return activeClients;
    }

    public int getTotalClients() {
        return totalClients;
    }

    public long getTotalComputeTimeMs() {
        return totalComputeTimeMs;
    }

    public long getUptimeMs() {
        return uptimeMs;
    }

    public double getAverageComputeTimePerClient() {
        return averageComputeTimePerClient;
    }

    public double getPiecesPerSecond() {
        return piecesPerSecond;
    }

    public int getBestScore() {
        return bestScore;
    }

    public long getPacketsSent() {
        return packetsSent;
    }

    public long getPacketsReceived() {
        return packetsReceived;
    }

    public boolean isRunning() {
        return serverUp;
    }

    public int getConnectedClients() {
        return activeClients;
    }

    public int getTotalJobs() {
        return 0; // Will be implemented when JobManager integration is complete
    }

    public int getCompletedJobs() {
        return 0; // Will be implemented when JobManager integration is complete
    }

    public int getPendingJobs() {
        return 0; // Will be implemented when JobManager integration is complete
    }
}
