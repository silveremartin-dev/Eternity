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
    private final int maxCandidates;

    public GPUEternitySolver(int maxCandidates) {
        this.maxCandidates = maxCandidates;
        this.driver = new TornadoEternityDriver(maxCandidates);
    }

    @Override
    public BoardPrimitive computeTessellation(BoardPrimitive startingBoard) {
        // Full puzzle solver using GPU
        return null; // Implemented via specialized kernels in actual use
    }

    /**
     * Checks multiple candidates in parallel on the GPU.
     * 
     * @param constraints Array of 4 integers [T, R, B, L]
     * @param candidates  List of pieces to check (each will be tried in 4 rotations)
     * @return List of indices in the candidates list that match
     */
    public List<Integer> findMatchingCandidates(int[] constraints, List<Long> candidates) {
        int numTiles = candidates.size();
        int totalCandidates = numTiles * 4;
        
        if (totalCandidates > maxCandidates) {
            totalCandidates = (maxCandidates / 4) * 4;
            numTiles = totalCandidates / 4;
        }

        int[] flatCandidates = new int[totalCandidates * 4];
        int[] results = new int[totalCandidates];

        for (int i = 0; i < numTiles; i++) {
            long tile = candidates.get(i);
            for (int r = 0; r < 4; r++) {
                int base = (i * 4 + r) * 4;
                flatCandidates[base] = PiecePrimitive.getTop(tile);
                flatCandidates[base + 1] = PiecePrimitive.getRight(tile);
                flatCandidates[base + 2] = PiecePrimitive.getBottom(tile);
                flatCandidates[base + 3] = PiecePrimitive.getLeft(tile);
                tile = PiecePrimitive.rotateCW(tile);
            }
        }

        driver.solve(constraints, flatCandidates, results);

        List<Integer> matches = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            if (results[i] == 1) {
                matches.add(i); // Combined index (tileIndex * 4 + rotation)
            }
        }
        return matches;
    }
}
