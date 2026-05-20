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
    public List<JobData> pendingJobs = new ArrayList<>();
    public StatisticsData stats;

    public int getPatterns() {
        if (pieces != null && !pieces.isEmpty()) {
            java.util.Set<Integer> unique = new java.util.HashSet<>();
            for (PieceData p : pieces) {
                if (p.top > 0) unique.add(p.top);
                if (p.right > 0) unique.add(p.right);
                if (p.bottom > 0) unique.add(p.bottom);
                if (p.left > 0) unique.add(p.left);
            }
            patterns = unique.size();
        }
        return patterns;
    }

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

        public PlacementData() {}
        public PlacementData(int x, int y, int pieceId, int rotation) {
            this.x = x;
            this.y = y;
            this.pieceId = pieceId;
            this.rotation = rotation;
        }
    }

    public static class JobData {
        public String jobId;
        public List<PlacementData> initialBoardPlacements = new ArrayList<>();
        public List<PositionData> positionsToFill = new ArrayList<>();
        public String strategyName;
    }

    public static class PositionData {
        public int row;
        public int col;
        
        public PositionData() {}
        public PositionData(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public static class StatisticsData {
        public long totalBacktracks;
        public long totalTimeMs;
        public int bestScore;
        public int totalPackets;
        public String solverEngine;
        public String timestamp;
    }
}
