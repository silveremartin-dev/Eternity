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
import org.game.eternity2.model.Hint;

import java.util.List;

/**
 * Strategy for dividing puzzle-solving work into jobs.
 * Different strategies explore the search space in different orders.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.1
  * @author Antigravity
  * @since 1.0
 */
public interface WorkStrategy {

    /**
     * Generate a list of jobs for solving the given puzzle.
     *
     * @param puzzle The puzzle to solve
     * @param hints  Pre-placed tiles (constraints)
     * @return List of jobs that cover the entire search space without overlap
     */
    List<Job> generateJobs(BoardPrimitive puzzle, List<Hint> hints);

    /**
     * Get the name of this strategy.
     *
     * @return Strategy name
     */
    String getName();
}
