package org.game.eternity2.server.kernel;

import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;

/**
 * Driver to execute the EternityKernel on GPU using TornadoVM.
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
