package org.game.eternity2.model.optimized;

/**
 * Optimized piece representation using primitives.
 * 
 * A piece is encoded as a single long (64 bits):
 * - Bits 0-7: Piece ID (0-255)
 * - Bits 8-15: Top edge pattern (0-255)
 * - Bits 16-23: Right edge pattern (0-255)
 * - Bits 24-31: Bottom edge pattern (0-255)
 * - Bits 32-39: Left edge pattern (0-255)
 * - Bits 40-41: Rotation (0-3)
 * - Bits 42-43: Piece type (0=corner, 1=edge, 2=inner)
 * - Bit 44: Placed flag
 * 
 * Edge pattern 0 = Border (grey edge).
 */
public class PiecePrimitive {

    // Bit positions
    private static final int ID_SHIFT = 0;
    private static final int TOP_SHIFT = 8;
    private static final int RIGHT_SHIFT = 16;
    private static final int BOTTOM_SHIFT = 24;
    private static final int LEFT_SHIFT = 32;
    private static final int ROTATION_SHIFT = 40;
    private static final int TYPE_SHIFT = 42;
    private static final int PLACED_SHIFT = 44;

    // Masks
    private static final long BYTE_MASK = 0xFFL;
    private static final long TWO_BIT_MASK = 0x3L;
    private static final long PLACED_MASK = 1L << PLACED_SHIFT;

    // Type constants
    public static final int TYPE_CORNER = 0;
    public static final int TYPE_EDGE = 1;
    public static final int TYPE_INNER = 2;

    /**
     * Create a piece from components.
     */
    public static long create(int id, int top, int right, int bottom, int left) {
        int type = determineType(top, right, bottom, left);
        return ((long) id & BYTE_MASK) << ID_SHIFT
                | ((long) top & BYTE_MASK) << TOP_SHIFT
                | ((long) right & BYTE_MASK) << RIGHT_SHIFT
                | ((long) bottom & BYTE_MASK) << BOTTOM_SHIFT
                | ((long) left & BYTE_MASK) << LEFT_SHIFT
                | ((long) type & TWO_BIT_MASK) << TYPE_SHIFT;
    }

    /**
     * Determine piece type based on border edges (pattern 0).
     */
    private static int determineType(int top, int right, int bottom, int left) {
        int borderCount = 0;
        if (top == 0)
            borderCount++;
        if (right == 0)
            borderCount++;
        if (bottom == 0)
            borderCount++;
        if (left == 0)
            borderCount++;

        return switch (borderCount) {
            case 2 -> TYPE_CORNER;
            case 1 -> TYPE_EDGE;
            default -> TYPE_INNER;
        };
    }

    // Getters
    public static int getId(long piece) {
        return (int) ((piece >> ID_SHIFT) & BYTE_MASK);
    }

    public static int getTop(long piece) {
        return (int) ((piece >> TOP_SHIFT) & BYTE_MASK);
    }

    public static int getRight(long piece) {
        return (int) ((piece >> RIGHT_SHIFT) & BYTE_MASK);
    }

    public static int getBottom(long piece) {
        return (int) ((piece >> BOTTOM_SHIFT) & BYTE_MASK);
    }

    public static int getLeft(long piece) {
        return (int) ((piece >> LEFT_SHIFT) & BYTE_MASK);
    }

    public static int getRotation(long piece) {
        return (int) ((piece >> ROTATION_SHIFT) & TWO_BIT_MASK);
    }

    public static int getType(long piece) {
        return (int) ((piece >> TYPE_SHIFT) & TWO_BIT_MASK);
    }

    public static boolean isPlaced(long piece) {
        return (piece & PLACED_MASK) != 0;
    }

    // Setters (return new piece)
    public static long setRotation(long piece, int rotation) {
        return (piece & ~(TWO_BIT_MASK << ROTATION_SHIFT))
                | ((long) (rotation & 0x3) << ROTATION_SHIFT);
    }

    public static long setPlaced(long piece, boolean placed) {
        if (placed) {
            return piece | PLACED_MASK;
        } else {
            return piece & ~PLACED_MASK;
        }
    }

    /**
     * Rotate piece clockwise by 90 degrees.
     * Returns a new piece with edges rotated.
     */
    public static long rotateCW(long piece) {
        int top = getTop(piece);
        int right = getRight(piece);
        int bottom = getBottom(piece);
        int left = getLeft(piece);
        int rotation = (getRotation(piece) + 1) & 0x3;

        // CW rotation: left->top, top->right, right->bottom, bottom->left
        long rotated = (piece & ~(BYTE_MASK << TOP_SHIFT))
                & ~(BYTE_MASK << RIGHT_SHIFT)
                & ~(BYTE_MASK << BOTTOM_SHIFT)
                & ~(BYTE_MASK << LEFT_SHIFT)
                & ~(TWO_BIT_MASK << ROTATION_SHIFT);

        return rotated
                | ((long) left << TOP_SHIFT)
                | ((long) top << RIGHT_SHIFT)
                | ((long) right << BOTTOM_SHIFT)
                | ((long) bottom << LEFT_SHIFT)
                | ((long) rotation << ROTATION_SHIFT);
    }

    /**
     * Get edge at given direction (0=top, 1=right, 2=bottom, 3=left).
     */
    public static int getEdge(long piece, int direction) {
        return switch (direction & 0x3) {
            case 0 -> getTop(piece);
            case 1 -> getRight(piece);
            case 2 -> getBottom(piece);
            case 3 -> getLeft(piece);
            default -> 0;
        };
    }

    /**
     * Check if piece matches constraints at a position.
     * Constraint value 0 means wildcard (match anything except 0 for non-border).
     * Negative constraint means must be border (edge 0).
     */
    public static boolean matches(long piece, int topConstraint, int rightConstraint,
            int bottomConstraint, int leftConstraint) {
        if (topConstraint > 0 && getTop(piece) != topConstraint)
            return false;
        if (topConstraint < 0 && getTop(piece) != 0)
            return false;
        if (rightConstraint > 0 && getRight(piece) != rightConstraint)
            return false;
        if (rightConstraint < 0 && getRight(piece) != 0)
            return false;
        if (bottomConstraint > 0 && getBottom(piece) != bottomConstraint)
            return false;
        if (bottomConstraint < 0 && getBottom(piece) != 0)
            return false;
        if (leftConstraint > 0 && getLeft(piece) != leftConstraint)
            return false;
        if (leftConstraint < 0 && getLeft(piece) != 0)
            return false;
        return true;
    }

    /**
     * Debug string representation.
     */
    public static String toString(long piece) {
        return String.format("Piece[id=%d, edges=%d/%d/%d/%d, rot=%d, type=%d, placed=%b]",
                getId(piece), getTop(piece), getRight(piece), getBottom(piece), getLeft(piece),
                getRotation(piece), getType(piece), isPlaced(piece));
    }
}
