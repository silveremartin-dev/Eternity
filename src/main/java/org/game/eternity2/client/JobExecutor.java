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
package org.game.eternity2.client;

import org.game.eternity2.solver.HybridSolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import org.game.eternity2.io.PuzzleLoaderWriter;
import org.game.eternity2.server.Job;
import org.game.eternity2.solver.GPUEternitySolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Executes jobs using backtracking algorithm with tile rotation.
 * Optimized version using primitive models.
 *
 * @author Silvere Martin-Michiellot
 * @version 3.0
  * @author Antigravity
  * @since 1.0
 */
public class JobExecutor {
    private static final Logger logger = LogManager.getLogger(JobExecutor.class);

    private final ClientStatistics statistics;
    private long[] allPieces;
    private volatile boolean cancelled = false;
    private boolean useGPU = false;
    private GPUEternitySolver gpuSolver;
    private final HybridSolver hybridSolver;

    public JobExecutor(ClientStatistics statistics) {
        this.statistics = statistics;
        this.allPieces = PuzzleLoaderWriter.generateEternity2Pieces();
        this.hybridSolver = new HybridSolver();
        this.hybridSolver.setStatistics(statistics);
        try {
            this.gpuSolver = new GPUEternitySolver(1024);
        } catch (Throwable e) {
            logger.warn("TornadoVM not available, GPU acceleration disabled: {}", e.getMessage());
        }
    }

    public void setAllPieces(long[] pieces) {
        if (pieces != null && pieces.length > 0) {
            this.allPieces = pieces;
            logger.info("Updated piece library with {} pieces", pieces.length);
        }
    }

    public void setUseGPU(boolean useGPU) {
        this.useGPU = useGPU && (gpuSolver != null);
    }

    public BoardPrimitive executeJob(Job job) {
        cancelled = false;
        long startTime = System.currentTimeMillis();
        long startBacktracks = hybridSolver.getTotalBacktracks();

        BoardPrimitive board = job.getInitialBoard();
        logger.info("Starting job {}: Hybrid solving mode", job.getJobId());

        BoardPrimitive result = hybridSolver.computeTessellation(board);

        long elapsed = System.currentTimeMillis() - startTime;
        long backtracksDone = hybridSolver.getTotalBacktracks() - startBacktracks;
        
        statistics.addComputeTime(elapsed);
        statistics.addBacktracks(backtracksDone);
        statistics.incrementJobsCompleted();
        
        // Update PPS based on backtracks for hybrid engine
        if (elapsed > 0) {
            int pps = (int) (backtracksDone * 1000 / elapsed);
            statistics.setPiecesPerSecond(pps);
        }

        if (result != null) {
            statistics.updateBestBoard(result);
            logger.info("Job {} completed: score={}, pps={}, time={}ms",
                    job.getJobId(), result.computeScore(), statistics.getPiecesPerSecond(), elapsed);
        } else {
            logger.info("Job {} completed: no full solution found, time={}ms", job.getJobId(), elapsed);
        }

        return result;
    }

    public void cancel() {
        cancelled = true;
    }
}
