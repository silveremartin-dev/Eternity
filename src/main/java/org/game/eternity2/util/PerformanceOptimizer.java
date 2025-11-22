package org.game.eternity2.util;

import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.EternityTileInterface;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance optimizations for critical components.
 * Provides caching and efficient algorithms for heavy operations.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class PerformanceOptimizer {

    /**
     * Cache for tile compatibility checks.
     * Key: "tile1_tile2_orientation", Value: compatibility result
     */
    private static final Map<String, Boolean> compatibilityCache = new ConcurrentHashMap<>();

    /**
     * Cache for board scores.
     * Key: board hash, Value: score
     */
    private static final Map<Integer, Integer> scoreCache = new ConcurrentHashMap<>();

    /**
     * Check if two tiles are compatible (with caching).
     *
     * @param tile1       First tile
     * @param tile2       Second tile
     * @param orientation Orientation (0=top, 1=right, 2=bottom, 3=left)
     * @return true if compatible
     */
    public static boolean areCompatible(EternityTileInterface tile1, EternityTileInterface tile2, int orientation) {
        String cacheKey = String.format("%d_%d_%d",
                tile1.getBackValue(),
                tile2.getBackValue(),
                orientation);

        return compatibilityCache.computeIfAbsent(cacheKey, k -> {
            // Actual compatibility check
            return switch (orientation) {
                case 0 -> tile1.getTopValue() == tile2.getBottomValue(); // top
                case 1 -> tile1.getRightValue() == tile2.getLeftValue(); // right
                case 2 -> tile1.getBottomValue() == tile2.getTopValue(); // bottom
                case 3 -> tile1.getLeftValue() == tile2.getRightValue(); // left
                default -> false;
            };
        });
    }

    /**
     * Get cached board score.
     *
     * @param board Board to score
     * @return Cached or computed score
     */
    public static int getCachedScore(EternityBoardInterface board) {
        int hash = board.hashCode();
        return scoreCache.computeIfAbsent(hash, k -> board.computeScore());
    }

    /**
     * Clear all caches (useful for memory management).
     */
    public static void clearCaches() {
        compatibilityCache.clear();
        scoreCache.clear();
    }

    /**
     * Get cache statistics.
     *
     * @return Map with cache sizes
     */
    public static Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("compatibilityCache", compatibilityCache.size());
        stats.put("scoreCache", scoreCache.size());
        return stats;
    }

    /**
     * Optimize tile list by pre-sorting based on edge patterns.
     * Tiles with rare patterns should be tried first.
     *
     * @param tiles List of tiles
     * @return Optimized list
     */
    public static List<EternityTileInterface> optimizeTileOrder(List<EternityTileInterface> tiles) {
        // Count edge pattern frequencies
        Map<Integer, Integer> edgeFrequency = new HashMap<>();

        for (EternityTileInterface tile : tiles) {
            edgeFrequency.merge(tile.getTopValue(), 1, Integer::sum);
            edgeFrequency.merge(tile.getRightValue(), 1, Integer::sum);
            edgeFrequency.merge(tile.getBottomValue(), 1, Integer::sum);
            edgeFrequency.merge(tile.getLeftValue(), 1, Integer::sum);
        }

        // Sort tiles by rarity (sum of edge frequencies)
        List<EternityTileInterface> optimized = new ArrayList<>(tiles);
        optimized.sort((t1, t2) -> {
            int freq1 = edgeFrequency.getOrDefault(t1.getTopValue(), 0) +
                    edgeFrequency.getOrDefault(t1.getRightValue(), 0) +
                    edgeFrequency.getOrDefault(t1.getBottomValue(), 0) +
                    edgeFrequency.getOrDefault(t1.getLeftValue(), 0);

            int freq2 = edgeFrequency.getOrDefault(t2.getTopValue(), 0) +
                    edgeFrequency.getOrDefault(t2.getRightValue(), 0) +
                    edgeFrequency.getOrDefault(t2.getBottomValue(), 0) +
                    edgeFrequency.getOrDefault(t2.getLeftValue(), 0);

            return Integer.compare(freq1, freq2); // Rarest first
        });

        return optimized;
    }
}
