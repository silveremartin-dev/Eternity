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
public class KernelBenchmark {

    private EternityKernel kernel;
    private int[] constraints;
    private int[] candidates;
    private int[] results;
    private static final int BATCH_SIZE = 1000;

    @Setup
    public void setup() {
        kernel = new EternityKernel();
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
