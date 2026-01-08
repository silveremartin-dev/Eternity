package org.game.eternity2.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Optimized board representation using primitive arrays.
 * 
 * Board layout:
 * - cells[]: Array of piece values (0 = empty, piece value otherwise)
 * - width, height: Board dimensions
 * - placedCount: Number of pieces placed
 */
public class BoardPrimitive implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int width;
    private final int height;
    private final long[] cells;
    private int placedCount;

    public BoardPrimitive(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new long[width * height];
        this.placedCount = 0;
    }

    /**
     * Copy constructor for backtracking.
     */
    public BoardPrimitive(BoardPrimitive other) {
        this.width = other.width;
        this.height = other.height;
        this.cells = Arrays.copyOf(other.cells, other.cells.length);
        this.placedCount = other.placedCount;
    }

    /**
     * Get cell index from coordinates.
     */
    private int index(int x, int y) {
        return y * width + x;
    }

    /**
     * Check if coordinates are valid.
     */
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Get piece at position. Returns 0 if empty.
     */
    public long getPiece(int x, int y) {
        if (!isValid(x, y))
            return 0;
        return cells[index(x, y)];
    }

    /**
     * Place a piece at position.
     */
    public void placePiece(int x, int y, long piece) {
        int idx = index(x, y);
        if (cells[idx] == 0 && piece != 0) {
            placedCount++;
        } else if (cells[idx] != 0 && piece == 0) {
            placedCount--;
        }
        cells[idx] = PiecePrimitive.setPlaced(piece, piece != 0);
    }

    /**
     * Remove piece from position.
     */
    public void removePiece(int x, int y) {
        placePiece(x, y, 0);
    }

    /**
     * Check if position is empty.
     */
    public boolean isEmpty(int x, int y) {
        return getPiece(x, y) == 0;
    }

    /**
     * Get constraints for a position based on neighbors.
     * Returns [top, right, bottom, left] constraints.
     * 0 = no constraint, >0 = must match, <0 = must be border.
     */
    public int[] getConstraints(int x, int y) {
        int[] constraints = new int[4];

        // Top constraint (from neighbor above)
        if (y == 0) {
            constraints[0] = -1; // Must be border
        } else {
            long neighbor = getPiece(x, y - 1);
            if (neighbor != 0) {
                constraints[0] = PiecePrimitive.getBottom(neighbor); // Must match neighbor's bottom
            }
        }

        // Right constraint (from neighbor to right)
        if (x == width - 1) {
            constraints[1] = -1; // Must be border
        } else {
            long neighbor = getPiece(x + 1, y);
            if (neighbor != 0) {
                constraints[1] = PiecePrimitive.getLeft(neighbor);
            }
        }

        // Bottom constraint (from neighbor below)
        if (y == height - 1) {
            constraints[2] = -1; // Must be border
        } else {
            long neighbor = getPiece(x, y + 1);
            if (neighbor != 0) {
                constraints[2] = PiecePrimitive.getTop(neighbor);
            }
        }

        // Left constraint (from neighbor to left)
        if (x == 0) {
            constraints[3] = -1; // Must be border
        } else {
            long neighbor = getPiece(x - 1, y);
            if (neighbor != 0) {
                constraints[3] = PiecePrimitive.getRight(neighbor);
            }
        }

        return constraints;
    }

    /**
     * Get position type (corner=0, edge=1, inner=2).
     */
    public int getPositionType(int x, int y) {
        int borderCount = 0;
        if (x == 0)
            borderCount++;
        if (x == width - 1)
            borderCount++;
        if (y == 0)
            borderCount++;
        if (y == height - 1)
            borderCount++;

        return switch (borderCount) {
            case 2 -> PiecePrimitive.TYPE_CORNER;
            case 1 -> PiecePrimitive.TYPE_EDGE;
            default -> PiecePrimitive.TYPE_INNER;
        };
    }

    // Getters
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSize() {
        return width * height;
    }

    public int getPlacedCount() {
        return placedCount;
    }

    public boolean isComplete() {
        return placedCount == width * height;
    }

    /**
     * Get raw cells array (for fast iteration).
     */
    public long[] getCells() {
        return cells;
    }

    /**
     * Compute the number of matching edges on the board.
     * Each successful match between two pieces counts as 1.
     * Each successful border match counts as 1 (optional, depends on scoring
     * rules).
     */
    public int computeScore() {
        int score = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long piece = getPiece(x, y);
                if (piece == 0)
                    continue;

                // Check top/bottom match
                if (y < height - 1) {
                    long below = getPiece(x, y + 1);
                    if (below != 0 && PiecePrimitive.getBottom(piece) == PiecePrimitive.getTop(below)) {
                        score++;
                    }
                } else {
                    // Border match
                    if (PiecePrimitive.getBottom(piece) == 0)
                        score++;
                }

                // Check left/right match
                if (x < width - 1) {
                    long right = getPiece(x + 1, y);
                    if (right != 0 && PiecePrimitive.getRight(piece) == PiecePrimitive.getLeft(right)) {
                        score++;
                    }
                } else {
                    // Border match
                    if (PiecePrimitive.getRight(piece) == 0)
                        score++;
                }

                // Initial borders (top and left)
                if (y == 0 && PiecePrimitive.getTop(piece) == 0)
                    score++;
                if (x == 0 && PiecePrimitive.getLeft(piece) == 0)
                    score++;
            }
        }
        return score;
    }

    /**
     * Validate board state (check all adjacencies).
     */
    public boolean isValid() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long piece = getPiece(x, y);
                if (piece == 0)
                    continue;

                // Check top
                if (y == 0) {
                    if (PiecePrimitive.getTop(piece) != 0)
                        return false;
                } else {
                    long neighbor = getPiece(x, y - 1);
                    if (neighbor != 0 && PiecePrimitive.getTop(piece) != PiecePrimitive.getBottom(neighbor)) {
                        return false;
                    }
                }

                // Check left
                if (x == 0) {
                    if (PiecePrimitive.getLeft(piece) != 0)
                        return false;
                } else {
                    long neighbor = getPiece(x - 1, y);
                    if (neighbor != 0 && PiecePrimitive.getLeft(piece) != PiecePrimitive.getRight(neighbor)) {
                        return false;
                    }
                }

                // Check right border
                if (x == width - 1 && PiecePrimitive.getRight(piece) != 0)
                    return false;

                // Check bottom border
                if (y == height - 1 && PiecePrimitive.getBottom(piece) != 0)
                    return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Board[%dx%d, placed=%d/%d]\n", width, height, placedCount, getSize()));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long piece = getPiece(x, y);
                if (piece == 0) {
                    sb.append("  .  ");
                } else {
                    sb.append(String.format(" %3d ", PiecePrimitive.getId(piece)));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
