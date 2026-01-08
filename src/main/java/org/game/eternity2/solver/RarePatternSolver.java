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

import java.util.*;

/**
 * Solver that prioritizes pieces with rare edge patterns.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class RarePatternSolver implements EternitySolverInterface {

    private final long[] allPieces;

    public RarePatternSolver() {
        this.allPieces = PuzzleLoaderWriter.generateEternity2Pieces();
    }

    @Override
    public BoardPrimitive computeTessellation(BoardPrimitive startingBoard) {
        // 1. Analyze pattern counts
        Map<Integer, Integer> patternCounts = countPatterns(startingBoard);

        // 2. Get unused tiles
        List<Long> unusedTiles = getUnusedTiles(startingBoard);

        // 3. Sort tiles by rarity (rare patterns first)
        unusedTiles.sort(Comparator.comparingInt(t -> calculateTileRarityScore(t, patternCounts)));

        // 4. Solve with backtracking
        return solve(new BoardPrimitive(startingBoard), unusedTiles, 0);
    }

    private Map<Integer, Integer> countPatterns(BoardPrimitive board) {
        Map<Integer, Integer> counts = new HashMap<>();
        // Count from all pieces in the set
        for (long piece : allPieces) {
            counts.merge(PiecePrimitive.getTop(piece), 1, (a, b) -> a + b);
            counts.merge(PiecePrimitive.getRight(piece), 1, (a, b) -> a + b);
            counts.merge(PiecePrimitive.getBottom(piece), 1, (a, b) -> a + b);
            counts.merge(PiecePrimitive.getLeft(piece), 1, (a, b) -> a + b);
        }
        return counts;
    }

    private int calculateTileRarityScore(long piece, Map<Integer, Integer> counts) {
        // Piece rarity is the minimum count of any of its patterns
        int min = Integer.MAX_VALUE;
        min = Math.min(min, counts.getOrDefault(PiecePrimitive.getTop(piece), 100));
        min = Math.min(min, counts.getOrDefault(PiecePrimitive.getRight(piece), 100));
        min = Math.min(min, counts.getOrDefault(PiecePrimitive.getBottom(piece), 100));
        min = Math.min(min, counts.getOrDefault(PiecePrimitive.getLeft(piece), 100));
        return min;
    }

    private BoardPrimitive solve(BoardPrimitive board, List<Long> pieces, int pieceIndex) {
        // Find next empty position
        int[] nextPos = findNextEmpty(board);
        if (nextPos == null) {
            return board; // Solved
        }
        int x = nextPos[0];
        int y = nextPos[1];

        int[] constraints = board.getConstraints(x, y);

        for (int i = 0; i < pieces.size(); i++) {
            long piece = pieces.get(i);

            // Try all 4 rotations
            for (int r = 0; r < 4; r++) {
                if (PiecePrimitive.matches(piece, constraints[0], constraints[1], constraints[2], constraints[3])) {
                    board.placePiece(x, y, piece);

                    Long removed = pieces.remove(i);
                    BoardPrimitive result = solve(board, pieces, pieceIndex);
                    if (result != null)
                        return result;

                    // Backtrack
                    board.removePiece(x, y);
                    pieces.add(i, removed);
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

    private int[] findNextEmpty(BoardPrimitive board) {
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (board.isEmpty(x, y)) {
                    return new int[] { x, y };
                }
            }
        }
        return null;
    }
}
