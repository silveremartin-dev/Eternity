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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.EternityTileInterface;
import org.game.eternity2.server.Job;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Executes jobs using backtracking algorithm with tile rotation.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class JobExecutor {
    private static final Logger logger = LogManager.getLogger(JobExecutor.class);

    private final ClientStatistics statistics;
    private volatile boolean cancelled = false;

    public JobExecutor(ClientStatistics statistics) {
        this.statistics = statistics;
    }

    public EternityBoardInterface executeJob(Job job) {
        cancelled = false;
        long startTime = System.currentTimeMillis();

        EternityBoardInterface board = job.getInitialBoard();
        List<Job.Position> positions = job.getPositionsToFill();

        logger.info("Starting job {}: {} positions to fill", job.getJobId(), positions.size());

        Set<EternityTileInterface> availableTiles = board.getMissingTiles();
        List<EternityTileInterface> tilesList = new ArrayList<>(availableTiles);

        EternityBoardInterface result = backtrack(board, positions, tilesList, 0);

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

    private EternityBoardInterface backtrack(EternityBoardInterface board,
            List<Job.Position> positions,
            List<EternityTileInterface> availableTiles,
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

        for (int i = 0; i < availableTiles.size(); i++) {
            EternityTileInterface tile = availableTiles.get(i);
            if (tile == null)
                continue;

            // Try each rotation
            for (int rotation = 0; rotation < 4; rotation++) {
                // Rotate tile
                for (int r = 0; r < rotation; r++) {
                    tile.rotateClockwise();
                }

                if (canPlaceTile(board, row, col, tile)) {
                    board.setTileAtNoCheck(row, col, tile);
                    statistics.incrementPiecesPlaced(1);

                    availableTiles.remove(i);

                    EternityBoardInterface result = backtrack(board, positions, availableTiles, positionIndex + 1);

                    if (result != null) {
                        return result;
                    }

                    statistics.incrementBacktrackCount();
                    board.setTileAtNoCheck(row, col, null);
                    availableTiles.add(i, tile);
                }

                // Rotate back
                for (int r = 0; r < (4 - rotation); r++) {
                    tile.rotateClockwise();
                }
            }
        }

        return null;
    }

    private boolean canPlaceTile(EternityBoardInterface board, int row, int col, EternityTileInterface tile) {
        if (tile == null)
            return false;
        if (board.isBorder(row, col)) {
            if (!board.areBordersMatchingForBorderTile(row, col, tile)) {
                return false;
            }
        } else {
            if (!board.areBordersMatchingForInBoardTile(tile)) {
                return false;
            }
        }

        return board.areNeighborsMatching(row, col, tile);
    }

    public void cancel() {
        cancelled = true;
    }
}
