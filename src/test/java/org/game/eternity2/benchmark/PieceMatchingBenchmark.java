package org.game.eternity2.benchmark;

import org.game.eternity2.model.PiecePrimitive;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 1, jvmArgs = { "-Xms2G", "-Xmx2G" })
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class PieceMatchingBenchmark {

    private long piece;
    private int top, right, bottom, left;

    @Setup
    public void setup() {
        // Create a sample piece: ID 1, Colors 1, 2, 3, 4
        piece = PiecePrimitive.create(1, 1, 2, 3, 4);
        top = 1;
        right = 2;
        bottom = 3;
        left = 4;
    }

    @Benchmark
    public void testFullMatch(Blackhole bh) {
        bh.consume(PiecePrimitive.matches(piece, top, right, bottom, left));
    }

    @Benchmark
    public void testPartialMatch(Blackhole bh) {
        // Match with some -1 (ignore) constraints
        bh.consume(PiecePrimitive.matches(piece, 1, -1, -1, 4));
    }

    @Benchmark
    public void testRotationMatch(Blackhole bh) {
        // Rotate and match
        long rotated = PiecePrimitive.rotateCW(piece);
        bh.consume(PiecePrimitive.matches(rotated, 4, 1, 2, 3));
    }
}
