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
package org.game.eternity2.server.kernel;

import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;

/**
 * Driver to execute the EternityKernel on GPU using TornadoVM.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class TornadoEternityDriver {

    private final TaskGraph taskGraph;
    private final TornadoExecutionPlan executionPlan;

    private final int[] constraints;
    private final int[] candidates;
    private final int[] results;

    public TornadoEternityDriver(int maxCandidates) {
        this.constraints = new int[4];
        this.candidates = new int[maxCandidates * 4];
        this.results = new int[maxCandidates];

        this.taskGraph = new TaskGraph("eternity")
                .transferToDevice(DataTransferMode.EVERY_EXECUTION, constraints, candidates)
                .task("check", EternityKernel::checkCandidates, constraints, candidates, results)
                .transferToHost(DataTransferMode.EVERY_EXECUTION, results);

        ImmutableTaskGraph immutableTaskGraph = taskGraph.snapshot();
        this.executionPlan = new TornadoExecutionPlan(immutableTaskGraph);
    }

    public void solve(int[] currentConstraints, int[] currentCandidates, int[] currentResults) {
        // Copy to internal arrays (TornadoVM works best with fixed buffers)
        System.arraycopy(currentConstraints, 0, constraints, 0, 4);
        System.arraycopy(currentCandidates, 0, candidates, 0, currentResults.length * 4);

        // Execute on GPU
        executionPlan.execute();

        // Copy results back
        System.arraycopy(results, 0, currentResults, 0, currentResults.length);
    }
}
