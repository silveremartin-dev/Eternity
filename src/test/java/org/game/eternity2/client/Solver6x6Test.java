package org.game.eternity2.client;

import org.game.eternity2.elements.BoardFactory;
import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.server.Job;
import org.game.eternity2.server.strategy.BorderFirstStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Solver6x6Test {

    @Test
    public void testSolve6x6() {
        // 1. Create a 6x6 board
        EternityBoardInterface board = BoardFactory.createSquareBoard(6);

        // 2. Create a job that covers the whole board (or a significant part)
        // For simplicity, let's try to solve the whole thing or check if it finds *any*
        // valid placement
        // effectively simulating a job.

        // We need a strategy to generate a job.
        BorderFirstStrategy strategy = new BorderFirstStrategy();
        // We need to mock hints or pass empty hints
        List<Job> jobs = strategy.generateJobs(board, new ArrayList<>());

        // There should be at least one job (Border or Interior)
        assertTrue(jobs.size() > 0);

        Job job = jobs.get(0); // Take the first job (likely Border)

        // 3. Run the solver
        ClientStatistics stats = new ClientStatistics();
        JobExecutor executor = new JobExecutor(stats);

        System.out.println("Starting 6x6 Solver Test...");
        System.out.println("Positions to fill: " + job.getPositionsToFill().size());
        System.out.println("Available tiles: " + board.getMissingTiles().size());
        System.out.println("Job type: " + job.getJobId());

        long start = System.currentTimeMillis();
        EternityBoardInterface result = executor.executeJob(job);
        long end = System.currentTimeMillis();

        System.out.println("Solver finished in " + (end - start) + "ms");
        if (result != null) {
            System.out.println("Solution found! Score: " + result.computeScore());
        } else {
            System.out.println("No solution found.");
            System.out.println("Backtrack count: " + stats.getBacktrackCount());
            System.out.println("Pieces placed: " + stats.getPiecesPlaced());
        }

        // 4. Assert that a result was found (it shouldn't be null if solvable)
        assertNotNull(result, "Solver returned null, meaning no solution found.");
    }
}
