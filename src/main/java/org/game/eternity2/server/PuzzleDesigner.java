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
package org.game.eternity2.server;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.game.eternity2.model.Hint;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * A simple UI for designing Eternity II puzzles.
 * Allows visualizing and potentially editing board configurations.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class PuzzleDesigner extends Stage {

    private List<Hint> hints = new ArrayList<>();
    private Canvas boardCanvas;
    private int sizeX = 16;
    private int sizeY = 16;
    private double cellSize = 30;

    public PuzzleDesigner() {
        setTitle("Eternity II - Puzzle Designer");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(0, 0, 10, 0));

        ComboBox<String> sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll("4x4", "6x6", "12x6", "16x16");
        sizeCombo.setValue("16x16");
        sizeCombo.setOnAction(e -> updateSize(sizeCombo.getValue()));

        Button clearBtn = new Button("Clear Board");
        clearBtn.setOnAction(e -> {
            hints.clear();
            drawGrid();
        });

        Button loadBtn = new Button("Load Design");
        loadBtn.setOnAction(e -> loadDesign());

        Button saveBtn = new Button("Save Design");
        saveBtn.setOnAction(e -> saveDesign());

        toolbar.getChildren().addAll(new Label("Size:"), sizeCombo, clearBtn, loadBtn, saveBtn);
        root.setTop(toolbar);

        // Canvas
        boardCanvas = new Canvas(sizeX * cellSize, sizeY * cellSize);
        boardCanvas.setOnMouseClicked(e -> handleMouseClick(e.getX(), e.getY()));
        ScrollPane scrollPane = new ScrollPane(boardCanvas);
        root.setCenter(scrollPane);

        // Palette (Placeholder)
        VBox palette = new VBox(10);
        palette.setPadding(new Insets(0, 0, 0, 10));
        palette.getChildren().add(new Label("Palette"));
        palette.getChildren().add(new Label("Click on grid to add hint"));
        root.setRight(palette);

        Scene scene = new Scene(root, 800, 600);
        setScene(scene);

        drawGrid();
    }

    private void handleMouseClick(double x, double y) {
        int col = (int) (x / cellSize);
        int row = (int) (y / cellSize);

        if (col >= 0 && col < sizeX && row >= 0 && row < sizeY) {
            // Check if hint already exists
            hints.removeIf(h -> h.row() == row && h.col() == col);

            // Ask for tile ID and rotation
            TextInputDialog dialog = new TextInputDialog("0,0");
            dialog.setTitle("Add Hint");
            dialog.setHeaderText("Enter Tile ID and Rotation (comma separated)");
            dialog.setContentText("Format: ID,Rotation (e.g. 42,1):");

            dialog.showAndWait().ifPresent(result -> {
                try {
                    String[] parts = result.split(",");
                    if (parts.length == 2) {
                        int id = Integer.parseInt(parts[0].trim());
                        int rot = Integer.parseInt(parts[1].trim());
                        hints.add(new Hint(row, col, id, rot));
                        drawGrid();
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            });
            drawGrid();
        }
    }

    private void updateSize(String sizeStr) {
        if (sizeStr.startsWith("4x4")) {
            sizeX = 4;
            sizeY = 4;
        } else if (sizeStr.startsWith("6x6")) {
            sizeX = 6;
            sizeY = 6;
        } else if (sizeStr.startsWith("12x6")) {
            sizeX = 12;
            sizeY = 6;
        } else if (sizeStr.startsWith("16x16")) {
            sizeX = 16;
            sizeY = 16;
        }

        hints.clear();
        boardCanvas.setWidth(sizeX * cellSize);
        boardCanvas.setHeight(sizeY * cellSize);
        drawGrid();
    }

    private void drawGrid() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());

        // Draw hints
        for (Hint hint : hints) {
            gc.setFill(Color.LIGHTBLUE);
            gc.fillRect(hint.col() * cellSize, hint.row() * cellSize, cellSize, cellSize);
            gc.setFill(Color.BLACK);
            gc.fillText(hint.tileId() + "", hint.col() * cellSize + 5, hint.row() * cellSize + 15);
            gc.fillText("r" + hint.rotation(), hint.col() * cellSize + 5, hint.row() * cellSize + 25);
        }

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);

        for (int x = 0; x <= sizeX; x++) {
            gc.strokeLine(x * cellSize, 0, x * cellSize, sizeY * cellSize);
        }
        for (int y = 0; y <= sizeY; y++) {
            gc.strokeLine(0, y * cellSize, sizeX * cellSize, y * cellSize);
        }
    }

    private void saveDesign() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Puzzle Design");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Puzzle Files", "*.puzzle"));
        File file = fileChooser.showSaveDialog(this);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("SIZE=" + sizeX);
                writer.println("DIM=" + sizeX + "x" + sizeY);
                writer.println("HINTS=" + hints.size());
                for (int i = 0; i < hints.size(); i++) {
                    Hint h = hints.get(i);
                    writer.println("HINT_" + i + "=" + h.row() + "," + h.col() + "," + h.tileId() + ","
                            + h.rotation());
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Puzzle saved to " + file.getName());
                alert.showAndWait();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not save file");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void loadDesign() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Load Puzzle Design");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Puzzle Files", "*.puzzle"));
        File file = fileChooser.showOpenDialog(this);

        if (file != null) {
            try (Scanner scanner = new Scanner(file)) {
                hints.clear();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("DIM=")) {
                        String[] parts = line.substring(4).split("x");
                        sizeX = Integer.parseInt(parts[0]);
                        sizeY = Integer.parseInt(parts[1]);
                        boardCanvas.setWidth(sizeX * cellSize);
                        boardCanvas.setHeight(sizeY * cellSize);
                    } else if (line.startsWith("HINT_")) {
                        String[] parts = line.split("=")[1].split(",");
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);
                        int id = Integer.parseInt(parts[2]);
                        int rot = Integer.parseInt(parts[3]);
                        hints.add(new Hint(row, col, id, rot));
                    }
                }
                drawGrid();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not load file");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        }
    }
}
