package org.game.eternity2.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Unified model for Eternity II puzzles.
 */
public class UnifiedPuzzle {
    public int width;
    public int height;
    public int patterns;
    public List<PieceData> pieces = new ArrayList<>();
    public List<HintData> hints = new ArrayList<>();
    public BoardData currentBoard;
    public StatisticsData stats;

    public static class PieceData {
        public int id;
        public int top, right, bottom, left;
        
        public PieceData() {}
        public PieceData(int id, int t, int r, int b, int l) {
            this.id = id;
            this.top = t;
            this.right = r;
            this.bottom = b;
            this.left = l;
        }
    }

    public static class HintData {
        public int x, y;
        public int pieceId;
        public int rotation;

        public HintData() {}
        public HintData(int x, int y, int pieceId, int rotation) {
            this.x = x;
            this.y = y;
            this.pieceId = pieceId;
            this.rotation = rotation;
        }
    }

    public static class BoardData {
        public List<PlacementData> placements = new ArrayList<>();
    }

    public static class PlacementData {
        public int x, y;
        public int pieceId;
        public int rotation;
    }

    public static class StatisticsData {
        public long totalBacktracks;
        public long totalTimeMs;
        public int bestScore;
    }
}
