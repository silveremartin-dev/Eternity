package org.game.eternity2.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BoardPrimitive.
 * 
 * @author Gemini AI Assistant
 * @author Silvère
 */
class BoardPrimitiveTest {

    private BoardPrimitive board;

    @BeforeEach
    void setUp() {
        board = new BoardPrimitive(4, 4);
    }

    @Test
    @DisplayName("Create empty board")
    void testCreateBoard() {
        assertEquals(4, board.getWidth());
        assertEquals(4, board.getHeight());
        assertEquals(16, board.getSize());
        assertEquals(0, board.getPlacedCount());
        assertFalse(board.isComplete());
    }

    @Test
    @DisplayName("Place piece on board")
    void testPlacePiece() {
        long piece = PiecePrimitive.create(42, 0, 5, 10, 0);
        board.placePiece(0, 0, piece);

        assertEquals(1, board.getPlacedCount());
        assertFalse(board.isEmpty(0, 0));
        assertEquals(42, PiecePrimitive.getId(board.getPiece(0, 0)));
    }

    @Test
    @DisplayName("Remove piece from board")
    void testRemovePiece() {
        long piece = PiecePrimitive.create(1, 0, 5, 10, 0);
        board.placePiece(0, 0, piece);
        assertEquals(1, board.getPlacedCount());

        board.removePiece(0, 0);
        assertEquals(0, board.getPlacedCount());
        assertTrue(board.isEmpty(0, 0));
    }

    @Test
    @DisplayName("Position type detection")
    void testPositionType() {
        // Corners
        assertEquals(PiecePrimitive.TYPE_CORNER, board.getPositionType(0, 0));
        assertEquals(PiecePrimitive.TYPE_CORNER, board.getPositionType(3, 0));
        assertEquals(PiecePrimitive.TYPE_CORNER, board.getPositionType(0, 3));
        assertEquals(PiecePrimitive.TYPE_CORNER, board.getPositionType(3, 3));

        // Edges
        assertEquals(PiecePrimitive.TYPE_EDGE, board.getPositionType(1, 0));
        assertEquals(PiecePrimitive.TYPE_EDGE, board.getPositionType(0, 1));

        // Inner
        assertEquals(PiecePrimitive.TYPE_INNER, board.getPositionType(1, 1));
        assertEquals(PiecePrimitive.TYPE_INNER, board.getPositionType(2, 2));
    }

    @Test
    @DisplayName("Get constraints for empty position")
    void testGetConstraints() {
        int[] constraints = board.getConstraints(0, 0);

        // Top-left corner: top and left must be border (-1)
        assertEquals(-1, constraints[0]); // Top
        assertEquals(0, constraints[1]); // Right (no neighbor)
        assertEquals(0, constraints[2]); // Bottom (no neighbor)
        assertEquals(-1, constraints[3]); // Left
    }

    @Test
    @DisplayName("Get constraints with neighbor")
    void testGetConstraintsWithNeighbor() {
        // Place piece at (1, 0) with right edge = 15
        long piece = PiecePrimitive.create(1, 0, 15, 10, 5);
        board.placePiece(1, 0, piece);

        // Check constraints at (2, 0) - left should match neighbor's right
        int[] constraints = board.getConstraints(2, 0);
        assertEquals(15, constraints[3]); // Left must match piece's right edge
    }

    @Test
    @DisplayName("Board copy constructor")
    void testCopyConstructor() {
        long piece = PiecePrimitive.create(42, 0, 5, 10, 0);
        board.placePiece(0, 0, piece);

        BoardPrimitive copy = new BoardPrimitive(board);

        assertEquals(board.getPlacedCount(), copy.getPlacedCount());
        assertEquals(42, PiecePrimitive.getId(copy.getPiece(0, 0)));

        // Modify copy, original should be unchanged
        copy.removePiece(0, 0);
        assertEquals(0, copy.getPlacedCount());
        assertEquals(1, board.getPlacedCount());
    }

    @Test
    @DisplayName("Valid coordinates check")
    void testIsValid() {
        assertTrue(board.isValid(0, 0));
        assertTrue(board.isValid(3, 3));
        assertFalse(board.isValid(-1, 0));
        assertFalse(board.isValid(0, -1));
        assertFalse(board.isValid(4, 0));
        assertFalse(board.isValid(0, 4));
    }
}
