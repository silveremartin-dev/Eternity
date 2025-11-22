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

import org.game.eternity2.elements.EternityBoardInterface;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a work unit to be executed by a client.
 * Contains the initial board state and exact instructions for what to solve.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class Job implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String jobId;
    private final EternityBoardInterface initialBoard;
    private final List<Position> positionsToFill;
    private final String strategyName;
    private final long createdTimestamp;

    /**
     * Create a new job.
     *
     * @param jobId           Unique identifier for this job
     * @param initialBoard    Starting board state (may have some tiles placed)
     * @param positionsToFill List of positions to fill in order
     * @param strategyName    Name of the strategy being used
     */
    public Job(String jobId, EternityBoardInterface initialBoard,
            List<Position> positionsToFill, String strategyName) {
        this.jobId = jobId;
        this.initialBoard = initialBoard;
        this.positionsToFill = positionsToFill;
        this.strategyName = strategyName;
        this.createdTimestamp = System.currentTimeMillis();
    }

    public String getJobId() {
        return jobId;
    }

    public EternityBoardInterface getInitialBoard() {
        return initialBoard;
    }

    public List<Position> getPositionsToFill() {
        return positionsToFill;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    /**
     * Represents a position on the board.
     */
    public static class Position implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int row;
        private final int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        @Override
        public String toString() {
            return String.format("(%d,%d)", row, col);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Position))
                return false;
            Position position = (Position) o;
            return row == position.row && col == position.col;
        }

        @Override
        public int hashCode() {
            return 31 * row + col;
        }
    }

    @Override
    public String toString() {
        return String.format("Job[%s, strategy=%s, positions=%d]",
                jobId, strategyName, positionsToFill.size());
    }
}
