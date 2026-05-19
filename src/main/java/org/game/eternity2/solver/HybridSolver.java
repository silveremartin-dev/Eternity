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

    public void setStatistics(org.game.eternity2.client.ClientStatistics stats) {
        backtracker.setStatistics(stats);
        stochastic.setStatistics(stats);
    }

    @Override
    public BoardPrimitive computeTessellation(BoardPrimitive startingBoard) {
        System.out.println("Starting Hybrid Solver Search...");
        
        // 1. Try backtracking
        backtracker.loadState(startingBoard);
        backtracker.solve(); 

        BoardPrimitive bestBacktrack = backtracker.getBestBoard();
        int backtrackingBest = backtracker.getBestScore();
        System.out.println("Backtracking finished. Best score: " + backtrackingBest);

        // 2. Stochastic refinement
        if (backtrackingBest < (startingBoard.getWidth() * startingBoard.getHeight())) {
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
        return backtracker.getIterations();
    }
}
