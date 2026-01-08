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
package org.game.eternity2.server.strategy;

import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.Hint;
import org.game.eternity2.server.Job;
import org.game.eternity2.server.WorkStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Border-first strategy: starts by placing edge pieces first.
 * Generates jobs that explore different edge configurations.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.1
  * @author Antigravity
  * @since 1.0
 */
public class BorderFirstStrategy implements WorkStrategy {

    @Override
    public List<Job> generateJobs(BoardPrimitive puzzle, List<Hint> hints) {
        List<Job> jobs = new ArrayList<>();
        int width = puzzle.getWidth();
        int height = puzzle.getHeight();

        // For now, create a simple job that fills positions row by row
        // Starting with borders (top row, bottom row, left column, right column)
        // Split into two jobs: Border and Interior
        List<Job.Position> borderPositions = new ArrayList<>();
        List<Job.Position> interiorPositions = new ArrayList<>();

        // Top row
        for (int col = 0; col < width; col++) {
            if (!isHintPosition(0, col, hints))
                borderPositions.add(new Job.Position(0, col));
        }
        // Bottom row
        for (int col = 0; col < width; col++) {
            if (!isHintPosition(height - 1, col, hints))
                borderPositions.add(new Job.Position(height - 1, col));
        }
        // Left column
        for (int row = 1; row < height - 1; row++) {
            if (!isHintPosition(row, 0, hints))
                borderPositions.add(new Job.Position(row, 0));
        }
        // Right column
        for (int row = 1; row < height - 1; row++) {
            if (!isHintPosition(row, width - 1, hints))
                borderPositions.add(new Job.Position(row, width - 1));
        }

        // Interior
        for (int row = 1; row < height - 1; row++) {
            for (int col = 1; col < width - 1; col++) {
                if (!isHintPosition(row, col, hints)) {
                    interiorPositions.add(new Job.Position(row, col));
                }
            }
        }

        String jobId1 = "BORDER_" + UUID.randomUUID().toString().substring(0, 8);
        jobs.add(new Job(jobId1, puzzle, borderPositions, getName()));

        String jobId2 = "INTERIOR_" + UUID.randomUUID().toString().substring(0, 8);
        jobs.add(new Job(jobId2, puzzle, interiorPositions, getName()));

        return jobs;
    }

    private boolean isHintPosition(int row, int col, List<Hint> hints) {
        for (Hint hint : hints) {
            if (hint.row() == row && hint.col() == col) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "BORDER_FIRST";
    }
}
