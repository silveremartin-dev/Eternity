package org.game.eternity2.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PiecePrimitive.
 * 
 * @author Gemini AI Assistant
 * @author Silvère
 */
class PiecePrimitiveTest {

    @Test
    @DisplayName("Create piece with valid edges")
    void testCreatePiece() {
        long piece = PiecePrimitive.create(42, 5, 10, 15, 20);

        assertEquals(42, PiecePrimitive.getId(piece));
        assertEquals(5, PiecePrimitive.getTop(piece));
        assertEquals(10, PiecePrimitive.getRight(piece));
        assertEquals(15, PiecePrimitive.getBottom(piece));
        assertEquals(20, PiecePrimitive.getLeft(piece));
    }

    @Test
    @DisplayName("Piece type detection - corner")
    void testCornerType() {
        // Corner has 2 border edges (pattern 0)
        long piece = PiecePrimitive.create(1, 0, 5, 10, 0);
        assertEquals(PiecePrimitive.TYPE_CORNER, PiecePrimitive.getType(piece));
    }

    @Test
    @DisplayName("Piece type detection - edge")
    void testEdgeType() {
        // Edge has 1 border edge
        long piece = PiecePrimitive.create(2, 0, 5, 10, 15);
        assertEquals(PiecePrimitive.TYPE_EDGE, PiecePrimitive.getType(piece));
    }

    @Test
    @DisplayName("Piece type detection - inner")
    void testInnerType() {
        // Inner has no border edges
        long piece = PiecePrimitive.create(3, 5, 10, 15, 20);
        assertEquals(PiecePrimitive.TYPE_INNER, PiecePrimitive.getType(piece));
    }

    @Test
    @DisplayName("Rotate piece clockwise")
    void testRotateCW() {
        long piece = PiecePrimitive.create(1, 1, 2, 3, 4);
        long rotated = PiecePrimitive.rotateCW(piece);

        // After CW rotation: left->top, top->right, right->bottom, bottom->left
        assertEquals(4, PiecePrimitive.getTop(rotated));
        assertEquals(1, PiecePrimitive.getRight(rotated));
        assertEquals(2, PiecePrimitive.getBottom(rotated));
        assertEquals(3, PiecePrimitive.getLeft(rotated));
        assertEquals(1, PiecePrimitive.getRotation(rotated));
    }

    @Test
    @DisplayName("Get edge by direction")
    void testGetEdge() {
        long piece = PiecePrimitive.create(1, 10, 20, 30, 40);

        assertEquals(10, PiecePrimitive.getEdge(piece, 0)); // Top
        assertEquals(20, PiecePrimitive.getEdge(piece, 1)); // Right
        assertEquals(30, PiecePrimitive.getEdge(piece, 2)); // Bottom
        assertEquals(40, PiecePrimitive.getEdge(piece, 3)); // Left
    }

    @Test
    @DisplayName("Placed flag")
    void testPlacedFlag() {
        long piece = PiecePrimitive.create(1, 1, 2, 3, 4);
        assertFalse(PiecePrimitive.isPlaced(piece));

        long placed = PiecePrimitive.setPlaced(piece, true);
        assertTrue(PiecePrimitive.isPlaced(placed));

        long unplaced = PiecePrimitive.setPlaced(placed, false);
        assertFalse(PiecePrimitive.isPlaced(unplaced));
    }

    @Test
    @DisplayName("Constraint matching")
    void testMatches() {
        long piece = PiecePrimitive.create(1, 5, 10, 0, 15);

        // All wildcards (0) - should match
        assertTrue(PiecePrimitive.matches(piece, 0, 0, 0, 0));

        // Exact match
        assertTrue(PiecePrimitive.matches(piece, 5, 10, 0, 15));

        // Mismatch
        assertFalse(PiecePrimitive.matches(piece, 5, 10, 20, 15));

        // Border constraint (negative) - bottom should be 0
        assertTrue(PiecePrimitive.matches(piece, 0, 0, -1, 0));

        // Border constraint where not border
        assertFalse(PiecePrimitive.matches(piece, -1, 0, 0, 0));
    }

    @Test
    @DisplayName("Set rotation")
    void testSetRotation() {
        long piece = PiecePrimitive.create(1, 1, 2, 3, 4);
        long rotated = PiecePrimitive.setRotation(piece, 2);
        assertEquals(2, PiecePrimitive.getRotation(rotated));
        // Edges should be UNCHANGED by setRotation (only the flag changes)
        assertEquals(1, PiecePrimitive.getTop(rotated));
    }

    @Test
    @DisplayName("ToString check")
    void testToString() {
        long piece = PiecePrimitive.create(1, 1, 2, 3, 4);
        String s = PiecePrimitive.toString(piece);
        assertNotNull(s);
        assertTrue(s.contains("id=1"));
    }

    @Test
    @DisplayName("Multiple CW rotations")
    void testMultipleRotateCW() {
        long piece = PiecePrimitive.create(1, 5, 10, 15, 20);
        long r1 = PiecePrimitive.rotateCW(piece);
        long r2 = PiecePrimitive.rotateCW(r1);
        long r3 = PiecePrimitive.rotateCW(r2);
        long r4 = PiecePrimitive.rotateCW(r3);

        // After 4 rotations, should be back to original edges (but rotation flag might
        // be different depending on implementation,
        // actually rotateCW increases rotation flag too)
        assertEquals(PiecePrimitive.getTop(piece), PiecePrimitive.getTop(r4));
        assertEquals(PiecePrimitive.getRight(piece), PiecePrimitive.getRight(r4));
        assertEquals(0, PiecePrimitive.getRotation(r4));
    }
}
