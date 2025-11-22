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

import org.game.eternity2.elements.EternityBoardInterface;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks client statistics for monitoring and display.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class ClientStatistics {
    private final AtomicInteger jobsCompleted = new AtomicInteger(0);
    private final AtomicInteger piecesPlaced = new AtomicInteger(0);
    private final AtomicLong backtrackCount = new AtomicLong(0);
    private final AtomicInteger bestScore = new AtomicInteger(0);
    private final AtomicLong computeTimeMs = new AtomicLong(0);
    private EternityBoardInterface bestBoard;
    private final long startTime;

    public ClientStatistics() {
        this.startTime = System.currentTimeMillis();
    }

    public void incrementJobsCompleted() {
        jobsCompleted.incrementAndGet();
    }

    public void incrementPiecesPlaced(int count) {
        piecesPlaced.addAndGet(count);
    }

    public void incrementBacktrackCount() {
        backtrackCount.incrementAndGet();
    }

    public void addComputeTime(long milliseconds) {
        computeTimeMs.addAndGet(milliseconds);
    }

    public synchronized void updateBestBoard(EternityBoardInterface board) {
        int score = board.computeScore();
        if (score > bestScore.get()) {
            bestScore.set(score);
            this.bestBoard = board;
        }
    }

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

    public synchronized EternityBoardInterface getBestBoard() {
        return bestBoard;
    }

    public long getUptimeMs() {
        return System.currentTimeMillis() - startTime;
    }

    public double getPiecesPerSecond() {
        long uptimeSec = getUptimeMs() / 1000;
        return uptimeSec > 0 ? (double) piecesPlaced.get() / uptimeSec : 0.0;
    }
}
