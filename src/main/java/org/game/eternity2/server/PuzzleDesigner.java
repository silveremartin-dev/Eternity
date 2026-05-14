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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.game.eternity2.io.PuzzleLoaderWriter;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.Hint;
import org.game.eternity2.model.PiecePrimitive;

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
        try {
            getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/editor_icon.png")));
        } catch (Exception e) {
            // Ignore
        }

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

        Spinner<Integer> patternSpinner = new Spinner<>(2, 22, 22);
        patternSpinner.setPrefWidth(60);
        this.patternSpinner = patternSpinner;

        Button generateBtn = new Button("Generate Random");
        generateBtn.setOnAction(e -> generateRandomPuzzle());

        Button hintBtn = new Button("Add Hint (ID)");
        hintBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("1,0,0,0");
            dialog.setTitle("Add Hint by Piece ID");
            dialog.setHeaderText("Enter Piece ID, Rotation, Row, Col");
            dialog.setContentText("Format: ID,Rot,Row,Col (e.g. 1,0,5,5):");
            dialog.showAndWait().ifPresent(result -> {
                try {
                    String[] parts = result.split(",");
                    if (parts.length == 4) {
                        int id = Integer.parseInt(parts[0].trim());
                        int rot = Integer.parseInt(parts[1].trim());
                        int row = Integer.parseInt(parts[2].trim());
                        int col = Integer.parseInt(parts[3].trim());
                        hints.add(new Hint(row, col, id, rot));
                        drawGrid();
                    }
                } catch (Exception ex) {}
            });
        });

        toolbar.getChildren().addAll(new Label("Size:"), sizeCombo, new Label("Patterns:"), patternSpinner, clearBtn, generateBtn, hintBtn, loadBtn, saveBtn);
        root.setTop(toolbar);

        // Canvas
        boardCanvas = new Canvas(sizeX * cellSize, sizeY * cellSize);
        boardCanvas.setOnMouseClicked(e -> handleMouseClick(e.getX(), e.getY()));
        ScrollPane scrollPane = new ScrollPane(boardCanvas);
        root.setCenter(scrollPane);

        // Piece Library / Palette
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(250);
        rightPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");

        Label paletteTitle = new Label("Piece Library");
        paletteTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        pieceListView = new ListView<>();
        pieceListView.setPrefHeight(300);
        pieceListView.setCellFactory(lv -> new ListCell<Long>() {
            @Override
            protected void updateItem(Long piece, boolean empty) {
                super.updateItem(piece, empty);
                if (empty || piece == null) {
                    setText(null);
                } else {
                    setText(String.format("ID: %d (%d/%d/%d/%d)", 
                        PiecePrimitive.getId(piece),
                        PiecePrimitive.getTop(piece), PiecePrimitive.getRight(piece),
                        PiecePrimitive.getBottom(piece), PiecePrimitive.getLeft(piece)));
                }
            }
        });

        Button addPieceBtn = new Button("Add Piece");
        addPieceBtn.setOnAction(e -> showPieceDialog(null));
        
        Button editPieceBtn = new Button("Edit Piece");
        editPieceBtn.setOnAction(e -> {
            Long selected = pieceListView.getSelectionModel().getSelectedItem();
            if (selected != null) showPieceDialog(selected);
        });

        Button removePieceBtn = new Button("Remove Piece");
        removePieceBtn.setOnAction(e -> {
            Long selected = pieceListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                pieceLibrary.remove(selected);
                updatePieceList();
            }
        });

        HBox pieceOps = new HBox(5, addPieceBtn, editPieceBtn, removePieceBtn);

        VBox motifsPanel = new VBox(5);
        addMotifsDisplay(motifsPanel);

        rightPanel.getChildren().addAll(paletteTitle, pieceListView, pieceOps, new Label("Click grid to place piece"), motifsPanel);
        root.setRight(rightPanel);

        Scene scene = new Scene(root, 1200, 850);
        setScene(scene);

        // Add Zoom support
        boardCanvas.setOnScroll(e -> {
            if (e.isControlDown()) {
                double delta = e.getDeltaY();
                if (delta > 0) zoomFactor *= 1.1;
                else zoomFactor /= 1.1;
                boardCanvas.setScaleX(zoomFactor);
                boardCanvas.setScaleY(zoomFactor);
                e.consume();
            }
        });

        drawGrid();
    }

    private void addMotifsDisplay(VBox parent) {
        Label motifsTitle = new Label("Available Motifs (Patterns):");
        motifsTitle.setStyle("-fx-font-weight: bold;");
        
        GridPane motifsGrid = new GridPane();
        motifsGrid.setHgap(5);
        motifsGrid.setVgap(5);
        motifsGrid.setPadding(new Insets(5));
        
        for (int i = 0; i < 23; i++) {
            javafx.scene.canvas.Canvas motifCanvas = new javafx.scene.canvas.Canvas(30, 30);
            javafx.scene.canvas.GraphicsContext gc = motifCanvas.getGraphicsContext2D();
            org.game.eternity2.util.BoardRenderer.drawTriangle(gc, i, 0, 30); 
            
            int row = i / 6;
            int col = i % 6;
            motifsGrid.add(motifCanvas, col, row);
            Tooltip.install(motifCanvas, new Tooltip("Pattern ID: " + i + " (" + org.game.eternity2.util.BoardRenderer.PATTERN_NAMES[i] + ")"));
        }
        parent.getChildren().addAll(motifsTitle, motifsGrid);
    }

    private void handleMouseClick(double x, double y) {
        int col = (int) (x / (cellSize * zoomFactor));
        int row = (int) (y / (cellSize * zoomFactor));

        if (col >= 0 && col < sizeX && row >= 0 && row < sizeY) {
            // Check if hint already exists
            hints.removeIf(h -> h.row() == row && h.col() == col);

            Long selected = pieceListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                hints.add(new Hint(row, col, org.game.eternity2.model.PiecePrimitive.getId(selected), org.game.eternity2.model.PiecePrimitive.getRotation(selected)));
                drawGrid();
                return;
            }

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
        
        // Adjust window size
        this.setWidth(Math.max(1200, sizeX * cellSize + 400));
        this.setHeight(Math.max(850, sizeY * cellSize + 200));
        
        drawGrid();
    }

    private void updatePieceList() {
        pieceListView.getItems().setAll(pieceLibrary);
    }

    private void showPieceDialog(Long existing) {
        Dialog<Long> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Piece" : "Edit Piece");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idField = new TextField(existing == null ? "0" : String.valueOf(org.game.eternity2.model.PiecePrimitive.getId(existing)));
        TextField topField = new TextField(existing == null ? "1" : String.valueOf(org.game.eternity2.model.PiecePrimitive.getTop(existing)));
        TextField rightField = new TextField(existing == null ? "1" : String.valueOf(org.game.eternity2.model.PiecePrimitive.getRight(existing)));
        TextField bottomField = new TextField(existing == null ? "1" : String.valueOf(org.game.eternity2.model.PiecePrimitive.getBottom(existing)));
        TextField leftField = new TextField(existing == null ? "1" : String.valueOf(org.game.eternity2.model.PiecePrimitive.getLeft(existing)));

        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Top Pattern:"), 0, 1);
        grid.add(topField, 1, 1);
        grid.add(new Label("Right Pattern:"), 0, 2);
        grid.add(rightField, 1, 2);
        grid.add(new Label("Bottom Pattern:"), 0, 3);
        grid.add(bottomField, 1, 3);
        grid.add(new Label("Left Pattern:"), 0, 4);
        grid.add(leftField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return org.game.eternity2.model.PiecePrimitive.create(
                        Integer.parseInt(idField.getText()),
                        Integer.parseInt(topField.getText()),
                        Integer.parseInt(rightField.getText()),
                        Integer.parseInt(bottomField.getText()),
                        Integer.parseInt(leftField.getText())
                    );
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(piece -> {
            if (existing != null) pieceLibrary.remove(existing);
            pieceLibrary.add(piece);
            updatePieceList();
        });
    }

    private void generateRandomPuzzle() {
        int width = sizeX;
        int height = sizeY;
        
        int[][] hEdges = new int[height][width - 1];
        int[][] vEdges = new int[height - 1][width];
        
        java.util.Random rnd = new java.util.Random();
        int maxPattern = patternSpinner.getValue();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width - 1; x++) {
                hEdges[y][x] = rnd.nextInt(maxPattern) + 1;
            }
        }
        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width; x++) {
                vEdges[y][x] = rnd.nextInt(maxPattern) + 1;
            }
        }

        pieceLibrary.clear();
        hints.clear();
        
        List<Long> solvedPieces = new ArrayList<>();
        int idCounter = 1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int top = (y == 0) ? 0 : vEdges[y - 1][x];
                int bottom = (y == height - 1) ? 0 : vEdges[y][x];
                int left = (x == 0) ? 0 : hEdges[y][x - 1];
                int right = (x == width - 1) ? 0 : hEdges[y][x];
                
                long piece = org.game.eternity2.model.PiecePrimitive.create(idCounter++, top, right, bottom, left);
                solvedPieces.add(piece);
            }
        }

        List<Long> shuffled = new ArrayList<>(solvedPieces);
        java.util.Collections.shuffle(shuffled);
        
        for (Long p : shuffled) {
            long rotated = p;
            int rotations = rnd.nextInt(4);
            for (int i = 0; i < rotations; i++) {
                rotated = org.game.eternity2.model.PiecePrimitive.rotateCW(rotated);
            }
            pieceLibrary.add(rotated);
        }
        
        int hintCount = 0;
        updatePieceList();
        drawGrid();
    }

    private void drawGrid() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());

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
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Puzzle Files", "*.json"));
        File file = fileChooser.showSaveDialog(this);

        if (file != null) {
            try {
                org.game.eternity2.model.BoardPrimitive board = new org.game.eternity2.model.BoardPrimitive(sizeX, sizeY);
                for (Hint h : hints) {
                    long piece = org.game.eternity2.model.PiecePrimitive.create(h.tileId(), 1, 1, 1, 1);
                    for (int i = 0; i < h.rotation(); i++) {
                        piece = org.game.eternity2.model.PiecePrimitive.rotateCW(piece);
                    }
                    board.placePiece(h.col(), h.row(), piece);
                }
                org.game.eternity2.io.PuzzleLoaderWriter.saveSolution(file.toPath(), board);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.showAndWait();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.showAndWait();
            }
        }
    }

    private void loadDesign() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Load Puzzle Design");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Puzzle Files", "*.json"));
        File file = fileChooser.showOpenDialog(this);

        if (file != null) {
            try {
                org.game.eternity2.model.BoardPrimitive board = org.game.eternity2.io.PuzzleLoaderWriter.loadSolution(file.toPath(), null);
                sizeX = board.getWidth();
                sizeY = board.getHeight();
                hints.clear();
                pieceLibrary.clear();
                for (int y = 0; y < sizeY; y++) {
                    for (int x = 0; x < sizeX; x++) {
                        long piece = board.getPiece(x, y);
                        if (piece != 0) {
                            hints.add(new Hint(y, x, org.game.eternity2.model.PiecePrimitive.getId(piece), org.game.eternity2.model.PiecePrimitive.getRotation(piece)));
                            if (!pieceLibrary.contains(piece)) pieceLibrary.add(piece);
                        }
                    }
                }
                updatePieceList();
                updateSize(sizeX + "x" + sizeY);
                drawGrid();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.showAndWait();
            }
        }
    }
}
