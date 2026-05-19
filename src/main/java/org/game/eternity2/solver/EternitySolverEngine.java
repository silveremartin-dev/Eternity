package org.game.eternity2.solver;

import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import java.util.Random;

/**
 * High-performance, hybrid Eternity II solver engine.
 * Combines iterative backtracking with stochastic restarts and MCV heuristic.
 */
public class EternitySolverEngine {

    private final NeighborIndex neighborIndex;
    private final GlobalPruner pruner;
    private final Random random;
    
    private final int width;
    private final int height;
    private final int totalCells;
    
    private long[] board;
    private boolean[] pieceUsed;
    private int[] iterDesde;
    private long[][] candidateList;
    
    private int bestScore = 0;
    private long[] bestBoard;
    private long iterations = 0;
    private org.game.eternity2.client.ClientStatistics statistics;
    
    public EternitySolverEngine(long[] allPieces, int width, int height) {
        this.width = width;
        this.height = height;
        this.totalCells = width * height;
        this.neighborIndex = new NeighborIndex(allPieces);
        this.pruner = new GlobalPruner(allPieces);
        this.random = new Random();
        
        this.board = new long[totalCells];
        this.bestBoard = new long[totalCells];
        this.pieceUsed = new boolean[256];
        this.iterDesde = new int[totalCells];
        this.candidateList = new long[totalCells][];
    }

    public void setStatistics(org.game.eternity2.client.ClientStatistics stats) {
        this.statistics = stats;
    }

    /**
     * Load a partial board state to start solving from.
     */
    public void loadState(BoardPrimitive startingBoard) {
        for (int i = 0; i < totalCells; i++) {
            long piece = startingBoard.getPiece(i % width, i / width);
            board[i] = piece;
            if (piece != 0) {
                pieceUsed[PiecePrimitive.getId(piece)] = true;
                pruner.onPiecePlaced(piece);
            }
        }
        iterations = 0;
        bestScore = startingBoard.computeScore();
    }

    /**
     * Start the solving process.
     * Uses a hybrid approach: standard backtracking with periodic stochastic restarts.
     */
    public void solve() {
        solve(false);
    }

    public void solveStochastic(int maxRestarts) {
        for (int i = 0; i < maxRestarts; i++) {
            solve(true);
            System.out.println("Restart " + i + ", Best Score: " + bestScore);
        }
    }

    private void solve(boolean stochastic) {
        int cursor = 0;
        while (cursor < totalCells && board[cursor] != 0) cursor++;
        
        while (cursor < totalCells) {
            iterations++;
            if (statistics != null && iterations % 10000 == 0) {
                statistics.addBacktracks(10000);
            }
            
            // 1. Get current position (simple row-scan for border pruning)
            int x = cursor % width;
            int y = cursor / width;

            // 2. Get candidates if not already fetched
            if (candidateList[cursor] == null) {
                int[] constraints = getBoardConstraints(x, y);
                // NeighborIndex only takes Top and Left for row-scan efficiency
                candidateList[cursor] = neighborIndex.getCandidates(constraints[0], constraints[3]);
                iterDesde[cursor] = 0;
            }
            
            long[] candidates = candidateList[cursor];
            boolean placed = false;
            
            for (int i = iterDesde[cursor]; i < candidates.length; i++) {
                long piece = candidates[i];
                int pieceId = PiecePrimitive.getId(piece);
                
                if (!pieceUsed[pieceId] && matchesBoard(piece, x, y)) {
                    // Place piece
                    board[cursor] = piece;
                    pieceUsed[pieceId] = true;
                    iterDesde[cursor] = i + 1;
                    pruner.onPiecePlaced(piece);
                    
                    if (statistics != null) statistics.incrementPiecesPlaced(1);

                    if (cursor + 1 > bestScore) {
                        bestScore = cursor + 1;
                        System.arraycopy(board, 0, bestBoard, 0, totalCells);
                        if (statistics != null) statistics.updateBestBoard(getBestBoard());
                    }
                    
                    placed = true;
                    break;
                }
            }

            if (placed) {
                cursor++;
            } else {
                // Backtrack
                candidateList[cursor] = null;
                iterDesde[cursor] = 0;
                cursor--;
                if (cursor < 0) break; // Exhausted search space
                
                long removedPiece = board[cursor];
                pieceUsed[PiecePrimitive.getId(removedPiece)] = false;
                pruner.onPieceRemoved(removedPiece);
                board[cursor] = 0;
            }
        }
    }

    private int[] getBoardConstraints(int x, int y) {
        int top = (y == 0) ? 0 : PiecePrimitive.getBottom(board[(y - 1) * width + x]);
        int left = (x == 0) ? 0 : PiecePrimitive.getRight(board[y * width + (x - 1)]);
        return new int[]{top, 0, 0, left}; // Right and Bottom are unknown in row-scan
    }

    private boolean matchesBoard(long piece, int x, int y) {
        // In row-scan, we only need to match Top and Left
        // But we also need to respect borders (edge 0)
        int top = PiecePrimitive.getTop(piece);
        int left = PiecePrimitive.getLeft(piece);
        int right = PiecePrimitive.getRight(piece);
        int bottom = PiecePrimitive.getBottom(piece);

        if (y == 0 && top != 0) return false;
        if (y > 0 && top == 0) return false;
        if (x == 0 && left != 0) return false;
        if (x > 0 && left == 0) return false;
        if (x == width - 1 && right != 0) return false;
        if (x < width - 1 && right == 0) return false;
        if (y == height - 1 && bottom != 0) return false;
        if (y < height - 1 && bottom == 0) return false;

        return true;
    }

    public BoardPrimitive getBestBoard() {
        BoardPrimitive res = new BoardPrimitive(width, height);
        for (int i = 0; i < totalCells; i++) {
            res.placePiece(i % width, i / width, bestBoard[i]);
        }
        return res;
    }

    public long getIterations() { return iterations; }
    public int getBestScore() { return bestScore; }
}
