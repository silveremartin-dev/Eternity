package org.game.eternity2.solver;

import org.game.eternity2.model.PiecePrimitive;
import java.util.Arrays;

/**
 * Advanced pruning strategies to cut the search space.
 */
public class GlobalPruner {

    private final int[] colorCounts; // Remaining available edges of each color
    private final int totalPieces;
    private int placedCount;

    public GlobalPruner(long[] allPieces) {
        this.colorCounts = new int[256];
        this.totalPieces = allPieces.length;
        this.placedCount = 0;
        initialize(allPieces);
    }

    private void initialize(long[] allPieces) {
        for (long piece : allPieces) {
            // Count each unique edge color once per piece? 
            // No, count all 4 edges.
            colorCounts[PiecePrimitive.getTop(piece)]++;
            colorCounts[PiecePrimitive.getRight(piece)]++;
            colorCounts[PiecePrimitive.getBottom(piece)]++;
            colorCounts[PiecePrimitive.getLeft(piece)]++;
        }
    }

    public void onPiecePlaced(long piece) {
        colorCounts[PiecePrimitive.getTop(piece)]--;
        colorCounts[PiecePrimitive.getRight(piece)]--;
        colorCounts[PiecePrimitive.getBottom(piece)]--;
        colorCounts[PiecePrimitive.getLeft(piece)]--;
        placedCount++;
    }

    public void onPieceRemoved(long piece) {
        colorCounts[PiecePrimitive.getTop(piece)]++;
        colorCounts[PiecePrimitive.getRight(piece)]++;
        colorCounts[PiecePrimitive.getBottom(piece)]++;
        colorCounts[PiecePrimitive.getLeft(piece)]++;
        placedCount--;
    }

    /**
     * Check if the current state is still solvable based on color parity.
     * This is a simplified version; more complex ones check North/South/East/West separately.
     */
    public boolean isSolvable(int[] requiredColors) {
        // Simple parity check: for each color, we must have at least as many 
        // available edges as required on the board.
        // In practice, each edge is shared, so total counts must be even (except for borders).
        for (int i = 1; i < 256; i++) {
            if (colorCounts[i] < 0) return false;
        }
        return true;
    }

    /**
     * Border Pruning (Contorno): 
     * If the current line of placement creates a pattern that is known to be impossible.
     */
    public boolean checkBorder(int[] borderColors) {
        // Placeholder for complex pattern matching
        return true;
    }
}
