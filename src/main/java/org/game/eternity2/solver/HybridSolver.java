package org.game.eternity2.solver;

import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import org.game.eternity2.io.PuzzleLoaderWriter;

/**
 * Hybrid solver that combines Backtracking and Stochastic Search.
 */
public class HybridSolver implements EternitySolverInterface {

    private long[] allPieces;
    private int currentWidth = 16;
    private int currentHeight = 16;
    private EternitySolverEngine backtracker;
    private StochasticRefinement stochastic;
    private org.game.eternity2.client.ClientStatistics statistics;
    private long currentSeed = 42L;

    public HybridSolver() {
        this.allPieces = PuzzleLoaderWriter.generateEternity2Pieces();
        reinitSolvers(allPieces, 16, 16);
    }

    public void setStatistics(org.game.eternity2.client.ClientStatistics stats) {
        this.statistics = stats;
        if (backtracker != null)
            backtracker.setStatistics(stats);
        if (stochastic != null)
            stochastic.setStatistics(stats);
    }

    public void setSeed(long seed) {
        this.currentSeed = seed;
        if (stochastic != null) {
            stochastic.setSeed(seed);
        }
    }

    public void setPieces(long[] pieces) {
        if (pieces != null && pieces.length > 0) {
            this.allPieces = pieces;
            reinitSolvers(pieces, currentWidth, currentHeight);
        }
    }

    private void reinitSolvers(long[] pieces, int w, int h) {
        this.currentWidth = w;
        this.currentHeight = h;
        this.backtracker = new EternitySolverEngine(pieces, w, h);
        this.stochastic = new StochasticRefinement(w, h, currentSeed);
        if (statistics != null) {
            this.backtracker.setStatistics(statistics);
            this.stochastic.setStatistics(statistics);
        }
    }

    @Override
    public BoardPrimitive computeTessellation(BoardPrimitive startingBoard) {
        int w = startingBoard.getWidth();
        int h = startingBoard.getHeight();
        if (w != currentWidth || h != currentHeight) {
            System.out.println("Dimensions changed from " + currentWidth + "x" + currentHeight + " to " + w + "x" + h
                    + ". Reinitializing solvers...");
            reinitSolvers(allPieces, w, h);
        }
        System.out.println("Starting Hybrid Solver Search...");

        // 1. Try backtracking
        backtracker.loadState(startingBoard);
        backtracker.solve();

        BoardPrimitive bestBacktrack = backtracker.getBestBoard();
        int backtrackingBest = backtracker.getBestScore();
        System.out.println("Backtracking finished. Best score: " + backtrackingBest);

        // 2. Stochastic refinement if board is not 100% solved
        int maxScore = (w - 1) * h + w * (h - 1) + 2 * w + 2 * h;
        if (backtrackingBest < maxScore) {
            System.out.println("Switching to Stochastic Refinement...");
            stochastic.initializeFromBoard(bestBacktrack, allPieces);
            stochastic.refine(1000000);

            BoardPrimitive bestStochastic = stochastic.getBestBoard();
            if (bestStochastic.computeScore() > backtrackingBest) {
                return bestStochastic;
            }
        }

        return bestBacktrack;
    }

    public long getTotalBacktracks() {
        return backtracker != null ? backtracker.getIterations() : 0;
    }
}
