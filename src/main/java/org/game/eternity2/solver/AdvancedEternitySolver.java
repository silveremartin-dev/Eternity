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
import org.game.eternity2.server.kernel.EternityKernel;
import org.game.eternity2.server.kernel.TornadoEternityDriver;
import org.game.eternity2.io.PuzzleLoaderWriter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An algorithm to solve the puzzle using "Parallel Candidate Evaluation".
 * This solver leverages the EternityKernel (CPU or GPU) to check multiple
 * candidates at once.
 * Optimized for primitive models.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class AdvancedEternitySolver implements EternitySolverInterface {

    private final long[] allPieces;
    private TornadoEternityDriver gpuDriver;
    private boolean useGpu;

    public AdvancedEternitySolver() {
        this.allPieces = PuzzleLoaderWriter.generateEternity2Pieces();
        try {
            // Assume 1024 candidates max for a single evaluation (enough for 256 tiles * 4
            // rotations)
            this.gpuDriver = new TornadoEternityDriver(1024);
            this.useGpu = true;
        } catch (Throwable e) {
            this.useGpu = false;
        }
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
            return board;
        }
        int x = nextPos[0];
        int y = nextPos[1];

        // 1. Prepare Constraints
        int[] constraints = board.getConstraints(x, y);

        // 2. Prepare Candidates (All tiles * 4 rotations)
        int numTiles = tiles.size();
        int[] candidates = new int[numTiles * 4 * 4];

        for (int i = 0; i < numTiles; i++) {
            long tile = tiles.get(i);
            for (int r = 0; r < 4; r++) {
                int base = (i * 4 + r) * 4;
                candidates[base] = PiecePrimitive.getTop(tile);
                candidates[base + 1] = PiecePrimitive.getRight(tile);
                candidates[base + 2] = PiecePrimitive.getBottom(tile);
                candidates[base + 3] = PiecePrimitive.getLeft(tile);
                tile = PiecePrimitive.rotateCW(tile);
            }
        }

        // 3. Call Kernel (GPU or CPU)
        int[] results = new int[numTiles * 4];
        if (useGpu && results.length <= 1024) {
            gpuDriver.solve(constraints, candidates, results);
        } else {
            EternityKernel.checkCandidates(constraints, candidates, results);
        }

        // 4. Process Results
        for (int i = 0; i < results.length; i++) {
            if (results[i] == 1) {
                int tileIndex = i / 4;
                int rotationIndex = i % 4;

                long originalTile = tiles.get(tileIndex);
                long rotatedTile = originalTile;
                for (int r = 0; r < rotationIndex; r++) {
                    rotatedTile = PiecePrimitive.rotateCW(rotatedTile);
                }

                board.placePiece(x, y, rotatedTile);

                List<Long> remaining = new ArrayList<>(tiles);
                remaining.remove(tileIndex);

                BoardPrimitive result = solve(board, remaining);
                if (result != null) {
                    return result;
                }

                // Backtrack
                board.removePiece(x, y);
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
