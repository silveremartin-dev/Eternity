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
import org.game.eternity2.elements.EternityTileInterface;
import org.game.eternity2.server.kernel.EternityKernel;

import java.util.ArrayList;
import java.util.List;

/**
 * An algorithm to solve the puzzle using "Parallel Candidate Evaluation".
 * This solver leverages the EternityKernel (CPU or GPU) to check multiple
 * candidates at once.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class AdvancedEternitySolver implements EternitySolverInterface {

    @Override
    public AbstractEternityBoard computeTessellation(EternityBoardInterface startingBoard) {
        List<EternityTileInterface> missingTiles = new ArrayList<>(startingBoard.getMissingTiles());
        return solve(startingBoard, missingTiles);
    }

    private AbstractEternityBoard solve(EternityBoardInterface board, List<EternityTileInterface> tiles) {
        int[] nextPos = findNextEmpty(board);
        if (nextPos == null) {
            return (AbstractEternityBoard) board;
        }
        int x = nextPos[0];
        int y = nextPos[1];

        // 1. Prepare Constraints
        int[] constraints = getConstraints(board, x, y);

        // 2. Prepare Candidates (All tiles * 4 rotations)
        // Optimization: In a real GPU scenario, we would keep this array in GPU memory
        // and only update the "available" mask.
        // For now, we rebuild it or use a simplified approach.
        // Size: num_tiles * 4 rotations * 4 patterns
        int numTiles = tiles.size();
        int[] candidates = new int[numTiles * 4 * 4];

        for (int i = 0; i < numTiles; i++) {
            EternityTileInterface tile = tiles.get(i);
            int t = tile.getTop().getValue();
            int r = tile.getRight().getValue();
            int b = tile.getBottom().getValue();
            int l = tile.getLeft().getValue();

            // Rotation 0: T, R, B, L
            int base = i * 16; // 4 rotations * 4 values
            candidates[base] = t;
            candidates[base + 1] = r;
            candidates[base + 2] = b;
            candidates[base + 3] = l;

            // Rotation 1: L, T, R, B
            candidates[base + 4] = l;
            candidates[base + 5] = t;
            candidates[base + 6] = r;
            candidates[base + 7] = b;

            // Rotation 2: B, L, T, R
            candidates[base + 8] = b;
            candidates[base + 9] = l;
            candidates[base + 10] = t;
            candidates[base + 11] = r;

            // Rotation 3: R, B, L, T
            candidates[base + 12] = r;
            candidates[base + 13] = b;
            candidates[base + 14] = l;
            candidates[base + 15] = t;
        }

        // 3. Call Kernel
        int[] results = new int[numTiles * 4];
        EternityKernel.checkCandidates(constraints, candidates, results);

        // 4. Process Results
        for (int i = 0; i < results.length; i++) {
            if (results[i] == 1) {
                int tileIndex = i / 4;
                int rotationIndex = i % 4;

                EternityTileInterface tile = tiles.get(tileIndex);

                // Rotate tile to match the valid rotation
                // Note: tile.getRotation() might be useful if we want to be precise,
                // but here we just rotate relative to current state.
                // Wait, the patterns we extracted (T, R, B, L) were from the *current* state of
                // the tile object.
                // So "Rotation 0" means "keep as is". "Rotation 1" means "rotate 1 clockwise".

                for (int r = 0; r < rotationIndex; r++) {
                    tile.rotateClockwise();
                }

                if (board.setTileAt(x, y, tile)) {
                    List<EternityTileInterface> remaining = new ArrayList<>(tiles);
                    remaining.remove(tileIndex);

                    AbstractEternityBoard result = solve(board, remaining);
                    if (result != null) {
                        return result;
                    }

                    board.setTileAt(x, y, null);
                }

                // Backtrack rotation
                for (int r = 0; r < rotationIndex; r++) {
                    tile.rotateCounterClockwise();
                }
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

    private int[] getConstraints(EternityBoardInterface board, int x, int y) {
        int[] constraints = new int[] { -1, -1, -1, -1 }; // T, R, B, L

        // Top
        if (y == 0) {
            constraints[0] = 0; // Border (Gray)
        } else {
            EternityTileInterface topTile = board.getTileAt(x, y - 1);
            if (topTile != null) {
                constraints[0] = topTile.getBottom().getValue();
            }
        }

        // Right
        if (x == board.getXBoardSize() - 1) {
            constraints[1] = 0; // Border
        } else {
            EternityTileInterface rightTile = board.getTileAt(x + 1, y);
            if (rightTile != null) {
                constraints[1] = rightTile.getLeft().getValue();
            }
        }

        // Bottom
        if (y == board.getYBoardSize() - 1) {
            constraints[2] = 0; // Border
        } else {
            EternityTileInterface bottomTile = board.getTileAt(x, y + 1);
            if (bottomTile != null) {
                constraints[2] = bottomTile.getTop().getValue();
            }
        }

        // Left
        if (x == 0) {
            constraints[3] = 0; // Border
        } else {
            EternityTileInterface leftTile = board.getTileAt(x - 1, y);
            if (leftTile != null) {
                constraints[3] = leftTile.getRight().getValue();
            }
        }

        return constraints;
    }
}
