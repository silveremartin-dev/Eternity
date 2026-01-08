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
package org.game.eternity2.server.benchmark;

import org.game.eternity2.server.kernel.EternityKernel;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
/**
 * @author Silvere Martin-Michiellot
 * @author Antigravity
 * @since 1.0
 */
public class KernelBenchmark {

    // private EternityKernel kernel; // Unused after static method change
    private int[] constraints;
    private int[] candidates;
    private int[] results;
    private static final int BATCH_SIZE = 1000;

    @Setup
    public void setup() {
        // kernel = new EternityKernel(); // Removed unused instantiation
        constraints = new int[4]; // Top, Right, Bottom, Left
        // Example constraints: Top=1, Right=0 (wildcard), Bottom=0, Left=2
        constraints[0] = 1;
        constraints[1] = 0;
        constraints[2] = 0;
        constraints[3] = 2;

        candidates = new int[BATCH_SIZE * 6]; // 6 ints per candidate (ID, T, R, B, L, Rot)
        results = new int[BATCH_SIZE];

        Random random = new Random(42);
        for (int i = 0; i < BATCH_SIZE; i++) {
            int offset = i * 6;
            candidates[offset] = i; // ID
            candidates[offset + 1] = random.nextInt(20); // Top
            candidates[offset + 2] = random.nextInt(20); // Right
            candidates[offset + 3] = random.nextInt(20); // Bottom
            candidates[offset + 4] = random.nextInt(20); // Left
            candidates[offset + 5] = 0; // Rotation (simplified)
        }
    }

    @Benchmark
    public void testCheckCandidates(Blackhole bh) {
        EternityKernel.checkCandidates(constraints, candidates, results);
        bh.consume(results);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
