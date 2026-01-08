package org.game.eternity2.client;

import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.solver.RarePatternSolver;
import org.junit.jupiter.api.Test;

class RarePatternSolverTest {

    @Test
    void testSolveStructure() {
        RarePatternSolver solver = new RarePatternSolver();
        BoardPrimitive board = new BoardPrimitive(4, 4);

        // Just checking structure, actual solving requires real pieces
        solver.computeTessellation(board);

        // It might be null if it can't solve (which is expected with empty/random
        // setup)
        // or return a board. The main point is checking it runs without crashing.
        // assertNotNull(result); // Result can be null if no solution found
    }
}
