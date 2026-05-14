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
package org.game.eternity2.util;

import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;

/**
 * Utility class to render a BoardPrimitive to a JavaFX GridPane.
 */
public class BoardRenderer {

    private static final Color[] EDGE_COLORS = new Color[23];
    public static final String[] PATTERN_NAMES = new String[23];
    private static final java.util.Map<Integer, javafx.scene.paint.Paint> PAINT_CACHE = new java.util.HashMap<>();

    static {
        EDGE_COLORS[0] = Color.GRAY; // Border
        PATTERN_NAMES[0] = "gray";
        PATTERN_NAMES[1] = "orangelightblue";
        PATTERN_NAMES[2] = "roseyellow2";
        PATTERN_NAMES[3] = "browngreen";
        PATTERN_NAMES[4] = "lightbluerose2";
        PATTERN_NAMES[5] = "greendarkblue";
        PATTERN_NAMES[6] = "purpleyellow";
        PATTERN_NAMES[7] = "lightbluerose";
        PATTERN_NAMES[8] = "darkblueorange";
        PATTERN_NAMES[9] = "darkblueyellow";
        PATTERN_NAMES[10] = "purplelightblue";
        PATTERN_NAMES[11] = "greenorange";
        PATTERN_NAMES[12] = "yellowdarkblue";
        PATTERN_NAMES[13] = "brownorange";
        PATTERN_NAMES[14] = "greenrose";
        PATTERN_NAMES[15] = "yellowgreen";
        PATTERN_NAMES[16] = "darkbluelightblue";
        PATTERN_NAMES[17] = "roselightblue";
        PATTERN_NAMES[18] = "yellowlightblue";
        PATTERN_NAMES[19] = "brownyellow";
        PATTERN_NAMES[20] = "orangepurple";
        PATTERN_NAMES[21] = "roseyellow";
        PATTERN_NAMES[22] = "darkbluerose";

        for (int i = 1; i < 23; i++) {
            EDGE_COLORS[i] = Color.hsb(i * (360.0 / 22), 0.8, 0.9);
        }
    }

    private static javafx.scene.paint.Paint getEdgePaint(int pattern) {
        if (pattern < 0 || pattern >= 23) return Color.WHITE;
        return PAINT_CACHE.computeIfAbsent(pattern, p -> {
            try {
                String path = "/images/patterns/" + PATTERN_NAMES[p] + ".gif";
                java.io.InputStream is = BoardRenderer.class.getResourceAsStream(path);
                if (is != null) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(is);
                    return new javafx.scene.paint.ImagePattern(img);
                }
            } catch (Exception e) {
                // Fallback to color
            }
            return getEdgeColor(p);
        });
    }

    public static int getTotalEdges(int width, int height) {
        if (width <= 0 || height <= 0) return 0;
        // Internal horizontal edges: (width-1) * height
        // Internal vertical edges: width * (height-1)
        // Border edges: 2*width + 2*height
        return (width - 1) * height + width * (height - 1) + 2 * width + 2 * height;
    }

    public static String formatScore(int score, int width, int height) {
        int total = getTotalEdges(width, height);
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
                javafx.scene.layout.StackPane cell = createCell(board.getPiece(x, y), cellSize);
                grid.add(cell, x, y);
                GridPane.setMargin(cell, new Insets(0.2));
            }
        }
    }

    private static javafx.scene.layout.StackPane createCell(long piece, double size) {
        javafx.scene.layout.StackPane cell = new javafx.scene.layout.StackPane();
        cell.setPrefSize(size, size);

        if (piece == 0) {
            Rectangle bg = new Rectangle(size - 0.5, size - 0.5);
            bg.setFill(Color.web("#1a1a2e"));
            cell.getChildren().add(bg);
        } else {
            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(size, size);
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Draw piece background
            gc.setFill(Color.web("#2d2d44"));
            gc.fillRect(0, 0, size, size);

            // Draw triangles
            drawTriangle(gc, PiecePrimitive.getTop(piece), 0, size);
            drawTriangle(gc, PiecePrimitive.getRight(piece), 1, size);
            drawTriangle(gc, PiecePrimitive.getBottom(piece), 2, size);
            drawTriangle(gc, PiecePrimitive.getLeft(piece), 3, size);

            // Draw border
            gc.setStroke(Color.web("#444466"));
            gc.setLineWidth(0.5);
            gc.strokeRect(0, 0, size, size);

            // Draw ID
            if (size > 15) {
                gc.save();
                String idStr = String.valueOf(PiecePrimitive.getId(piece));
                javafx.scene.text.Font font = javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, size / 4);
                gc.setFont(font);
                
                javafx.scene.text.Text text = new javafx.scene.text.Text(idStr);
                text.setFont(font);
                double textWidth = text.getLayoutBounds().getWidth();
                double tx = (size - textWidth) / 2;
                double ty = size / 1.7;

                // Shadow (offset)
                gc.setFill(Color.BLACK);
                gc.fillText(idStr, tx + 1, ty + 1);
                
                // Main text
                gc.setFill(Color.WHITE);
                gc.fillText(idStr, tx, ty);
                
                gc.restore();
            }
            
            cell.getChildren().add(canvas);
        }

        return cell;
    }

    public static void drawTriangle(javafx.scene.canvas.GraphicsContext gc, int pattern, int direction, double size) {
        if (pattern < 0 || pattern >= 23) return;
        
        Image img = null;
        try {
            String path = "/images/patterns/" + PATTERN_NAMES[pattern] + ".gif";
            java.io.InputStream is = BoardRenderer.class.getResourceAsStream(path);
            if (is != null) {
                img = new Image(is);
            }
        } catch (Exception e) {}

        double half = size / 2;
        gc.save();

        // Clip to the triangle
        gc.beginPath();
        switch (direction) {
            case 0 -> { gc.moveTo(0, 0); gc.lineTo(size, 0); gc.lineTo(half, half); }
            case 1 -> { gc.moveTo(size, 0); gc.lineTo(size, size); gc.lineTo(half, half); }
            case 2 -> { gc.moveTo(0, size); gc.lineTo(size, size); gc.lineTo(half, half); }
            case 3 -> { gc.moveTo(0, 0); gc.lineTo(0, size); gc.lineTo(half, half); }
        }
        gc.closePath();
        gc.clip();

        if (img != null) {
            // Draw rotated image
            gc.translate(half, half);
            gc.rotate(direction * 90);
            gc.translate(-half, -half);
            gc.drawImage(img, 0, 0, size, size);
        } else {
            gc.setFill(getEdgeColor(pattern));
            gc.fill();
        }

        gc.restore();
    }

    private static Color getEdgeColor(int pattern) {
        if (pattern >= 0 && pattern < EDGE_COLORS.length) {
            return EDGE_COLORS[pattern];
        }
        return Color.WHITE;
    }
}
