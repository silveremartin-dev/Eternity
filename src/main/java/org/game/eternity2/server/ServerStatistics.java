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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks server statistics for monitoring and display.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class ServerStatistics {
    private final AtomicInteger activeClients = new AtomicInteger(0);
    private final AtomicInteger totalClients = new AtomicInteger(0);
    private final AtomicLong dataPacketsSent = new AtomicLong(0);
    private final AtomicLong dataPacketsReceived = new AtomicLong(0);
    private final AtomicLong statPacketsSent = new AtomicLong(0);
    private final AtomicLong statPacketsReceived = new AtomicLong(0);
    private final AtomicLong totalComputeTimeMs = new AtomicLong(0);
    private final AtomicLong piecesSolved = new AtomicLong(0);
    private final AtomicLong currentPiecesPerSecond = new AtomicLong(0);
    private org.game.eternity2.model.BoardPrimitive bestBoard;
    private final long startTime;

    /**
     * Wall-clock time (ms) during which at least one client was connected.
     * Incremented by {@code delta} whenever {@code activeClients > 0}.
     */
    private final AtomicLong connectedTimeMs = new AtomicLong(0);

    /**
     * Cumulative parallel compute time (ms) across all clients.
     * Each tick adds {@code activeClients × delta}, so 3 clients working
     * 1 minute each contribute 3 min to this counter.
     */
    private final AtomicLong cumulativeClientTimeMs = new AtomicLong(0);

    public void updateBestBoard(org.game.eternity2.model.BoardPrimitive board) {
        this.bestBoard = board;
    }

    public org.game.eternity2.model.BoardPrimitive getBestBoard() {
        return bestBoard;
    }

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

    public void incrementPacketsSent(boolean isStat) {
        if (isStat) statPacketsSent.incrementAndGet();
        else dataPacketsSent.incrementAndGet();
    }

    public void incrementPacketsReceived(boolean isStat) {
        if (isStat) statPacketsReceived.incrementAndGet();
        else dataPacketsReceived.incrementAndGet();
    }

    public void addComputeTime(long milliseconds) {
        totalComputeTimeMs.addAndGet(milliseconds);
    }

    public void incrementPiecesSolved(long count) {
        piecesSolved.addAndGet(count);
    }

    public int getActiveClients() {
        return activeClients.get();
    }

    public int getTotalClients() {
        return totalClients.get();
    }

    public long getDataPacketsSent() { return dataPacketsSent.get(); }
    public long getStatPacketsSent() { return statPacketsSent.get(); }
    public long getDataPacketsReceived() { return dataPacketsReceived.get(); }
    public long getStatPacketsReceived() { return statPacketsReceived.get(); }

    public long getTotalComputeTimeMs() {
        return totalComputeTimeMs.get();
    }

    public long getPiecesSolved() {
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

    public void setPiecesPerSecond(long pps) {
        currentPiecesPerSecond.set(pps);
    }

    public long getCurrentPiecesPerSecond() {
        return currentPiecesPerSecond.get();
    }

    /**
     * Called once per scheduler tick (typically every second).
     * Updates both time accumulators based on the current active client count.
     *
     * @param deltaMs elapsed milliseconds since the last tick
     */
    public void tickTime(long deltaMs) {
        int clients = activeClients.get();
        if (clients > 0) {
            connectedTimeMs.addAndGet(deltaMs);
            cumulativeClientTimeMs.addAndGet((long) clients * deltaMs);
        }
    }

    /** Time (ms) during which ≥1 client was connected. */
    public long getConnectedTimeMs() {
        return connectedTimeMs.get();
    }

    /** Cumulative parallel compute time (ms) across all clients (sum of activeClients × elapsed). */
    public long getCumulativeClientTimeMs() {
        return cumulativeClientTimeMs.get();
    }

    /** Reset both time counters (e.g. on server restart). */
    public void resetTimers() {
        connectedTimeMs.set(0);
        cumulativeClientTimeMs.set(0);
    }
}
