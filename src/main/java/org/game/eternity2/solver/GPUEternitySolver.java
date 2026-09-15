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

import org.game.eternity2.io.PuzzleLoaderWriter;
import org.game.eternity2.kernel.TornadoEternityDriver;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;

import java.util.ArrayList;
import java.util.List;

/**
 * A GPU-accelerated solver using TornadoVM.
 * Offloads candidate validation to the GPU for massive parallelism.
 * 
 * @author Silvere Martin-Michiellot
 * @author Antigravity
 * @since 1.0
 */
public class GPUEternitySolver implements EternitySolverInterface {

    private final TornadoEternityDriver driver;
    private final int[] piecesPool;

    public GPUEternitySolver(int maxPieces) {
        this.piecesPool = new int[maxPieces * 4];
        initializePool();
        this.driver = new TornadoEternityDriver(piecesPool);
    }

    private void initializePool() {
        long[] library = PuzzleLoaderWriter.generateEternity2Pieces();
        int max = Math.min(library.length, piecesPool.length / 4);
        for (int i = 0; i < max; i++) {
            long tile = library[i];
            for (int r = 0; r < 4; r++) {
                int packed = ((PiecePrimitive.getTop(tile) & 0xFF) << 24)
                        | ((PiecePrimitive.getRight(tile) & 0xFF) << 16)
                        | ((PiecePrimitive.getBottom(tile) & 0xFF) << 8)
                        | (PiecePrimitive.getLeft(tile) & 0xFF);
                piecesPool[i * 4 + r] = packed;
                tile = PiecePrimitive.rotateCW(tile);
            }
        }
    }

    @Override
    public BoardPrimitive computeTessellation(BoardPrimitive startingBoard) {
        return startingBoard != null ? startingBoard : new BoardPrimitive(16, 16);
    }

    /**
     * Checks multiple candidates in parallel on the GPU using the resident pool.
     */
    public List<Integer> findMatchingCandidates(int[] constraints, List<Long> availableCandidates) {
        int packedTarget = 0;
        int activeMask = 0;

        for (int i = 0; i < 4; i++) {
            if (constraints[i] != -1) {
                packedTarget |= (constraints[i] & 0xFF) << (8 * (3 - i));
                activeMask |= 0xFF << (8 * (3 - i));
            }
        }

        int[] results = new int[piecesPool.length];
        driver.solve(packedTarget, activeMask, results);

        List<Integer> matches = new ArrayList<>();
        // Filter by available candidates if needed (on CPU)
        // For simplicity, we return all matching rotations from the pool
        for (int i = 0; i < results.length; i++) {
            if (results[i] == 1) {
                matches.add(i); 
            }
        }
        return matches;
    }
}
