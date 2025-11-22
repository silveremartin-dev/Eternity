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
import org.game.eternity2.elements.Hint;

import java.util.List;

/**
 * Strategy for dividing puzzle-solving work into jobs.
 * Different strategies explore the search space in different orders.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public interface WorkStrategy {

    /**
     * Generate a list of jobs for solving the given puzzle.
     *
     * @param puzzle The puzzle to solve
     * @param hints  Pre-placed tiles (constraints)
     * @return List of jobs that cover the entire search space without overlap
     */
    List<Job> generateJobs(EternityBoardInterface puzzle, List<Hint> hints);

    /**
     * Get the name of this strategy.
     *
     * @return Strategy name
     */
    String getName();
}
