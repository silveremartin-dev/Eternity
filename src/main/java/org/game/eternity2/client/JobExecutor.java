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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import org.game.eternity2.io.PuzzleLoaderWriter;
import org.game.eternity2.server.Job;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Executes jobs using backtracking algorithm with tile rotation.
 * Optimized version using primitive models.
 *
 * @author Silvere Martin-Michiellot
 * @version 3.0
  * @author Antigravity
  * @since 1.0
 */
public class JobExecutor {
    private static final Logger logger = LogManager.getLogger(JobExecutor.class);

    private final ClientStatistics statistics;
    private final long[] allPieces;
    private volatile boolean cancelled = false;

    public JobExecutor(ClientStatistics statistics) {
        this.statistics = statistics;
        this.allPieces = PuzzleLoaderWriter.generateEternity2Pieces();
    }

    public BoardPrimitive executeJob(Job job) {
        cancelled = false;
        long startTime = System.currentTimeMillis();

        BoardPrimitive board = job.getInitialBoard();
        List<Job.Position> positions = job.getPositionsToFill();

        logger.info("Starting job {}: {} positions to fill", job.getJobId(), positions.size());

        // Track used piece IDs
        Set<Integer> usedIds = new HashSet<>();
        for (long cell : board.getCells()) {
            if (cell != 0) {
                usedIds.add(PiecePrimitive.getId(cell));
            }
        }

        // Available pieces list
        List<Long> availableList = new ArrayList<>();
        for (long p : allPieces) {
            if (!usedIds.contains(PiecePrimitive.getId(p))) {
                availableList.add(p);
            }
        }

        BoardPrimitive result = backtrack(board, positions, availableList, 0);

        long elapsed = System.currentTimeMillis() - startTime;
        statistics.addComputeTime(elapsed);
        statistics.incrementJobsCompleted();

        if (result != null) {
            statistics.updateBestBoard(result);
            logger.info("Job {} completed: score={}, time={}ms",
                    job.getJobId(), result.computeScore(), elapsed);
        } else {
            logger.info("Job {} completed: no solution found, time={}ms", job.getJobId(), elapsed);
        }

        return result;
    }

    private BoardPrimitive backtrack(BoardPrimitive board,
            List<Job.Position> positions,
            List<Long> availableTiles,
            int positionIndex) {
        if (cancelled) {
            return null;
        }

        if (positionIndex >= positions.size()) {
            return board;
        }

        Job.Position pos = positions.get(positionIndex);
        int row = pos.getRow();
        int col = pos.getCol();

        int[] constraints = board.getConstraints(col, row);

        for (int i = 0; i < availableTiles.size(); i++) {
            long piece = availableTiles.get(i);

            // Try each rotation
            for (int rotation = 0; rotation < 4; rotation++) {
                if (PiecePrimitive.matches(piece, constraints[0], constraints[1], constraints[2], constraints[3])) {
                    board.placePiece(col, row, piece);
                    statistics.incrementPiecesPlaced(1);

                    Long removed = availableTiles.remove(i);
                    BoardPrimitive result = backtrack(board, positions, availableTiles, positionIndex + 1);

                    if (result != null) {
                        return result;
                    }

                    statistics.incrementBacktrackCount();
                    board.removePiece(col, row);
                    availableTiles.add(i, removed);
                }
                piece = PiecePrimitive.rotateCW(piece);
            }
        }

        return null;
    }

    public void cancel() {
        cancelled = true;
    }
}
