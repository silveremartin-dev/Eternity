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
    private final AtomicLong packetsSent = new AtomicLong(0);
    private final AtomicLong packetsReceived = new AtomicLong(0);
    private final AtomicLong totalComputeTimeMs = new AtomicLong(0);
    private final AtomicInteger piecesSolved = new AtomicInteger(0);
    private final AtomicInteger currentPiecesPerSecond = new AtomicInteger(0);
    private org.game.eternity2.model.BoardPrimitive bestBoard;
    private final long startTime;

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

    public void setPiecesPerSecond(int pps) {
        currentPiecesPerSecond.set(pps);
    }

    public int getCurrentPiecesPerSecond() {
        return currentPiecesPerSecond.get();
    }
}
