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
package org.game.eternity2.kernel;

/**
 * Kernel for parallel candidate evaluation.
 * Designed to be compatible with TornadoVM for future GPU acceleration.
 * @author Silvere Martin-Michiellot
 * @author Antigravity
 * @since 1.0
 */
public class EternityKernel {

    /**
     * Checks which candidates are valid for a given set of constraints using bitwise logic.
     * Optimized for TornadoVM with Resident Pool strategy.
     * 
     * @param packedConstraints Array of 1 int: (Top << 24 | Right << 16 | Bottom << 8 | Left)
     * @param mask              Array of 1 int: Bitmask for active constraints.
     * @param candidates        Array of packed pieces (Resident Pool).
     * @param results           Output array.
     */
    public static void checkCandidatesBitwise(int[] packedConstraints, int[] mask, int[] candidates, int[] results) {
        for (int i = 0; i < results.length; i++) {
            results[i] = ((candidates[i] & mask[0]) == packedConstraints[0]) ? 1 : 0;
        }
    }

    /**
     * Legacy support for the 4-int array structure, internally using the bitwise logic.
     */
    public static void checkCandidates(int[] constraints, int[] candidates, int[] results) {
        int packedConstraints = 0;
        int mask = 0;

        for (int i = 0; i < 4; i++) {
            if (constraints[i] != -1) {
                packedConstraints |= (constraints[i] & 0xFF) << (8 * (3 - i));
                mask |= 0xFF << (8 * (3 - i));
            }
        }

        for (int i = 0; i < results.length; i++) {
            int baseIndex = i * 4;
            int packedCandidate = ((candidates[baseIndex] & 0xFF) << 24)
                    | ((candidates[baseIndex + 1] & 0xFF) << 16)
                    | ((candidates[baseIndex + 2] & 0xFF) << 8)
                    | (candidates[baseIndex + 3] & 0xFF);

            results[i] = ((packedCandidate & mask) == packedConstraints) ? 1 : 0;
        }
    }
}
