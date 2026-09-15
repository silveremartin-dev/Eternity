package org.game.eternity2.solver;

import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import java.util.Random;

/**
 * Stochastic refinement engine using local search (Hill Climbing).
 * Optimized for finding high-score configurations by swapping pieces.
 */
public class StochasticRefinement {

    private final int width;
    private final int height;
    private final Random random;
    private long[] board;
    private org.game.eternity2.client.ClientStatistics statistics;

    public StochasticRefinement(int width, int height) {
        this.width = width;
        this.height = height;
        this.random = new Random();
        this.board = new long[width * height];
    }

    public StochasticRefinement(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.random = new Random(seed);
        this.board = new long[width * height];
    }

    public void setSeed(long seed) {
        this.random.setSeed(seed);
    }

    public void setStatistics(org.game.eternity2.client.ClientStatistics stats) {
        this.statistics = stats;
    }

    public void initializeRandom(long[] allPieces) {
        // Shuffle pieces and fill board
        long[] shuffled = allPieces.clone();
        for (int i = shuffled.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            long temp = shuffled[i];
            shuffled[i] = shuffled[j];
            shuffled[j] = temp;
        }
        System.arraycopy(shuffled, 0, board, 0, Math.min(shuffled.length, board.length));
    }

    public void initializeFromBoard(BoardPrimitive initialBoard, long[] allPieces) {
        for (int i = 0; i < board.length; i++) {
            board[i] = initialBoard.getPiece(i % width, i / width);
        }
    }

    public BoardPrimitive getBestBoard() {
        BoardPrimitive res = new BoardPrimitive(width, height);
        for (int i = 0; i < board.length; i++) {
            res.placePiece(i % width, i / width, board[i]);
        }
        return res;
    }

    /**
     * Perform local search to minimize mismatches.
     */
    public void refine(int maxIterations) {
        int currentMismatches = countMismatches();
        
        for (int i = 0; i < maxIterations; i++) {
            if (statistics != null && i % 10000 == 0) {
                statistics.addBacktracks(10000);
                statistics.incrementPiecesPlaced(10000);
            }

            // 50% swap two pieces, 50% rotate one piece
            if (random.nextBoolean()) {
                int p1 = random.nextInt(board.length);
                int p2 = random.nextInt(board.length);

                long t1 = board[p1];
                long t2 = board[p2];

                board[p1] = t2;
                board[p2] = t1;

                int newMismatches = countMismatches();
                if (newMismatches < currentMismatches) {
                    currentMismatches = newMismatches;
                    if (statistics != null) statistics.updateBestBoard(getBestBoard());
                    if (i % 1000 == 0) {
                        System.out.println("Iteration " + i + ", Mismatches: " + currentMismatches);
                    }
                    if (currentMismatches == 0) break;
                } else {
                    board[p1] = t1;
                    board[p2] = t2;
                }
            } else {
                int p = random.nextInt(board.length);
                long orig = board[p];
                if (orig != 0) {
                    long rotated = PiecePrimitive.rotateCW(orig);
                    board[p] = rotated;
                    int newMismatches = countMismatches();
                    if (newMismatches < currentMismatches) {
                        currentMismatches = newMismatches;
                        if (statistics != null) statistics.updateBestBoard(getBestBoard());
                        if (i % 1000 == 0) {
                            System.out.println("Iteration " + i + ", Mismatches: " + currentMismatches);
                        }
                        if (currentMismatches == 0) break;
                    } else {
                        board[p] = orig;
                    }
                }
            }
        }
    }

    private int countMismatches() {
        int mismatches = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long p = board[y * width + x];
                if (p == 0) continue;

                // Check Top
                if (y > 0) {
                    long top = board[(y - 1) * width + x];
                    if (top != 0 && PiecePrimitive.getBottom(top) != PiecePrimitive.getTop(p)) mismatches++;
                } else if (PiecePrimitive.getTop(p) != 0) {
                    mismatches++; // Top border mismatch
                }

                // Check Left
                if (x > 0) {
                    long left = board[y * width + (x - 1)];
                    if (left != 0 && PiecePrimitive.getRight(left) != PiecePrimitive.getLeft(p)) mismatches++;
                } else if (PiecePrimitive.getLeft(p) != 0) {
                    mismatches++; // Left border mismatch
                }

                // Check Bottom border
                if (y == height - 1 && PiecePrimitive.getBottom(p) != 0) {
                    mismatches++;
                }

                // Check Right border
                if (x == width - 1 && PiecePrimitive.getRight(p) != 0) {
                    mismatches++;
                }
            }
        }
        return mismatches;
    }

    public long[] getBoard() { return board; }
}
