package org.game.eternity2.elements;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BoardFactory.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
class BoardFactoryTest {

    @Test
    void testCreateSquareBoards() {
        // Test all supported square sizes
        EternityBoardInterface board4x4 = BoardFactory.createSquareBoard(4);
        assertNotNull(board4x4);
        assertEquals(4, board4x4.getXBoardSize());
        assertEquals(4, board4x4.getYBoardSize());

        EternityBoardInterface board6x6 = BoardFactory.createSquareBoard(6);
        assertNotNull(board6x6);
        assertEquals(6, board6x6.getXBoardSize());

        EternityBoardInterface board16x16 = BoardFactory.createSquareBoard(16);
        assertNotNull(board16x16);
        assertEquals(16, board16x16.getXBoardSize());
    }

    @Test
    void testCreateNonSquareBoard() {
        EternityBoardInterface board12x6 = BoardFactory.createBoard(12, 6);
        assertNotNull(board12x6);
        assertEquals(12, board12x6.getXBoardSize());
        assertEquals(6, board12x6.getYBoardSize());
    }

    @Test
    void testUnsupportedSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            BoardFactory.createSquareBoard(8);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            BoardFactory.createBoard(10, 10);
        });
    }

    @Test
    void testIsSupported() {
        assertTrue(BoardFactory.isSupported(4, 4));
        assertTrue(BoardFactory.isSupported(6, 6));
        assertTrue(BoardFactory.isSupported(12, 6));
        assertTrue(BoardFactory.isSupported(16, 16));

        assertFalse(BoardFactory.isSupported(8, 8));
        assertFalse(BoardFactory.isSupported(10, 5));
    }

    @Test
    void testGetSupportedSizes() {
        int[][] sizes = BoardFactory.getSupportedSizes();
        assertEquals(4, sizes.length);

        // Verify 4x4 is in the list
        boolean found4x4 = false;
        for (int[] size : sizes) {
            if (size[0] == 4 && size[1] == 4) {
                found4x4 = true;
                break;
            }
        }
        assertTrue(found4x4);
    }

    @Test
    void testGetBoardDescription() {
        String desc4x4 = BoardFactory.getBoardDescription(4, 4);
        assertTrue(desc4x4.contains("Square"));
        assertTrue(desc4x4.contains("4x4"));
        assertTrue(desc4x4.contains("16 tiles"));

        String desc12x6 = BoardFactory.getBoardDescription(12, 6);
        assertTrue(desc12x6.contains("Rectangular"));
        assertTrue(desc12x6.contains("12x6"));
        assertTrue(desc12x6.contains("72 tiles"));
    }
}
