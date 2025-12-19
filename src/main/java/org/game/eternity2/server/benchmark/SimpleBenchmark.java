package org.game.eternity2.server.benchmark;

import org.game.eternity2.server.kernel.EternityKernel;

import java.util.Random;

/**
 * Simple benchmark for EternityKernel without JMH dependencies.
 * Run directly with: java -cp target/eternity-1.0-SNAPSHOT.jar
 * org.game.eternity2.server.benchmark.SimpleBenchmark
 */
public class SimpleBenchmark {

    private static final int BATCH_SIZE = 1000;
    private static final int ITERATIONS = 100;

    public static void main(String[] args) {
        System.out.println("=== Eternity Kernel Simple Benchmark ===");
        System.out.println("Batch size: " + BATCH_SIZE);
        System.out.println("Iterations: " + ITERATIONS);
        System.out.println();

        // Setup test data
        int[] constraints = new int[4];
        constraints[0] = 1; // Top
        constraints[1] = 0; // Right (wildcard)
        constraints[2] = 0; // Bottom (wildcard)
        constraints[3] = 2; // Left

        int[] candidates = new int[BATCH_SIZE * 6];
        int[] results = new int[BATCH_SIZE];

        Random random = new Random(42);
        for (int i = 0; i < BATCH_SIZE; i++) {
            int offset = i * 6;
            candidates[offset] = i; // ID
            candidates[offset + 1] = random.nextInt(20); // Top
            candidates[offset + 2] = random.nextInt(20); // Right
            candidates[offset + 3] = random.nextInt(20); // Bottom
            candidates[offset + 4] = random.nextInt(20); // Left
            candidates[offset + 5] = 0; // Rotation
        }

        // Warmup
        System.out.println("Warmup...");
        for (int i = 0; i < 10; i++) {
            EternityKernel.checkCandidates(constraints, candidates, results);
        }

        // Benchmark
        System.out.println("Running benchmark...");
        long totalTime = 0;
        long totalCandidates = 0;

        for (int iteration = 0; iteration < ITERATIONS; iteration++) {
            long start = System.nanoTime();
            EternityKernel.checkCandidates(constraints, candidates, results);
            long end = System.nanoTime();

            totalTime += (end - start);
            totalCandidates += BATCH_SIZE;
        }

        // Results
        double avgTimeMs = totalTime / 1_000_000.0 / ITERATIONS;
        double candidatesPerSec = (totalCandidates / (totalTime / 1_000_000_000.0));
        System.out.println();
        System.out.println("=== Results ===");
        System.out.println("Average time per batch: " + String.format("%.3f", avgTimeMs) + " ms");
        System.out.println("Candidates checked: " + totalCandidates);
        System.out.println("Throughput: " + String.format("%.0f", candidatesPerSec) + " candidates/sec");
        System.out.println("Throughput: " + String.format("%.2f", candidatesPerSec / 1_000_000) + " M candidates/sec");
    }
}
