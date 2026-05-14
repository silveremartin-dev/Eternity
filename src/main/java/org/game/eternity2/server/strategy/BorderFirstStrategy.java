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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Border-first strategy: starts by placing edge pieces first.
 * Generates jobs that explore different edge configurations.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.2
 * @author Antigravity
 * @since 1.0
 */
public class BorderFirstStrategy implements WorkStrategy {

    @Override
    public List<Job> generateJobs(BoardPrimitive puzzle, List<Hint> hints) {
        List<Job> jobs = new ArrayList<>();
        int width = puzzle.getWidth();
        int height = puzzle.getHeight();

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
                if (!isHintPosition(row, col, hints) && puzzle.getPiece(col, row) == 0) {
                    interiorPositions.add(new Job.Position(row, col));
                }
            }
        }

        // Generate many jobs for parallelism
        // Find first empty position
        boolean found = false;
        for (int r = 0; r < height && !found; r++) {
            for (int c = 0; c < width && !found; c++) {
                if (!isHintPosition(r, c, hints) && puzzle.getPiece(c, r) == 0) {
                    found = true;
                }
            }
        }

        if (!found) {
            // All filled? Just one interior job
            jobs.add(new Job("FINAL_" + UUID.randomUUID().toString().substring(0, 8), puzzle, interiorPositions, getName()));
            return jobs;
        }

        int jobCount = 32; // Create 32 jobs
        for (int i = 0; i < jobCount; i++) {
            String jobId = "JOB_" + i + "_" + UUID.randomUUID().toString().substring(0, 8);
            // Each job takes a slice of the interior positions
            List<Job.Position> jobPositions = new ArrayList<>(borderPositions);
            jobPositions.addAll(interiorPositions);
            
            // To make jobs truly different, we could shuffle or offset the start
            // But for now, let's just distribute the interior differently
            if (!interiorPositions.isEmpty()) {
                Collections.rotate(interiorPositions, Math.max(1, interiorPositions.size() / jobCount));
            }
            
            jobs.add(new Job(jobId, puzzle, jobPositions, getName()));
        }

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
