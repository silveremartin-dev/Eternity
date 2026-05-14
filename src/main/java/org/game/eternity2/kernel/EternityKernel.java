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
     * Checks which candidates are valid for a given set of constraints.
     * 
     * @param constraints Array of 4 integers representing the required pattern for
     *                    [Top, Right, Bottom, Left].
     *                    Use -1 if there is no constraint (e.g., empty neighbor).
     *                    For borders, use the specific border pattern value
     *                    (usually 0).
     * @param candidates  Flattened array of candidate tiles. Each tile is 4
     *                    integers [Top, Right, Bottom, Left].
     *                    Length = num_candidates * 4.
     * @param results     Output array. 1 if valid, 0 if invalid. Length =
     *                    num_candidates.
     */
    public static void checkCandidates(int[] constraints, int[] candidates, int[] results) {
        // CPU implementation (mimics the parallel kernel structure)
        for (int i = 0; i < results.length; i++) {
            int baseIndex = i * 4;
            int cTop = candidates[baseIndex];
            int cRight = candidates[baseIndex + 1];
            int cBottom = candidates[baseIndex + 2];
            int cLeft = candidates[baseIndex + 3];

            boolean valid = true;

            // Check Top Constraint
            if (constraints[0] != -1) {
                if (cTop != constraints[0]) {
                    valid = false;
                }
            }

            // Check Right Constraint
            if (valid && constraints[1] != -1) {
                if (cRight != constraints[1]) {
                    valid = false;
                }
            }

            // Check Bottom Constraint
            if (valid && constraints[2] != -1) {
                if (cBottom != constraints[2]) {
                    valid = false;
                }
            }

            // Check Left Constraint
            if (valid && constraints[3] != -1) {
                if (cLeft != constraints[3]) {
                    valid = false;
                }
            }

            results[i] = valid ? 1 : 0;
        }
    }
}
