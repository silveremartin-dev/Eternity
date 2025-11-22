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

package org.game.eternity2.server.strategy;

import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.Hint;
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
 * @version 2.0
 */
public class BorderFirstStrategy implements WorkStrategy {

    @Override
    public List<Job> generateJobs(EternityBoardInterface puzzle, List<Hint> hints) {
        List<Job> jobs = new ArrayList<>();
        int size = puzzle.getXBoardSize(); // Assuming square board

        // For now, create a simple job that fills positions row by row
        // Starting with borders (top row, bottom row, left column, right column)
        List<Job.Position> positions = new ArrayList<>();

        // Top row
        for (int col = 0; col < size; col++) {
            if (!isHintPosition(0, col, hints)) {
                positions.add(new Job.Position(0, col));
            }
        }

        // Bottom row
        for (int col = 0; col < size; col++) {
            if (!isHintPosition(size - 1, col, hints)) {
                positions.add(new Job.Position(size - 1, col));
            }
        }

        // Left column (excluding corners already added)
        for (int row = 1; row < size - 1; row++) {
            if (!isHintPosition(row, 0, hints)) {
                positions.add(new Job.Position(row, 0));
            }
        }

        // Right column (excluding corners already added)
        for (int row = 1; row < size - 1; row++) {
            if (!isHintPosition(row, size - 1, hints)) {
                positions.add(new Job.Position(row, size - 1));
            }
        }

        // Interior positions
        for (int row = 1; row < size - 1; row++) {
            for (int col = 1; col < size - 1; col++) {
                if (!isHintPosition(row, col, hints)) {
                    positions.add(new Job.Position(row, col));
                }
            }
        }

        // Create a single job for now (in production, this would be split into multiple
        // jobs)
        String jobId = "BORDER_" + UUID.randomUUID().toString().substring(0, 8);
        jobs.add(new Job(jobId, puzzle, positions, getName()));

        return jobs;
    }

    private boolean isHintPosition(int row, int col, List<Hint> hints) {
        for (Hint hint : hints) {
            if (hint.getRow() == row && hint.getCol() == col) {
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
