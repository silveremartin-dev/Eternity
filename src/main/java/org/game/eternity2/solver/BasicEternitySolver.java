/*
 * MIT License
 *
 * Copyright (c) 2026 Silvere Martin-Michiellot, Antigravity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.game.eternity2.solver;

import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import org.game.eternity2.io.PuzzleLoaderWriter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A basic brute-force backtracking solver.
 * Optimized for primitive models.
 * 
 * @author Silvere Martin-Michiellot
 * @author Antigravity
 * @since 1.0
 */
public class BasicEternitySolver implements EternitySolverInterface {

    private final long[] allPieces;

    public BasicEternitySolver() {
        this.allPieces = PuzzleLoaderWriter.generateEternity2Pieces();
    }

    @Override
    public BoardPrimitive computeTessellation(BoardPrimitive startingBoard) {
        List<Long> unusedTiles = getUnusedTiles(startingBoard);
        return solve(new BoardPrimitive(startingBoard), unusedTiles);
    }

    private BoardPrimitive solve(BoardPrimitive board, List<Long> tiles) {
        if (board.isComplete()) {
            return board;
        }

        int[] nextPos = board.findMostConstrainedPosition();
        if (nextPos[0] == -1) {
            return board; // Should not happen if not complete
        }
        int x = nextPos[0];
        int y = nextPos[1];

        int[] constraints = board.getConstraints(x, y);

        for (int i = 0; i < tiles.size(); i++) {
            long piece = tiles.get(i);
            for (int r = 0; r < 4; r++) {
                if (PiecePrimitive.matches(piece, constraints[0], constraints[1], constraints[2], constraints[3])) {
                    board.placePiece(x, y, piece);

                    List<Long> remaining = new ArrayList<>(tiles);
                    remaining.remove(i);

                    BoardPrimitive result = solve(board, remaining);
                    if (result != null)
                        return result;

                    // Backtrack
                    board.removePiece(x, y);
                }
                piece = PiecePrimitive.rotateCW(piece);
            }
        }
        return null;
    }

    private List<Long> getUnusedTiles(BoardPrimitive board) {
        Set<Integer> usedIds = new HashSet<>();
        for (long cell : board.getCells()) {
            if (cell != 0) {
                usedIds.add(PiecePrimitive.getId(cell));
            }
        }
        List<Long> unused = new ArrayList<>();
        for (long p : allPieces) {
            if (!usedIds.contains(PiecePrimitive.getId(p))) {
                unused.add(p);
            }
        }
        return unused;
    }
}
