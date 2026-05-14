package org.game.eternity2.solver;

import org.game.eternity2.model.PiecePrimitive;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre-calculated index for $O(1)$ piece lookup based on top and left constraints.
 * Highly optimized for the core solving loop.
 */
public class NeighborIndex {

    // Eternity II has 23 colors + 1 border (0)
    private static final int MAX_COLORS = 256; 
    
    // index[top][left] -> array of compatible pieces
    private final long[][][] index;

    public NeighborIndex(long[] allPieces) {
        this.index = new long[MAX_COLORS][MAX_COLORS][];
        initialize(allPieces);
    }

    private void initialize(long[] allPieces) {
        @SuppressWarnings("unchecked")
        List<Long>[][] tempIndex = new ArrayList[MAX_COLORS][MAX_COLORS];

        for (long piece : allPieces) {
            // Try all 4 rotations for each piece
            long rotated = piece;
            for (int r = 0; r < 4; r++) {
                int top = PiecePrimitive.getTop(rotated);
                int left = PiecePrimitive.getLeft(rotated);

                if (tempIndex[top][left] == null) {
                    tempIndex[top][left] = new ArrayList<>();
                }
                tempIndex[top][left].add(rotated);
                
                rotated = PiecePrimitive.rotateCW(rotated);
            }
        }

        // Convert to flat arrays for performance
        for (int t = 0; t < MAX_COLORS; t++) {
            for (int l = 0; l < MAX_COLORS; l++) {
                if (tempIndex[t][l] != null) {
                    index[t][l] = tempIndex[t][l].stream().mapToLong(Long::longValue).toArray();
                } else {
                    index[t][l] = new long[0];
                }
            }
        }
    }

    /**
     * Get all pieces that match the given top and left colors.
     */
    public long[] getCandidates(int top, int left) {
        // Ensure bounds safety (colors should be 0-255)
        return index[top & 0xFF][left & 0xFF];
    }
}
