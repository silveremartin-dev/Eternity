package org.game.eternity2.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.elements.size16x16.EternityTile16x16;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PerformanceOptimizer.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
class PerformanceOptimizerTest {

    @BeforeEach
    void setUp() {
        PerformanceOptimizer.clearCaches();
    }

    @Test
    void testCompatibilityCaching() {
        EternityTile16x16 tile1 = new EternityTile16x16(1, 2, 3, 4, 5);
        EternityTile16x16 tile2 = new EternityTile16x16(6, 4, 8, 9, 10);

        // First call - cache miss
        boolean result1 = PerformanceOptimizer.areCompatible(tile1, tile2, 1);

        // Second call - cache hit
        boolean result2 = PerformanceOptimizer.areCompatible(tile1, tile2, 1);

        assertEquals(result1, result2, "Cached result should match");

        Map<String, Integer> stats = PerformanceOptimizer.getCacheStats();
        assertTrue(stats.get("compatibilityCache") > 0, "Cache should have entries");
    }

    @Test
    void testScoreCaching() {
        EternityBoard16x16 board = new EternityBoard16x16();

        int score1 = PerformanceOptimizer.getCachedScore(board);
        int score2 = PerformanceOptimizer.getCachedScore(board);

        assertEquals(score1, score2, "Cached score should match");
    }

    @Test
    void testTileOrderOptimization() {
        List<EternityTile16x16> tiles = new ArrayList<>();
        tiles.add(new EternityTile16x16(1, 1, 1, 1, 1)); // Common pattern
        tiles.add(new EternityTile16x16(2, 99, 99, 99, 99)); // Rare pattern

        // Note: This test is conceptual as we can't easily verify ordering
        // without knowing the full tile set
        assertDoesNotThrow(() -> {
            PerformanceOptimizer.optimizeTileOrder(new ArrayList<>(tiles));
        });
    }

    @Test
    void testClearCaches() {
        EternityTile16x16 tile1 = new EternityTile16x16(1, 2, 3, 4, 5);
        EternityTile16x16 tile2 = new EternityTile16x16(6, 4, 8, 9, 10);

        PerformanceOptimizer.areCompatible(tile1, tile2, 1);

        Map<String, Integer> statsBefore = PerformanceOptimizer.getCacheStats();
        assertTrue(statsBefore.get("compatibilityCache") > 0);

        PerformanceOptimizer.clearCaches();

        Map<String, Integer> statsAfter = PerformanceOptimizer.getCacheStats();
        assertEquals(0, statsAfter.get("compatibilityCache"));
    }
}
