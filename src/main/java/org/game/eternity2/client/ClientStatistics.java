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

package org.game.eternity2.client;

import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;

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
