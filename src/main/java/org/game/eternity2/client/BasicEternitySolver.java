/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.game.eternity2.client;

import org.game.eternity2.elements.AbstractEternityBoard;
import org.game.eternity2.elements.EternityBoardInterface;

import java.util.List;

/**
 * An algorithm to solve the puzzle.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

// brute force solver
public class BasicEternitySolver implements EternitySolverInterface {

    public AbstractEternityBoard computeTessellation(EternityBoardInterface startingBoard) {
        List<org.game.eternity2.elements.EternityTileInterface> missingTiles = new java.util.ArrayList<>(
                startingBoard.getMissingTiles());
        return solve(startingBoard, missingTiles);
    }

    private AbstractEternityBoard solve(EternityBoardInterface board,
            java.util.List<org.game.eternity2.elements.EternityTileInterface> tiles) {
        int[] nextPos = findNextEmpty(board);
        if (nextPos == null)
            return (AbstractEternityBoard) board;
        int x = nextPos[0];
        int y = nextPos[1];

        for (int i = 0; i < tiles.size(); i++) {
            org.game.eternity2.elements.EternityTileInterface tile = tiles.get(i);
            for (int r = 0; r < 4; r++) {
                if (board.setTileAt(x, y, tile)) {
                    java.util.List<org.game.eternity2.elements.EternityTileInterface> remaining = new java.util.ArrayList<>(
                            tiles);
                    remaining.remove(i);
                    AbstractEternityBoard result = solve(board, remaining);
                    if (result != null)
                        return result;
                    board.setTileAt(x, y, null);
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
