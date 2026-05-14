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

/**
 * Scanline strategy: fills positions row by row, column by column.
 * Generates jobs by fixing the first few positions.
 *
 * @author Silvere Martin-Michiellot
 * @author Antigravity
 * @since 1.0
 */
public class ScanlineStrategy implements WorkStrategy {

    private final int depth;

    public ScanlineStrategy() {
        this(2); // Default depth
    }

    public ScanlineStrategy(int depth) {
        this.depth = depth;
    }

    @Override
    public List<Job> generateJobs(BoardPrimitive puzzle, List<Hint> hints) {
        List<Job> jobs = new ArrayList<>();
        
        // Find first N empty positions
        List<Job.Position> scanOrder = new ArrayList<>();
        for (int y = 0; y < puzzle.getHeight(); y++) {
            for (int x = 0; x < puzzle.getWidth(); x++) {
                boolean isHint = false;
                for (Hint h : hints) {
                    if (h.row() == y && h.col() == x) {
                        isHint = true;
                        break;
                    }
                }
                if (!isHint) {
                    scanOrder.add(new Job.Position(y, x));
                }
            }
        }

        // The jobs will cover the remaining positions
        List<Job.Position> remainingPositions = new ArrayList<>();
        if (scanOrder.size() > depth) {
            remainingPositions.addAll(scanOrder.subList(depth, scanOrder.size()));
        }

        // Simplification: generate a single job with all positions for now
        // To properly split into multiple jobs, we would need to iterate through
        // possible pieces for the first 'depth' positions.
        // For now, let's just provide the full scanline order.
        
        Job job = new Job("scanline-0", new BoardPrimitive(puzzle), scanOrder, getName());
        jobs.add(job);
        
        return jobs;
    }

    @Override
    public String getName() {
        return "Scanline";
    }
}
