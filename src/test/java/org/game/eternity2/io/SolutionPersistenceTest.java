package org.game.eternity2.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SolutionPersistence.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
class SolutionPersistenceTest {

    @Test
    void testSaveSolution(@TempDir Path tempDir) {
        EternityBoard16x16 board = new EternityBoard16x16();
        int score = 100;

        Path savedPath = SolutionPersistence.saveSolution(board, score);

        assertNotNull(savedPath, "Should return a path");
        assertTrue(savedPath.toString().contains("solution_"), "Filename should contain 'solution_'");
        assertTrue(savedPath.toString().endsWith(".csv"), "Should be a CSV file");
    }

    @Test
    void testListSolutions() {
        List<Path> solutions = SolutionPersistence.listSolutions();

        assertNotNull(solutions, "Should return a list");
        // List may be empty if no solutions saved yet
    }

    @Test
    void testTilePlacement() {
        SolutionPersistence.TilePlacement placement = new SolutionPersistence.TilePlacement(1, 2, 42, 3);

        assertEquals(1, placement.getRow());
        assertEquals(2, placement.getCol());
        assertEquals(42, placement.getTileId());
        assertEquals(3, placement.getRotation());
    }
}
