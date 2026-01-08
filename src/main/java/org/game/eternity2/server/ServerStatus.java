/*
 * MIT License
 *
 * Copyright (c) 2026 Silvere Martin-Michiellot, Antigravity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.game.eternity2.server;

import java.io.Serializable;

/**
 * Server status information sent to clients for monitoring.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
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
