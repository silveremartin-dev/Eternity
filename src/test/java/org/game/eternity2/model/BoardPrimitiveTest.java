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

    @Test
    @DisplayName("Is complete check")
    void testIsComplete() {
        assertFalse(board.isComplete());
        // Fill the board (4x4)
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                board.placePiece(x, y, PiecePrimitive.create(1, 0, 0, 0, 0));
            }
        }
        assertTrue(board.isComplete());
    }

    @Test
    @DisplayName("Neighbor count calculation")
    void testNeighborCount() {
        assertEquals(0, board.getNeighborCount(1, 1));

        board.placePiece(1, 0, PiecePrimitive.create(1, 0, 0, 0, 0));
        assertEquals(1, board.getNeighborCount(1, 1));

        board.placePiece(0, 1, PiecePrimitive.create(2, 0, 0, 0, 0));
        assertEquals(2, board.getNeighborCount(1, 1));

        board.placePiece(2, 1, PiecePrimitive.create(3, 0, 0, 0, 0));
        assertEquals(3, board.getNeighborCount(1, 1));

        board.placePiece(1, 2, PiecePrimitive.create(4, 0, 0, 0, 0));
        assertEquals(4, board.getNeighborCount(1, 1));
    }

    @Test
    @DisplayName("Compute board score")
    void testComputeScore() {
        // Place two matching pieces side by side
        // Piece 1 at (0,0): Right edge = 5, Bottom edge = 10
        long p1 = PiecePrimitive.create(1, 0, 5, 10, 0);
        // Piece 2 at (1,0): Left edge = 5 (match), Top edge = 0 (border), Right edge =
        // 7
        long p2 = PiecePrimitive.create(2, 0, 7, 0, 5);

        board.placePiece(0, 0, p1);
        board.placePiece(1, 0, p2);

        // Match at (0,0) with Top border, Left border
        // Match at (1,0) with Top border
        // Match between (0,0) and (1,0)
        // Total matching edges should be calculated
        int score = board.computeScore();
        assertTrue(score > 0);
    }

    @Test
    @DisplayName("Validate board state")
    void testBoardIsValidState() {
        assertTrue(board.isValid()); // Empty board is valid

        // Place two non-matching pieces
        long p1 = PiecePrimitive.create(1, 0, 5, 0, 0);
        long p2 = PiecePrimitive.create(2, 0, 0, 0, 10); // Left is 10, doesn't match p1's right (5)

        board.placePiece(0, 0, p1);
        board.placePiece(1, 0, p2);

        assertFalse(board.isValid());

        // Correct p2
        board.removePiece(1, 0);
        long p2Correct = PiecePrimitive.create(2, 0, 0, 0, 5); // Left is 5, matches p1's right
        board.placePiece(1, 0, p2Correct);
        assertTrue(board.isValid());
    }

    @Test
    @DisplayName("Find most constrained position")
    void testFindMostConstrainedPosition() {
        // Initially, corners are most constrained (2 border match constraints)
        int[] pos = board.findMostConstrainedPosition();
        assertNotNull(pos);
        // Should be one of the corners
        assertTrue((pos[0] == 0 || pos[0] == 3) && (pos[1] == 0 || pos[1] == 3));

        // Fill a corner
        board.placePiece(0, 0, PiecePrimitive.create(1, -1, 5, 10, -1));

        // Now (1,0) or (0,1) might be more constrained as they have a neighbor and a
        // border
        pos = board.findMostConstrainedPosition();
        assertNotNull(pos);
        assertFalse(pos[0] == 0 && pos[1] == 0); // (0,0) is filled
    }

    @Test
    @DisplayName("Board toString check")
    void testToString() {
        String s = board.toString();
        assertNotNull(s);
        assertTrue(s.contains("4x4"));
    }

    @Test
    @DisplayName("Board raw cells access")
    void testGetCells() {
        long[] cells = board.getCells();
        assertEquals(board.getSize(), cells.length);
    }

}
