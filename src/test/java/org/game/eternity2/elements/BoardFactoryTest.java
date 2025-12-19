/*
 * Copyright 2022-2024 Silvere Martin-Michiellot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.game.eternity2.elements;

import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.elements.size4x4.EternityBoard4x4;
import org.game.eternity2.elements.size6x6.EternityBoard6x6;
import org.game.eternity2.elements.size12x6.EternityBoard12x6;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BoardFactory.
 */
class BoardFactoryTest {

    @Test
    void testCreate4x4Board() {
        AbstractEternityBoard board = BoardFactory.createBoard(4, 4);
        assertNotNull(board);
        assertTrue(board instanceof EternityBoard4x4);
        assertEquals(4, board.getXBoardSize());
        assertEquals(4, board.getYBoardSize());
    }

    @Test
    void testCreate6x6Board() {
        AbstractEternityBoard board = BoardFactory.createBoard(6, 6);
        assertNotNull(board);
        assertTrue(board instanceof EternityBoard6x6);
        assertEquals(6, board.getXBoardSize());
        assertEquals(6, board.getYBoardSize());
    }

    @Test
    void testCreate16x16Board() {
        AbstractEternityBoard board = BoardFactory.createBoard(16, 16);
        assertNotNull(board);
        assertTrue(board instanceof EternityBoard16x16);
        assertEquals(16, board.getXBoardSize());
        assertEquals(16, board.getYBoardSize());
    }

    @Test
    void testCreate12x6Board() {
        AbstractEternityBoard board = BoardFactory.createBoard(12, 6);
        assertNotNull(board);
        assertTrue(board instanceof EternityBoard12x6);
        assertEquals(12, board.getXBoardSize());
        assertEquals(6, board.getYBoardSize());
    }

    @Test
    void testCreateUnsupportedSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            BoardFactory.createBoard(5, 5);
        });
    }

    @Test
    void testCreateSquareBoard() {
        AbstractEternityBoard board = BoardFactory.createSquareBoard(4);
        assertNotNull(board);
        assertEquals(4, board.getXBoardSize());
        assertEquals(4, board.getYBoardSize());
    }

    @Test
    void testGetSupportedSizes() {
        int[][] sizes = BoardFactory.getSupportedSizes();
        assertNotNull(sizes);
        assertTrue(sizes.length >= 4);
    }

    @Test
    void testIsSupported() {
        assertTrue(BoardFactory.isSupported(4, 4));
        assertTrue(BoardFactory.isSupported(16, 16));
        assertFalse(BoardFactory.isSupported(5, 5));
    }

    @Test
    void testGetBoardDescription() {
        String desc = BoardFactory.getBoardDescription(4, 4);
        assertNotNull(desc);
        assertTrue(desc.contains("4x4"));
        assertTrue(desc.contains("16 tiles"));
    }
}
