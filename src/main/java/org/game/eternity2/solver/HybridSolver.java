package org.game.eternity2.solver;

import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import org.game.eternity2.io.PuzzleLoaderWriter;

/**
 * Hybrid solver that combines Backtracking and Stochastic Search.
 */
public class HybridSolver implements EternitySolverInterface {

    private final long[] allPieces;
    private final EternitySolverEngine backtracker;
    private final StochasticRefinement stochastic;

    public HybridSolver() {
        this.allPieces = PuzzleLoaderWriter.generateEternity2Pieces();
        this.backtracker = new EternitySolverEngine(allPieces, 16, 16);
        this.stochastic = new StochasticRefinement(16, 16);
    }

    @Override
    public BoardPrimitive computeTessellation(BoardPrimitive startingBoard) {
        System.out.println("Starting Hybrid Solver Search...");
        
        // 1. Try backtracking for a limited time/iterations
        backtracker.loadState(startingBoard);
        backtracker.solve(); // Runs until exhausted or best score found

        int backtrackingBest = backtracker.getBestScore();
        System.out.println("Backtracking finished. Best score: " + backtrackingBest);

        // 2. If not solved, try stochastic refinement around the best configuration
        if (backtrackingBest < 256) {
            System.out.println("Switching to Stochastic Refinement...");
            stochastic.initializeRandom(allPieces);
            stochastic.refine(1000000); // 1M iterations of local search
        }

        return null; // The actual results are stored in stats and logs for now
    }
}
