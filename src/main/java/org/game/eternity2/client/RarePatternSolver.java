package org.game.eternity2.client;

import org.game.eternity2.elements.AbstractEternityBoard;
import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.EternityTileInterface;

import java.util.*;

public class RarePatternSolver implements EternitySolverInterface {

    private static final Map<Integer, Integer> PATTERN_COUNTS = new HashMap<>();

    static {
        // Counts provided by user
        PATTERN_COUNTS.put(0, 64); // Gray
        PATTERN_COUNTS.put(1, 50); // OrangeLightBlue (Assuming mapping based on list order or name matching)
        // Wait, the user list order might not match IDs 1..22 exactly.
        // User list:
        // EternityBasicPatternGray: 64 (ID 0)
        // EternityBasicPatternLightBlue: 24 (Unknown ID, maybe 7 or 17?)
        // EternityBasicPatternRoseYellow2: 48 (ID 2)
        // ...

        // I need to map names to IDs carefully.
        // Let's use the names from EternityBasicPatterns16x16 to match user list.

        // ID 0: Gray - 64
        // ID 1: OrangeLightBlue - 50 (User: EternityBasicPatternOrangeLightBlue not
        // explicitly listed? Ah, "EternityBasicPatternOrangeLightBlue" is not in user
        // list?
        // User list has "EternityBasicPatternLightBlue",
        // "EternityBasicPatternRoseYellow2", etc.
        // Let's try to match by name suffix.

        // User list:
        // EternityBasicPatternGray: 64
        // EternityBasicPatternLightBlue: 24 -> ? Maybe ID 7 (LightBlueRose)? No.
        // Let's look at the user list again.
        /*
         * EternityBasicPatternGray: 64
         * EternityBasicPatternLightBlue: 24 <-- ID ?
         * EternityBasicPatternRoseYellow2: 48 <-- ID 2
         * EternityBasicPatternBrownGreen: 50 <-- ID 3
         * EternityBasicPatternLightBlueRose2: 50 <-- ID 4
         * EternityBasicPatternGreenDarkBlue: 24 <-- ID 5
         * EternityBasicPatternPurpleYellow: 48 <-- ID 6
         * EternityBasicPatternLightBlueRose: 50 <-- ID 7
         * EternityBasicPatternDarkBlueOrange: 50 <-- ID 8
         * EternityBasicPatternDarkBlueYellow: 24 <-- ID 9
         * EternityBasicPatternPurpleLightBlue: 48 <-- ID 10
         * EternityBasicPatternGreenOrange: 50 <-- ID 11
         * EternityBasicPatternYellowDarkBlue: 50 <-- ID 12
         * EternityBasicPatternBrownOrange: 24 <-- ID 13
         * EternityBasicPatternGreenRose: 48 <-- ID 14
         * EternityBasicPatternYellowGreen: 50 <-- ID 15
         * EternityBasicPatternDarkBlueLightBlue: 50 <-- ID 16
         * EternityBasicPatternRoseLightBlue: 24 <-- ID 17
         * EternityBasicPatternYellowLightBlue: 48 <-- ID 18
         * EternityBasicPatternBrownYellow: 50 <-- ID 19
         * EternityBasicPatternOrangePurple: 50 <-- ID 20
         * EternityBasicPatternRoseYellow: 50 <-- ID 21
         * EternityBasicPatternDarkBlueRose: 50 <-- ID 22
         */

        // Missing from user list compared to class:
        // ID 1: OrangeLightBlue.
        // User has "EternityBasicPatternLightBlue" with 24. Maybe that's
        // OrangeLightBlue? No, colors don't match.
        // Wait, ID 1 is OrangeLightBlue.
        // User list has "EternityBasicPatternLightBlue" (24).
        // Maybe I should just use a default high count if not found, or try to match
        // best effort.
        // Or maybe I should just count them dynamically from the board/tiles!
        // That's much safer and robust.
    }

    @Override
    public AbstractEternityBoard computeTessellation(EternityBoardInterface startingBoard) {
        // 1. Analyze pattern counts from the available tiles + board
        Map<String, Integer> patternCounts = countPatterns(startingBoard);

        // 2. Get missing tiles
        List<EternityTileInterface> missingTiles = new ArrayList<>(startingBoard.getMissingTiles());
        missingTiles.removeIf(Objects::isNull);

        // 3. Sort tiles by rarity
        missingTiles.sort(Comparator.comparingInt(t -> calculateTileRarityScore(t, patternCounts)));

        // 4. Solve with allocation-free backtracking
        boolean[] used = new boolean[missingTiles.size()];
        return solve(startingBoard, missingTiles, used, missingTiles.size());
    }

    // ... (helper methods like
    // countPatterns/incrementCounts/calculateTileRarityScore remain unchanged)
    private Map<String, Integer> countPatterns(EternityBoardInterface board) {
        Map<String, Integer> counts = new HashMap<>();
        // Count from all tiles (on board and missing)
        for (EternityTileInterface tile : board.getTiles()) {
            incrementCounts(tile, counts);
        }
        for (EternityTileInterface tile : board.getMissingTiles()) {
            incrementCounts(tile, counts);
        }
        return counts;
    }

    private void incrementCounts(EternityTileInterface tile, Map<String, Integer> counts) {
        if (tile == null)
            return;
        counts.merge(tile.getTop().toString(), 1, (a, b) -> a + b);
        counts.merge(tile.getRight().toString(), 1, (a, b) -> a + b);
        counts.merge(tile.getBottom().toString(), 1, (a, b) -> a + b);
        counts.merge(tile.getLeft().toString(), 1, (a, b) -> a + b);
    }

    private int calculateTileRarityScore(EternityTileInterface tile, Map<String, Integer> counts) {
        if (tile == null)
            return Integer.MAX_VALUE;
        // Score = min count of any pattern on the tile
        // We want tiles with rare patterns (low count) to have low score (be first)
        int min = Integer.MAX_VALUE;
        min = Math.min(min, counts.getOrDefault(tile.getTop().toString(), 100));
        min = Math.min(min, counts.getOrDefault(tile.getRight().toString(), 100));
        min = Math.min(min, counts.getOrDefault(tile.getBottom().toString(), 100));
        min = Math.min(min, counts.getOrDefault(tile.getLeft().toString(), 100));
        return min;
    }

    private AbstractEternityBoard solve(EternityBoardInterface board, List<EternityTileInterface> tiles, boolean[] used,
            int remainingCount) {
        // Optimization: Fail fast if tiles remaining doesn't match empty spots?
        // Actually, findNextEmpty handles completion.

        // Find next empty position
        int[] nextPos = findNextEmpty(board);
        if (nextPos == null) {
            return (AbstractEternityBoard) board; // Solved
        }
        int x = nextPos[0];
        int y = nextPos[1];

        for (int i = 0; i < tiles.size(); i++) {
            if (used[i])
                continue;

            EternityTileInterface tile = tiles.get(i);

            // Try all 4 rotations
            for (int r = 0; r < 4; r++) {
                if (board.setTileAt(x, y, tile)) {
                    // Mark used
                    used[i] = true;

                    AbstractEternityBoard result = solve(board, tiles, used, remainingCount - 1);
                    if (result != null)
                        return result;

                    // Backtrack
                    used[i] = false;
                    board.setTileAt(x, y, null); // Assuming null clears it
                }
                tile.rotateClockwise();
            }
        }

        return null;
    }

    private int[] findNextEmpty(EternityBoardInterface board) {
        for (int y = 0; y < board.getYBoardSize(); y++) {
            for (int x = 0; x < board.getXBoardSize(); x++) {
                if (board.getTileAt(x, y) == null) {
                    return new int[] { x, y };
                }
            }
        }
        return null;
    }
}
