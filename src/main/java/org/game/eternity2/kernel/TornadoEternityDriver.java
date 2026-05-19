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
package org.game.eternity2.kernel;

import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;

/**
 * Driver to execute the EternityKernel on GPU using TornadoVM.
 * Optimized for Java 25 with Execution Plans.
 * 
 * @author Silvere Martin-Michiellot
 * @author Antigravity
 * @since 1.0
 */
public class TornadoEternityDriver {

    private final TaskGraph taskGraph;
    private final TornadoExecutionPlan executionPlan;

    private final int[] packedConstraints;
    private final int[] mask;
    private final int[] candidates;
    private final int[] results;

    public TornadoEternityDriver(int[] piecesPool) {
        this.packedConstraints = new int[1];
        this.mask = new int[1];
        this.candidates = piecesPool;
        this.results = new int[piecesPool.length];

        this.taskGraph = new TaskGraph("eternity")
                .transferToDevice(DataTransferMode.EVERY_EXECUTION, packedConstraints, mask)
                .transferToDevice(DataTransferMode.FIRST_EXECUTION, candidates)
                .task("check", EternityKernel::checkCandidatesBitwise, packedConstraints, mask, candidates, results)
                .transferToHost(DataTransferMode.EVERY_EXECUTION, results);

        ImmutableTaskGraph immutableTaskGraph = taskGraph.snapshot();
        this.executionPlan = new TornadoExecutionPlan(immutableTaskGraph);
    }

    public void solve(int packedTarget, int activeMask, int[] currentResults) {
        this.packedConstraints[0] = packedTarget;
        this.mask[0] = activeMask;

        // Execute on GPU using the modern Execution Plan API
        executionPlan.execute();

        // Copy results back
        System.arraycopy(results, 0, currentResults, 0, results.length);
    }
}
