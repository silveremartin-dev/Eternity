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
package org.game.eternity2.client;

import org.game.eternity2.model.BoardPrimitive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tracks client statistics for monitoring and display.
 * Supports session vs total statistics and persistence.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.2
 * @author Antigravity
 * @since 1.0
 */
public class ClientStatistics {
    private static final Logger logger = LogManager.getLogger(ClientStatistics.class);

    // Session stats
    private final AtomicInteger jobsCompleted = new AtomicInteger(0);
    private final AtomicInteger piecesPlaced = new AtomicInteger(0);
    private final AtomicLong backtrackCount = new AtomicLong(0);
    private final AtomicInteger bestScore = new AtomicInteger(0);
    private final AtomicLong computeTimeMs = new AtomicLong(0);

    // Total stats (persistent)
    private final AtomicLong totalJobsCompleted = new AtomicLong(0);
    private final AtomicLong totalPiecesPlaced = new AtomicLong(0);
    private final AtomicLong totalBacktrackCount = new AtomicLong(0);
    private final AtomicLong totalComputeTimeMs = new AtomicLong(0);

    private BoardPrimitive bestBoard;
    private final long startTime;

    public ClientStatistics() {
        this.startTime = System.currentTimeMillis();
    }

    public void incrementJobsCompleted() {
        jobsCompleted.incrementAndGet();
        totalJobsCompleted.incrementAndGet();
    }

    public void incrementPiecesPlaced(int count) {
        piecesPlaced.addAndGet(count);
        totalPiecesPlaced.addAndGet(count);
    }

    public void incrementBacktrackCount() {
        backtrackCount.incrementAndGet();
        totalBacktrackCount.incrementAndGet();
    }

    public void addComputeTime(long milliseconds) {
        computeTimeMs.addAndGet(milliseconds);
        totalComputeTimeMs.addAndGet(milliseconds);
    }

    public synchronized void updateBestBoard(BoardPrimitive board) {
        int score = board.computeScore();
        if (score > bestScore.get()) {
            bestScore.set(score);
            this.bestBoard = board;
        }
    }

    // Session Getters
    public int getJobsCompleted() {
        return jobsCompleted.get();
    }

    public int getPiecesPlaced() {
        return piecesPlaced.get();
    }

    public long getBacktrackCount() {
        return backtrackCount.get();
    }

    public int getBestScore() {
        return bestScore.get();
    }

    public long getComputeTimeMs() {
        return computeTimeMs.get();
    }

    public synchronized BoardPrimitive getBestBoard() {
        return bestBoard;
    }

    public long getUptimeMs() {
        return System.currentTimeMillis() - startTime;
    }

    public double getPiecesPerSecond() {
        long uptimeSec = getUptimeMs() / 1000;
        return uptimeSec > 0 ? (double) piecesPlaced.get() / uptimeSec : 0.0;
    }

    // Total Getters
    public long getTotalJobsCompleted() {
        return totalJobsCompleted.get();
    }

    public long getTotalPiecesPlaced() {
        return totalPiecesPlaced.get();
    }

    public long getTotalBacktrackCount() {
        return totalBacktrackCount.get();
    }

    public long getTotalComputeTimeMs() {
        return totalComputeTimeMs.get();
    }

    // Persistence
    public void save(File file) {
        Properties props = new Properties();
        props.setProperty("totalJobsCompleted", String.valueOf(totalJobsCompleted.get()));
        props.setProperty("totalPiecesPlaced", String.valueOf(totalPiecesPlaced.get()));
        props.setProperty("totalBacktrackCount", String.valueOf(totalBacktrackCount.get()));
        props.setProperty("totalComputeTimeMs", String.valueOf(totalComputeTimeMs.get()));

        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "Eternity Client Statistics");
        } catch (IOException e) {
            logger.error("Failed to save client statistics", e);
        }
    }

    public void load(File file) {
        if (!file.exists())
            return;
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            totalJobsCompleted.set(Long.parseLong(props.getProperty("totalJobsCompleted", "0")));
            totalPiecesPlaced.set(Long.parseLong(props.getProperty("totalPiecesPlaced", "0")));
            totalBacktrackCount.set(Long.parseLong(props.getProperty("totalBacktrackCount", "0")));
            totalComputeTimeMs.set(Long.parseLong(props.getProperty("totalComputeTimeMs", "0")));
        } catch (IOException | NumberFormatException e) {
            logger.error("Failed to load client statistics", e);
        }
    }
}
