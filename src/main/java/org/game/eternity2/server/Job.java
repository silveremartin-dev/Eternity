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

import org.game.eternity2.model.BoardPrimitive;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a work unit to be executed by a client.
 * Contains the initial board state and exact instructions for what to solve.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class Job implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String jobId;
    private final BoardPrimitive initialBoard;
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
    public Job(String jobId, BoardPrimitive initialBoard,
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

    public BoardPrimitive getInitialBoard() {
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
