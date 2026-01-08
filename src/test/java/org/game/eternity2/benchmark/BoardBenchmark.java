package org.game.eternity2.benchmark;

import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(value = 1)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
public class BoardBenchmark {

    private BoardPrimitive board;
    private long piece;

    @Setup
    public void setup() {
        board = new BoardPrimitive(16, 16);
        piece = PiecePrimitive.create(1, 1, 1, 1, 1);

        // Pre-fill some pieces
        for (int i = 0; i < 10; i++) {
            board.placePiece(i, 0, piece);
        }
    }

    @Benchmark
    public void testComputeScore(Blackhole bh) {
        bh.consume(board.computeScore());
    }

    @Benchmark
    public void testPlaceAndRemove(Blackhole bh) {
        board.placePiece(5, 5, piece);
        board.removePiece(5, 5);
        bh.consume(board);
    }

    @Benchmark
    public void testGetConstraints(Blackhole bh) {
        bh.consume(board.getConstraints(5, 5));
    }
}
