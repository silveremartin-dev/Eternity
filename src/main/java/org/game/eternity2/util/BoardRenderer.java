package org.game.eternity2.util;

import javafx.geometry.Insets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to render Eternity puzzles.
 */
public class BoardRenderer {

    private static final Map<Integer, Paint> PAINT_CACHE = new HashMap<>();

    public static Paint getEdgePaint(int pattern) {
        if (pattern == 0) return Color.GRAY;
        if (pattern < 0) return Color.WHITE;
        
        return PAINT_CACHE.computeIfAbsent(pattern, p -> {
            double goldenRatioConjugate = 0.618033988749895;
            double h = (p * goldenRatioConjugate * 360) % 360;
            return Color.hsb(h, 0.7, 0.9);
        });
    }

    public static String formatScore(int score, int width, int height) {
        int total = (width - 1) * height + width * (height - 1) + 2 * width + 2 * height;
        if (total == 0) return String.valueOf(score);
        double percent = (double) score / total * 100;
        return String.format("%d/%d (%.1f%%)", score, total, percent);
    }

    public static void renderBoard(GridPane grid, BoardPrimitive board, double maxWidth, double maxHeight) {
        grid.getChildren().clear();
        if (board == null) return;

        int bw = board.getWidth();
        int bh = board.getHeight();
        double cellSize = Math.min(maxWidth / bw, maxHeight / bh);

        for (int y = 0; y < bh; y++) {
            for (int x = 0; x < bw; x++) {
                StackPane cell = createCell(board.getPiece(x, y), cellSize);
                grid.add(cell, x, y);
                GridPane.setMargin(cell, new Insets(0.2));
            }
        }
    }

    private static StackPane createCell(long piece, double size) {
        StackPane cell = new StackPane();
        cell.setPrefSize(size, size);
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(size, size);
        drawPiece(canvas.getGraphicsContext2D(), piece, 0, 0, (int)size);
        cell.getChildren().add(canvas);
        return cell;
    }

    public static void drawPiece(GraphicsContext gc, long piece, int x, int y, int size) {
        double s = size;
        gc.save();
        gc.translate(x, y);

        if (piece == 0) {
            gc.setFill(Color.web("#1a1a2e"));
            gc.fillRect(0, 0, s, s);
            gc.setStroke(Color.web("#2a2a3e"));
            gc.setLineWidth(0.5);
            gc.strokeRect(0, 0, s, s);
        } else {
            gc.setFill(Color.web("#2d2d44"));
            gc.fillRect(0, 0, s, s);

            drawTriangle(gc, PiecePrimitive.getTop(piece), 0, s);
            drawTriangle(gc, PiecePrimitive.getRight(piece), 1, s);
            drawTriangle(gc, PiecePrimitive.getBottom(piece), 2, s);
            drawTriangle(gc, PiecePrimitive.getLeft(piece), 3, s);

            gc.setStroke(Color.web("#444466"));
            gc.setLineWidth(0.5);
            gc.strokeRect(0, 0, s, s);
        }
        gc.restore();
    }

    public static void drawTriangle(GraphicsContext gc, int pattern, int direction, double size) {
        double half = size / 2;
        gc.save();
        gc.beginPath();
        switch (direction) {
            case 0 -> { gc.moveTo(0, 0); gc.lineTo(size, 0); gc.lineTo(half, half); }
            case 1 -> { gc.moveTo(size, 0); gc.lineTo(size, size); gc.lineTo(half, half); }
            case 2 -> { gc.moveTo(0, size); gc.lineTo(size, size); gc.lineTo(half, half); }
            case 3 -> { gc.moveTo(0, 0); gc.lineTo(0, size); gc.lineTo(half, half); }
        }
        gc.closePath();
        gc.clip();
        gc.setFill(getEdgePaint(pattern));
        gc.fill();
        gc.restore();
    }
}
