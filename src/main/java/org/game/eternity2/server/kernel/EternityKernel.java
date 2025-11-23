package org.game.eternity2.server.kernel;

/**
 * Kernel for parallel candidate evaluation.
 * Designed to be compatible with TornadoVM for future GPU acceleration.
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
        // For TornadoVM, we would use the @Parallel annotation here on a loop
        // for (int i = 0; i < results.length; i++) {

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
