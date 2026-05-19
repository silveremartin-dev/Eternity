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
    private Spinner<Integer> patternSpinner;
    private ListView<Long> pieceListView;
    private List<Long> pieceLibrary = new ArrayList<>();
    private double zoomFactor = 1.0;
    private GridPane motifsGrid;

    public PuzzleDesigner() {
        setTitle("Eternity II - Puzzle Designer");
        try {
            getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/editor_icon.png")));
        } catch (Exception e) {
            // Ignore
        }

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f4f4f4;");

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5));
        toolbar.setStyle("-fx-background-color: #e8e8e8; -fx-border-color: #cccccc; -fx-border-width: 0 0 1px 0;");

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

        this.patternSpinner = new Spinner<>(2, 256, 22);
        patternSpinner.setPrefWidth(80);
        patternSpinner.valueProperty().addListener((obs, oldVal, newVal) -> refreshMotifsDisplay());

        Button generateBtn = new Button("Generate Random");
        generateBtn.setOnAction(e -> generateRandomPuzzle());

        toolbar.getChildren().addAll(new Label("Size:"), sizeCombo, new Label("Patterns:"), patternSpinner, clearBtn, generateBtn, loadBtn, saveBtn);
        root.setTop(toolbar);

        // Main Content Area with SplitPane
        SplitPane splitPane = new SplitPane();
        
        // Canvas (Left)
        boardCanvas = new Canvas(sizeX * cellSize, sizeY * cellSize);
        boardCanvas.setOnMouseClicked(e -> handleMouseClick(e.getX(), e.getY()));
        
        ScrollPane boardScroll = new ScrollPane(boardCanvas);
        boardScroll.setStyle("-fx-background: #ffffff;");
        
        // Piece Library & Details (Right)
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setMinWidth(300);
        
        Label paletteTitle = new Label("Piece Library");
        paletteTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        pieceListView = new ListView<>();
        pieceListView.setPrefHeight(400);
        setupPieceListView();
        setupDragAndDrop();

        Button addPieceBtn = new Button("Add");
        addPieceBtn.setOnAction(e -> showPieceDialog(null));
        
        Button editPieceBtn = new Button("Edit");
        editPieceBtn.setOnAction(e -> {
            Long selected = pieceListView.getSelectionModel().getSelectedItem();
            if (selected != null) showPieceDialog(selected);
        });

        Button removePieceBtn = new Button("Remove");
        removePieceBtn.setOnAction(e -> {
            Long selected = pieceListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                pieceLibrary.remove(selected);
                updatePieceList();
            }
        });

        HBox pieceOps = new HBox(5, addPieceBtn, editPieceBtn, removePieceBtn);
        pieceOps.setPadding(new Insets(5, 0, 5, 0));

        // Piece Detail Panel (New)
        VBox detailPanel = new VBox(5);
        detailPanel.setStyle("-fx-border-color: #cccccc; -fx-padding: 10; -fx-background-color: #ffffff;");
        Label detailTitle = new Label("Selected Piece Details:");
        detailTitle.setStyle("-fx-font-weight: bold;");
        HBox detailVisual = new HBox(10);
        detailVisual.setMinHeight(80);
        detailPanel.getChildren().addAll(detailTitle, detailVisual);
        
        pieceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateDetailPanel(detailVisual, newVal));

        // Available Motifs Panel (Dynamic)
        VBox motifsContainer = new VBox(5);
        Label motifsTitle = new Label("Available Motifs (Patterns):");
        motifsTitle.setStyle("-fx-font-weight: bold;");
        ScrollPane motifsScroll = new ScrollPane();
        motifsScroll.setPrefHeight(200);
        GridPane motifsGrid = new GridPane();
        motifsGrid.setHgap(3);
        motifsGrid.setVgap(3);
        motifsScroll.setContent(motifsGrid);
        motifsContainer.getChildren().addAll(motifsTitle, motifsScroll);

        rightPanel.getChildren().addAll(paletteTitle, pieceListView, pieceOps, detailPanel, motifsContainer);
        
        splitPane.getItems().addAll(boardScroll, rightPanel);
        splitPane.setDividerPositions(0.7); // 70% for the grid

        root.setCenter(splitPane);

        Scene scene = new Scene(root, 1300, 900);
        setScene(scene);
        
        this.motifsGrid = motifsGrid; // Store reference
        refreshMotifsDisplay();
        drawGrid();
    }

    private void refreshMotifsDisplay() {
        if (motifsGrid == null) return;
        motifsGrid.getChildren().clear();
        int count = patternSpinner.getValue();
        for (int i = 0; i < count; i++) {
            Canvas m = new Canvas(25, 25);
            GraphicsContext gc = m.getGraphicsContext2D();
            org.game.eternity2.util.BoardRenderer.drawTriangle(gc, i, 0, 25);
            int row = i / 8;
            int col = i % 8;
            motifsGrid.add(m, col, row);
            Tooltip.install(m, new Tooltip("ID: " + i));
        }
    }

    private void updateDetailPanel(HBox container, Long piece) {
        container.getChildren().clear();
        if (piece == null) return;
        int[] sides = {
            PiecePrimitive.getTop(piece),
            PiecePrimitive.getRight(piece),
            PiecePrimitive.getBottom(piece),
            PiecePrimitive.getLeft(piece)
        };
        for (int side : sides) {
            VBox box = new VBox(2);
            Canvas c = new Canvas(40, 40);
            org.game.eternity2.util.BoardRenderer.drawTriangle(c.getGraphicsContext2D(), side, 0, 40);
            box.getChildren().addAll(c, new Label("ID: " + side));
            container.getChildren().add(box);
        }
    }

    private void setupDragAndDrop() {
        pieceListView.setOnDragDetected(event -> {
            Long selected = pieceListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                javafx.scene.input.Dragboard db = pieceListView.startDragAndDrop(javafx.scene.input.TransferMode.COPY);
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(selected.toString());
                db.setContent(content);
                event.consume();
            }
        });

        boardCanvas.setOnDragOver(event -> {
            if (event.getGestureSource() != boardCanvas && event.getDragboard().hasString()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });

        boardCanvas.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                try {
                    long piece = Long.parseLong(db.getString());
                    int col = (int) (event.getX() / (cellSize * zoomFactor));
                    int row = (int) (event.getY() / (cellSize * zoomFactor));
                    if (col >= 0 && col < sizeX && row >= 0 && row < sizeY) {
                        hints.removeIf(h -> h.row() == row && h.col() == col);
                        hints.add(new Hint(row, col, PiecePrimitive.getId(piece), PiecePrimitive.getRotation(piece)));
                        drawGrid();
                        success = true;
                    }
                } catch (Exception e) {}
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void setupPieceListView() {
        pieceListView.setCellFactory(lv -> new ListCell<Long>() {
            @Override
            protected void updateItem(Long piece, boolean empty) {
                super.updateItem(piece, empty);
                if (empty || piece == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("ID: %3d (%d,%d,%d,%d)", 
                        PiecePrimitive.getId(piece),
                        PiecePrimitive.getTop(piece), PiecePrimitive.getRight(piece),
                        PiecePrimitive.getBottom(piece), PiecePrimitive.getLeft(piece)));
                    
                    // Small preview icon
                    Canvas preview = new Canvas(20, 20);
                    org.game.eternity2.util.BoardRenderer.drawTriangle(preview.getGraphicsContext2D(), PiecePrimitive.getTop(piece), 0, 20);
                    setGraphic(preview);
                }
            }
        });
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
            double x = hint.col() * cellSize;
            double y = hint.row() * cellSize;
            
            // Find piece in library to draw it properly
            long piece = 0;
            for (long p : pieceLibrary) {
                if (org.game.eternity2.model.PiecePrimitive.getId(p) == hint.tileId()) {
                    piece = p;
                    for (int i = 0; i < hint.rotation(); i++) piece = org.game.eternity2.model.PiecePrimitive.rotateCW(piece);
                    break;
                }
            }

            if (piece != 0) {
                // Draw actual motifs if piece is found
                org.game.eternity2.util.BoardRenderer.drawPiece(gc, piece, (int)x, (int)y, (int)cellSize);
            } else {
                // Fallback for unknown pieces
                gc.setFill(Color.LIGHTBLUE);
                gc.fillRect(x, y, cellSize, cellSize);
                gc.setFill(Color.BLACK);
                gc.fillText(String.valueOf(hint.tileId()), x + 5, y + 15);
            }
        }

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        for (int x = 0; x <= sizeX; x++) gc.strokeLine(x * cellSize, 0, x * cellSize, sizeY * cellSize);
        for (int y = 0; y <= sizeY; y++) gc.strokeLine(0, y * cellSize, sizeX * cellSize, y * cellSize);
    }

    private void saveDesign() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Unified Design");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Unified Puzzle", "*.json"));
        File file = fileChooser.showSaveDialog(this);

        if (file != null) {
            try {
                org.game.eternity2.model.UnifiedPuzzle up = new org.game.eternity2.model.UnifiedPuzzle();
                up.width = sizeX;
                up.height = sizeY;
                up.pieces = new ArrayList<>();
                for (long p : pieceLibrary) {
                    up.pieces.add(new org.game.eternity2.model.UnifiedPuzzle.PieceData(
                        org.game.eternity2.model.PiecePrimitive.getId(p),
                        org.game.eternity2.model.PiecePrimitive.getTop(p),
                        org.game.eternity2.model.PiecePrimitive.getRight(p),
                        org.game.eternity2.model.PiecePrimitive.getBottom(p),
                        org.game.eternity2.model.PiecePrimitive.getLeft(p)
                    ));
                }
                up.hints = new ArrayList<>();
                for (Hint h : hints) {
                    up.hints.add(new org.game.eternity2.model.UnifiedPuzzle.HintData(h.col(), h.row(), h.tileId(), h.rotation()));
                }
                org.game.eternity2.io.PuzzleLoaderWriter.saveUnified(file.toPath(), up);
                new Alert(Alert.AlertType.INFORMATION, "Design saved successfully!").show();
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to save design: " + ex.getMessage()).show();
            }
        }
    }

    private void loadDesign() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Load Unified Design");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Unified Puzzle", "*.json"));
        File file = fileChooser.showOpenDialog(this);

        if (file != null) {
            try {
                org.game.eternity2.model.UnifiedPuzzle up = org.game.eternity2.io.PuzzleLoaderWriter.loadUnified(file.toPath());
                sizeX = up.width;
                sizeY = up.height;
                hints.clear();
                pieceLibrary.clear();
                
                for (org.game.eternity2.model.UnifiedPuzzle.PieceData pd : up.pieces) {
                    pieceLibrary.add(org.game.eternity2.model.PiecePrimitive.create(pd.id, pd.top, pd.right, pd.bottom, pd.left));
                }
                
                for (org.game.eternity2.model.UnifiedPuzzle.HintData hd : up.hints) {
                    hints.add(new Hint(hd.y, hd.x, hd.pieceId, hd.rotation));
                }
                
                updatePieceList();
                updateSize(sizeX + "x" + sizeY);
                drawGrid();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to load design: " + ex.getMessage()).show();
            }
        }
    }
}
