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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A highly interactive, beautiful, and premium editor for designing Eternity II puzzles.
 * Completely customized according to detailed user experience feedback.
 * 
 * @author Silvere Martin-Michiellot
 * @author Antigravity
 * @since 1.0
 */
public class PuzzleDesigner extends Stage {

    public static class PlacedPiece {
        public int row;
        public int col;
        public int pieceId;
        public int rotation; // CW rotation applied to the library primitive
        public boolean isHint;

        public PlacedPiece(int row, int col, int pieceId, int rotation, boolean isHint) {
            this.row = row;
            this.col = col;
            this.pieceId = pieceId;
            this.rotation = rotation;
            this.isHint = isHint;
        }
    }

    private List<PlacedPiece> placements = new ArrayList<>();
    private List<List<PlacedPiece>> undoStack = new ArrayList<>();
    
    private Canvas boardCanvas;
    private int sizeX = 16;
    private int sizeY = 16;
    private double cellSize = 40;
    private double zoomFactor = 1.0;
    
    private int selectedRow = -1;
    private int selectedCol = -1;

    private Spinner<Integer> patternSpinner;
    private TableView<Long> pieceTableView;
    private List<Long> pieceLibrary = new ArrayList<>();
    private GridPane motifsGrid;
    private ComboBox<String> sizeCombo;
    private ComboBox<String> rightSizeCombo;
    private boolean isUpdatingSize = false;

    public PuzzleDesigner() {
        setTitle("Eternity II - Puzzle Designer & Editor");
        try {
            getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/editor_icon.png")));
        } catch (Exception e) {
            // Ignore
        }

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Top Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(8));
        toolbar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-radius: 4; -fx-alignment: center-left;");

        ComboBox<String> knownPuzzlesCombo = new ComboBox<>();
        knownPuzzlesCombo.setPromptText("Load Puzzle...");
        knownPuzzlesCombo.setPrefWidth(220);
        
        java.io.File puzzlesDir = new java.io.File("src/main/resources/puzzles/");
        if (puzzlesDir.exists()) {
            java.io.File[] files = puzzlesDir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (java.io.File f : files) {
                    String name = f.getName().replace(".json", "");
                    if (!knownPuzzlesCombo.getItems().contains(name)) {
                        knownPuzzlesCombo.getItems().add(name);
                    }
                }
            }
        }
        knownPuzzlesCombo.getItems().add("Load Custom File...");
        
        knownPuzzlesCombo.setOnAction(e -> {
            String selected = knownPuzzlesCombo.getValue();
            if (selected == null) return;
            if ("Load Custom File...".equals(selected)) {
                loadDesign();
            } else {
                loadDesignFromResource(selected);
            }
            javafx.application.Platform.runLater(() -> knownPuzzlesCombo.setValue(null));
        });

        Button clearBtn = new Button("Clear Board");
        clearBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setOnAction(e -> {
            saveToUndoStack();
            placements.clear();
            selectedRow = -1;
            selectedCol = -1;
            updatePieceList();
            drawGrid();
            pieceTableView.refresh();
        });

        Button undoBtn = new Button("Undo");
        undoBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        undoBtn.setOnAction(e -> performUndo());

        Button generateBtn = new Button("Generate Random");
        generateBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        generateBtn.setOnAction(e -> generateRandomPuzzle());

        Button saveBtn = new Button("Save Design");
        saveBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> saveDesign());

        toolbar.getChildren().addAll(
            knownPuzzlesCombo,
            new Label("|"), clearBtn, undoBtn, generateBtn,
            new Label("|"), saveBtn
        );
        root.setTop(toolbar);
        BorderPane.setMargin(toolbar, new Insets(0, 0, 10, 0));

        // Canvas (Left)
        boardCanvas = new Canvas(sizeX * cellSize, sizeY * cellSize);
        boardCanvas.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                int col = (int) (e.getX() / (cellSize * zoomFactor));
                int row = (int) (e.getY() / (cellSize * zoomFactor));
                removePlacementAt(row, col);
            } else {
                handleMouseClick(e.getX(), e.getY());
            }
        });
        
        ScrollPane boardScroll = new ScrollPane(boardCanvas);
        boardScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 4;");
        boardScroll.setPadding(new Insets(10));
        boardScroll.setOnMouseClicked(e -> {
            if (e.getTarget() == boardScroll || e.getTarget() == boardScroll.getContent()) {
                selectedRow = -1;
                selectedCol = -1;
                pieceTableView.getSelectionModel().clearSelection();
                updatePieceList();
                drawGrid();
                pieceTableView.refresh();
            }
        });
        
        // Right Panel - Standardized Reorganization
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setMinWidth(380);
        rightPanel.setMaxWidth(420);
        rightPanel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-radius: 4;");

        // 1. Size Selection Panel
        VBox sizePanel = new VBox(5);
        Label sizeTitle = new Label("1. Puzzle Board Size");
        sizeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333333;");
        rightSizeCombo = new ComboBox<>();
        rightSizeCombo.getItems().addAll("4x4", "6x6", "12x6", "16x16");
        rightSizeCombo.setValue("16x16");
        rightSizeCombo.setPrefWidth(200);
        rightSizeCombo.setOnAction(e -> {
            if (!isUpdatingSize && rightSizeCombo.getValue() != null) {
                updateSize(rightSizeCombo.getValue());
            }
        });
        sizePanel.getChildren().addAll(sizeTitle, rightSizeCombo);

        // 2. Pattern Count Selection Panel
        VBox patternPanel = new VBox(5);
        Label patternTitle = new Label("2. Motif / Pattern Count");
        patternTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333333;");
        this.patternSpinner = new Spinner<>(2, 256, 22);
        patternSpinner.setPrefWidth(200);
        patternSpinner.valueProperty().addListener((obs, oldVal, newVal) -> refreshMotifsDisplay());
        patternPanel.getChildren().addAll(patternTitle, patternSpinner);

        // 3. Available Motifs Display
        VBox motifsContainer = new VBox(5);
        Label motifsTitle = new Label("3. Available Colors & Patterns");
        motifsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333333;");
        ScrollPane motifsScroll = new ScrollPane();
        motifsScroll.setPrefHeight(120);
        motifsScroll.setStyle("-fx-background: #fafafa; -fx-border-color: #e0e0e0;");
        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setPadding(new Insets(5));
        motifsScroll.setContent(grid);
        this.motifsGrid = grid;
        motifsContainer.getChildren().addAll(motifsTitle, motifsScroll);

        // 4. Piece Library TableView (Fully standard layout with preview)
        VBox libraryContainer = new VBox(5);
        Label libraryTitle = new Label("4. Piece Library");
        libraryTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333333;");

        pieceTableView = new TableView<>();
        pieceTableView.setPrefHeight(350);
        setupPieceTableView();
        setupDragAndDrop();

        Button addPieceBtn = new Button("Add");
        addPieceBtn.setOnAction(e -> showPieceDialog(null));
        
        Button editPieceBtn = new Button("Edit");
        editPieceBtn.setOnAction(e -> {
            Long selected = pieceTableView.getSelectionModel().getSelectedItem();
            if (selected != null) showPieceDialog(selected);
        });

        Button removePieceBtn = new Button("Remove");
        removePieceBtn.setOnAction(e -> {
            Long selected = pieceTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                    "Are you sure you want to delete this piece from the library? This will also remove any placements of this piece on the board.", 
                    ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Confirm Deletion");
                confirm.setHeaderText(null);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        saveToUndoStack();
                        int id = PiecePrimitive.getId(selected);
                        placements.removeIf(p -> p.pieceId == id);
                        pieceLibrary.remove(selected);
                        updatePieceList();
                        drawGrid();
                    }
                });
            }
        });

        HBox pieceOps = new HBox(8, addPieceBtn, editPieceBtn, removePieceBtn);
        pieceOps.setPadding(new Insets(5, 0, 5, 0));

        libraryContainer.getChildren().addAll(libraryTitle, pieceTableView, pieceOps);

        // Assembling the Right Panel items in order
        rightPanel.getChildren().addAll(
            sizePanel,
            new Separator(),
            patternPanel,
            new Separator(),
            motifsContainer,
            new Separator(),
            libraryContainer
        );

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(boardScroll, rightPanel);
        splitPane.setDividerPositions(0.7); // 70% for the board scroll grid

        root.setCenter(splitPane);

        Scene scene = new Scene(root, 1100, 780);
        setScene(scene);
        
        refreshMotifsDisplay();
        drawGrid();
    }

    private void saveToUndoStack() {
        List<PlacedPiece> copy = new ArrayList<>();
        for (PlacedPiece p : placements) {
            copy.add(new PlacedPiece(p.row, p.col, p.pieceId, p.rotation, p.isHint));
        }
        undoStack.add(copy);
        if (undoStack.size() > 50) {
            undoStack.remove(0);
        }
    }

    private void performUndo() {
        if (!undoStack.isEmpty()) {
            placements = undoStack.remove(undoStack.size() - 1);
            selectedRow = -1;
            selectedCol = -1;
            updatePieceList();
            drawGrid();
            pieceTableView.refresh();
        } else {
            new Alert(Alert.AlertType.INFORMATION, "Nothing to undo.").show();
        }
    }

    private PlacedPiece getPlacementForPiece(int id) {
        for (PlacedPiece p : placements) {
            if (p.pieceId == id) return p;
        }
        return null;
    }

    private PlacedPiece getPlacementAt(int row, int col) {
        for (PlacedPiece p : placements) {
            if (p.row == row && p.col == col) return p;
        }
        return null;
    }

    private long getPieceFromLibrary(int id) {
        for (long p : pieceLibrary) {
            if (PiecePrimitive.getId(p) == id) return p;
        }
        return 0;
    }

    private boolean isValidPlacement(int row, int col, long piece) {
        int top = PiecePrimitive.getTop(piece);
        int right = PiecePrimitive.getRight(piece);
        int bottom = PiecePrimitive.getBottom(piece);
        int left = PiecePrimitive.getLeft(piece);

        // Border checks:
        if (row == 0 && top != 0) return false;
        if (row > 0 && top == 0) return false; 
        
        if (row == sizeY - 1 && bottom != 0) return false;
        if (row < sizeY - 1 && bottom == 0) return false;

        if (col == 0 && left != 0) return false;
        if (col > 0 && left == 0) return false;

        if (col == sizeX - 1 && right != 0) return false;
        if (col < sizeX - 1 && right == 0) return false;

        // Neighbor checks:
        // Top neighbor
        if (row > 0) {
            PlacedPiece neighbor = getPlacementAt(row - 1, col);
            if (neighbor != null) {
                long neighborPiece = getPieceFromLibrary(neighbor.pieceId);
                if (neighborPiece != 0) {
                    for (int i = 0; i < neighbor.rotation; i++) neighborPiece = PiecePrimitive.rotateCW(neighborPiece);
                    int neighborBottom = PiecePrimitive.getBottom(neighborPiece);
                    if (neighborBottom != top) return false;
                }
            }
        }

        // Bottom neighbor
        if (row < sizeY - 1) {
            PlacedPiece neighbor = getPlacementAt(row + 1, col);
            if (neighbor != null) {
                long neighborPiece = getPieceFromLibrary(neighbor.pieceId);
                if (neighborPiece != 0) {
                    for (int i = 0; i < neighbor.rotation; i++) neighborPiece = PiecePrimitive.rotateCW(neighborPiece);
                    int neighborTop = PiecePrimitive.getTop(neighborPiece);
                    if (neighborTop != bottom) return false;
                }
            }
        }

        // Left neighbor
        if (col > 0) {
            PlacedPiece neighbor = getPlacementAt(row, col - 1);
            if (neighbor != null) {
                long neighborPiece = getPieceFromLibrary(neighbor.pieceId);
                if (neighborPiece != 0) {
                    for (int i = 0; i < neighbor.rotation; i++) neighborPiece = PiecePrimitive.rotateCW(neighborPiece);
                    int neighborRight = PiecePrimitive.getRight(neighborPiece);
                    if (neighborRight != left) return false;
                }
            }
        }

        // Right neighbor
        if (col < sizeX - 1) {
            PlacedPiece neighbor = getPlacementAt(row, col + 1);
            if (neighbor != null) {
                long neighborPiece = getPieceFromLibrary(neighbor.pieceId);
                if (neighborPiece != 0) {
                    for (int i = 0; i < neighbor.rotation; i++) neighborPiece = PiecePrimitive.rotateCW(neighborPiece);
                    int neighborLeft = PiecePrimitive.getLeft(neighborPiece);
                    if (neighborLeft != right) return false;
                }
            }
        }

        return true;
    }

    private void refreshMotifsDisplay() {
        if (motifsGrid == null) return;
        motifsGrid.getChildren().clear();
        int count = patternSpinner.getValue();
        for (int i = 0; i < count; i++) {
            Canvas m = new Canvas(25, 25);
            GraphicsContext gc = m.getGraphicsContext2D();
            
            // Draw a stylish round colored square representing available motifs
            gc.setFill(org.game.eternity2.util.BoardRenderer.getEdgePaint(i));
            gc.fillRoundRect(2, 2, 21, 21, 5, 5);
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(0.8);
            gc.strokeRoundRect(2, 2, 21, 21, 5, 5);
            
            int row = i / 10;
            int col = i % 10;
            motifsGrid.add(m, col, row);
            Tooltip.install(m, new Tooltip("Motif ID: " + i));
        }
    }

    private void setupPieceTableView() {
        TableColumn<Long, Long> previewCol = new TableColumn<>("Piece");
        previewCol.setPrefWidth(55);
        previewCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        previewCol.setCellFactory(col -> new TableCell<Long, Long>() {
            private final Canvas canvas = new Canvas(32, 32);
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, 32, 32);
                    org.game.eternity2.util.BoardRenderer.drawPiece(gc, item, 0, 0, 32);
                    setGraphic(canvas);
                }
            }
        });

        TableColumn<Long, Integer> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(35);
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(PiecePrimitive.getId(data.getValue())));

        TableColumn<Long, String> edgesCol = new TableColumn<>("Edges (T,R,B,L)");
        edgesCol.setPrefWidth(110);
        edgesCol.setCellValueFactory(data -> {
            long p = data.getValue();
            return new javafx.beans.property.SimpleStringProperty(String.format("(%d, %d, %d, %d)", 
                PiecePrimitive.getTop(p), PiecePrimitive.getRight(p), 
                PiecePrimitive.getBottom(p), PiecePrimitive.getLeft(p)));
        });

        TableColumn<Long, Boolean> hintCol = new TableColumn<>("Hint?");
        hintCol.setPrefWidth(50);
        hintCol.setCellValueFactory(data -> {
            int id = PiecePrimitive.getId(data.getValue());
            PlacedPiece placed = getPlacementForPiece(id);
            return new javafx.beans.property.SimpleBooleanProperty(placed != null && placed.isHint);
        });
        hintCol.setCellFactory(col -> new TableCell<Long, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(e -> {
                    Long piece = getTableView().getItems().get(getIndex());
                    if (piece != null) {
                        int id = PiecePrimitive.getId(piece);
                        PlacedPiece placed = getPlacementForPiece(id);
                        if (placed != null) {
                            saveToUndoStack();
                            placed.isHint = checkBox.isSelected();
                            drawGrid();
                            pieceTableView.refresh();
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Long piece = getTableView().getItems().get(getIndex());
                    if (piece != null) {
                        int id = PiecePrimitive.getId(piece);
                        PlacedPiece placed = getPlacementForPiece(id);
                        if (placed != null) {
                            checkBox.setDisable(false);
                            checkBox.setSelected(placed.isHint);
                        } else {
                            checkBox.setSelected(false);
                            checkBox.setDisable(true);
                        }
                        setGraphic(checkBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        TableColumn<Long, String> posCol = new TableColumn<>("Position");
        posCol.setPrefWidth(65);
        posCol.setCellValueFactory(data -> {
            int id = PiecePrimitive.getId(data.getValue());
            PlacedPiece placed = getPlacementForPiece(id);
            if (placed != null) {
                return new javafx.beans.property.SimpleStringProperty(String.format("(%d,%d)", placed.col, placed.row));
            } else {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
        });

        pieceTableView.getColumns().addAll(previewCol, idCol, edgesCol, hintCol, posCol);

        pieceTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Long selectedPiece = pieceTableView.getSelectionModel().getSelectedItem();
                if (selectedPiece != null && selectedRow >= 0 && selectedCol >= 0) {
                    // Check if already placed elsewhere
                    int id = PiecePrimitive.getId(selectedPiece);
                    if (getPlacementForPiece(id) != null) return;
                    
                    // Try placement under each of the 4 rotations
                    long pieceToPlace = selectedPiece;
                    boolean placedValid = false;
                    int bestRot = 0;
                    for (int rot = 0; rot < 4; rot++) {
                        if (isValidPlacement(selectedRow, selectedCol, pieceToPlace)) {
                            bestRot = rot;
                            placedValid = true;
                            break;
                        }
                        pieceToPlace = PiecePrimitive.rotateCW(pieceToPlace);
                    }
                    
                    if (placedValid) {
                        saveToUndoStack();
                        placements.removeIf(p -> p.row == selectedRow && p.col == selectedCol);
                        placements.add(new PlacedPiece(selectedRow, selectedCol, id, bestRot, false));
                        pieceTableView.getSelectionModel().clearSelection();
                        updatePieceList();
                        drawGrid();
                        pieceTableView.refresh();
                    }
                }
            }
        });

        // Standard Row Factory for Live Valid Placements Highlighting and Graying out Placed Pieces
        pieceTableView.setRowFactory(tv -> new TableRow<Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    setOpacity(1.0);
                } else {
                    int id = PiecePrimitive.getId(item);
                    PlacedPiece placed = getPlacementForPiece(id);
                    if (selectedRow >= 0 && selectedCol >= 0) {
                        // Cell is selected
                        if (placed != null && (placed.row != selectedRow || placed.col != selectedCol)) {
                            // Already placed elsewhere: opacity 0.4, no highlight
                            setStyle("");
                            setOpacity(0.4);
                        } else {
                            // Either not placed, or placed at the selected cell itself
                            boolean hasValidRot = false;
                            long p = item;
                            for (int rot = 0; rot < 4; rot++) {
                                if (isValidPlacement(selectedRow, selectedCol, p)) {
                                    hasValidRot = true;
                                    break;
                                }
                                p = PiecePrimitive.rotateCW(p);
                            }
                            if (hasValidRot) {
                                setStyle("-fx-background-color: #c8e6c9;"); // Premium light green background
                                setOpacity(1.0);
                            } else {
                                setStyle("");
                                setOpacity(0.4); // Does not satisfy constraints: opacity 0.4, no highlight
                            }
                        }
                    } else {
                        // No cell is selected
                        if (placed != null) {
                            setStyle("-fx-background-color: #f0f0f0;");
                            setOpacity(0.4); // Placed pieces: opacity 0.4
                        } else {
                            setStyle("");
                            setOpacity(1.0); // Default opacity 1.0
                        }
                    }
                }
            }
        });
    }

    private void setupDragAndDrop() {
        pieceTableView.setOnDragDetected(event -> {
            Long selected = pieceTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Prevent drag if already placed on the board
                int id = PiecePrimitive.getId(selected);
                if (getPlacementForPiece(id) != null) {
                    event.consume();
                    return;
                }
                
                javafx.scene.input.Dragboard db = pieceTableView.startDragAndDrop(javafx.scene.input.TransferMode.COPY);
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
                    int id = PiecePrimitive.getId(piece);
                    int col = (int) (event.getX() / (cellSize * zoomFactor));
                    int row = (int) (event.getY() / (cellSize * zoomFactor));
                    if (col >= 0 && col < sizeX && row >= 0 && row < sizeY) {
                        PlacedPiece existing = getPlacementForPiece(id);
                        if (existing != null && (existing.row != row || existing.col != col)) {
                            new Alert(Alert.AlertType.WARNING, "This piece is already placed on the board!").show();
                            event.setDropCompleted(false);
                            event.consume();
                            return;
                        }
                        
                        long pieceToPlace = piece;
                        boolean placedValid = false;
                        int bestRot = 0;
                        for (int rot = 0; rot < 4; rot++) {
                            if (isValidPlacement(row, col, pieceToPlace)) {
                                bestRot = rot;
                                placedValid = true;
                                break;
                            }
                            pieceToPlace = org.game.eternity2.model.PiecePrimitive.rotateCW(pieceToPlace);
                        }
                        
                        if (placedValid) {
                            saveToUndoStack();
                            placements.removeIf(p -> p.row == row && p.col == col);
                            placements.add(new PlacedPiece(row, col, id, bestRot, false));
                            selectedRow = row;
                            selectedCol = col;
                            updatePieceList();
                            drawGrid();
                            pieceTableView.refresh();
                            success = true;
                        } else {
                            new Alert(Alert.AlertType.ERROR, "No valid rotation for this piece fits at this cell!").show();
                        }
                    }
                } catch (Exception e) {}
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void handleMouseClick(double x, double y) {
        int col = (int) (x / (cellSize * zoomFactor));
        int row = (int) (y / (cellSize * zoomFactor));

        if (col >= 0 && col < sizeX && row >= 0 && row < sizeY) {
            PlacedPiece pp = getPlacementAt(row, col);
            if (pp != null) {
                // Click on occupied cell: remove piece, reset its state to unplaced, and deselect the cell
                saveToUndoStack();
                placements.remove(pp);
                selectedRow = -1;
                selectedCol = -1;
                pieceTableView.getSelectionModel().clearSelection();
            } else {
                // Click on empty cell: toggle selection or select and place if a piece is selected
                if (selectedRow == row && selectedCol == col) {
                    // Toggle / deselect if already active
                    selectedRow = -1;
                    selectedCol = -1;
                } else {
                    selectedRow = row;
                    selectedCol = col;
                    
                    Long selectedPiece = pieceTableView.getSelectionModel().getSelectedItem();
                    if (selectedPiece != null) {
                        int id = PiecePrimitive.getId(selectedPiece);
                        PlacedPiece existing = getPlacementForPiece(id);
                        if (existing != null) {
                            new Alert(Alert.AlertType.WARNING, "This piece is already placed on the board!").show();
                        } else {
                            long pieceToPlace = selectedPiece;
                            boolean placedValid = false;
                            int bestRot = 0;
                            for (int rot = 0; rot < 4; rot++) {
                                if (isValidPlacement(row, col, pieceToPlace)) {
                                    bestRot = rot;
                                    placedValid = true;
                                    break;
                                }
                                pieceToPlace = org.game.eternity2.model.PiecePrimitive.rotateCW(pieceToPlace);
                            }
                            
                            if (placedValid) {
                                saveToUndoStack();
                                placements.removeIf(p -> p.row == row && p.col == col);
                                placements.add(new PlacedPiece(row, col, id, bestRot, false));
                                selectedRow = -1;
                                selectedCol = -1;
                                pieceTableView.getSelectionModel().clearSelection();
                            } else {
                                new Alert(Alert.AlertType.ERROR, "No valid rotation for this piece fits at this cell!").show();
                            }
                        }
                    }
                }
            }
            updatePieceList();
            drawGrid();
            pieceTableView.refresh();
        } else {
            // Click outside puzzle resets selection
            selectedRow = -1;
            selectedCol = -1;
            pieceTableView.getSelectionModel().clearSelection();
            updatePieceList();
            drawGrid();
            pieceTableView.refresh();
        }
    }

    private void removePlacementAt(int row, int col) {
        PlacedPiece pp = getPlacementAt(row, col);
        if (pp != null) {
            saveToUndoStack();
            placements.remove(pp);
            selectedRow = row;
            selectedCol = col;
            updatePieceList();
            drawGrid();
            pieceTableView.refresh();
        }
    }

    private void updateSize(String sizeStr) {
        updateSize(sizeStr, true);
    }

    private void updateSize(String sizeStr, boolean clear) {
        if (isUpdatingSize) return;
        isUpdatingSize = true;
        try {
            final String fSizeStr = sizeStr;
            javafx.application.Platform.runLater(() -> {
                boolean oldGuard = isUpdatingSize;
                isUpdatingSize = true;
                try {
                    if (sizeCombo != null && !fSizeStr.equals(sizeCombo.getValue())) sizeCombo.setValue(fSizeStr);
                    if (rightSizeCombo != null && !fSizeStr.equals(rightSizeCombo.getValue())) rightSizeCombo.setValue(fSizeStr);
                } finally {
                    isUpdatingSize = oldGuard;
                }
            });

            if (sizeStr.startsWith("4x4")) {
                sizeX = 4;
                sizeY = 4;
                patternSpinner.getValueFactory().setValue(4);
            } else if (sizeStr.startsWith("6x6")) {
                sizeX = 6;
                sizeY = 6;
                patternSpinner.getValueFactory().setValue(10);
            } else if (sizeStr.startsWith("12x6")) {
                sizeX = 12;
                sizeY = 6;
                patternSpinner.getValueFactory().setValue(10);
            } else if (sizeStr.startsWith("16x16")) {
                sizeX = 16;
                sizeY = 16;
                patternSpinner.getValueFactory().setValue(22);
            }

            if (clear) {
                placements.clear();
                pieceLibrary.clear();
                undoStack.clear();
                selectedRow = -1;
                selectedCol = -1;
            }

            if (sizeX == 4) {
                cellSize = 75;
            } else if (sizeX == 6) {
                cellSize = 60;
            } else if (sizeX == 12) {
                cellSize = 45;
            } else {
                cellSize = 32;
            }

            boardCanvas.setWidth(sizeX * cellSize);
            boardCanvas.setHeight(sizeY * cellSize);

            double stageWidth = 1020;
            double stageHeight = 780;
            if (sizeStr.startsWith("4x4")) {
                stageWidth = 800;
                stageHeight = 620;
            } else if (sizeStr.startsWith("6x6")) {
                stageWidth = 880;
                stageHeight = 680;
            } else if (sizeStr.startsWith("12x6")) {
                stageWidth = 1020;
                stageHeight = 620;
            } else if (sizeStr.startsWith("16x16")) {
                stageWidth = 1020;
                stageHeight = 780;
            }
            this.setWidth(stageWidth);
            this.setHeight(stageHeight);
            
            updatePieceList();
            refreshMotifsDisplay();
            drawGrid();
        } finally {
            isUpdatingSize = false;
        }
    }

    private void updatePieceList() {
        pieceTableView.getItems().setAll(pieceLibrary);
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

        TextField idField = new TextField(existing == null ? "0" : String.valueOf(PiecePrimitive.getId(existing)));
        TextField topField = new TextField(existing == null ? "1" : String.valueOf(PiecePrimitive.getTop(existing)));
        TextField rightField = new TextField(existing == null ? "1" : String.valueOf(PiecePrimitive.getRight(existing)));
        TextField bottomField = new TextField(existing == null ? "1" : String.valueOf(PiecePrimitive.getBottom(existing)));
        TextField leftField = new TextField(existing == null ? "1" : String.valueOf(PiecePrimitive.getLeft(existing)));

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
                    return PiecePrimitive.create(
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
            saveToUndoStack();
            if (existing != null) {
                int oldId = PiecePrimitive.getId(existing);
                int newId = PiecePrimitive.getId(piece);
                if (oldId != newId) {
                    for (PlacedPiece p : placements) {
                        if (p.pieceId == oldId) {
                            p.pieceId = newId;
                        }
                    }
                }
                pieceLibrary.remove(existing);
            }
            pieceLibrary.add(piece);
            updatePieceList();
            drawGrid();
        });
    }

    private void generateRandomPuzzle() {
        saveToUndoStack();
        
        int width = sizeX;
        int height = sizeY;
        
        int[][] hEdges = new int[height][width - 1];
        int[][] vEdges = new int[height - 1][width];
        
        Random rnd = new Random();
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
        placements.clear();
        selectedRow = -1;
        selectedCol = -1;
        
        List<Long> solvedPieces = new ArrayList<>();
        int idCounter = 1;
        
        int[] appliedRotations = new int[width * height + 1];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int top = (y == 0) ? 0 : vEdges[y - 1][x];
                int bottom = (y == height - 1) ? 0 : vEdges[y][x];
                int left = (x == 0) ? 0 : hEdges[y][x - 1];
                int right = (x == width - 1) ? 0 : hEdges[y][x];
                
                long piece = PiecePrimitive.create(idCounter, top, right, bottom, left);
                solvedPieces.add(piece);
                idCounter++;
            }
        }

        List<Long> shuffled = new ArrayList<>(solvedPieces);
        Collections.shuffle(shuffled);
        
        for (Long p : shuffled) {
            int id = PiecePrimitive.getId(p);
            long rotated = p;
            int rotations = rnd.nextInt(4);
            appliedRotations[id] = rotations;
            for (int i = 0; i < rotations; i++) {
                rotated = PiecePrimitive.rotateCW(rotated);
            }
            pieceLibrary.add(rotated);
        }
        
        // Show solved solution directly on the board, compensating for library piece rotations!
        idCounter = 1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int id = idCounter;
                int libraryRot = appliedRotations[id];
                int placementRot = (4 - libraryRot) % 4;
                placements.add(new PlacedPiece(y, x, id, placementRot, false));
                idCounter++;
            }
        }
        
        updatePieceList();
        drawGrid();
    }

    private void drawGrid() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());

        for (PlacedPiece p : placements) {
            double x = p.col * cellSize;
            double y = p.row * cellSize;
            
            long piece = getPieceFromLibrary(p.pieceId);
            if (piece != 0) {
                // Apply CW rotation p.rotation times
                for (int i = 0; i < p.rotation; i++) piece = PiecePrimitive.rotateCW(piece);
                org.game.eternity2.util.BoardRenderer.drawPiece(gc, piece, (int)x, (int)y, (int)cellSize);
            } else {
                gc.setFill(Color.LIGHTBLUE);
                gc.fillRect(x, y, cellSize, cellSize);
                gc.setFill(Color.BLACK);
                gc.fillText(String.valueOf(p.pieceId), x + 5, y + 15);
            }

            // Sleek Golden frame indicating placed piece is locked as hint
            if (p.isHint) {
                gc.setStroke(Color.GOLD);
                gc.setLineWidth(2.5);
                gc.strokeRect(x + 1.5, y + 1.5, cellSize - 3, cellSize - 3);
            }
        }

        // Highlight selected cell in Red
        if (selectedRow >= 0 && selectedCol >= 0) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2.0);
            gc.strokeRect(selectedCol * cellSize, selectedRow * cellSize, cellSize, cellSize);
        }

        // Grid lines drawing
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
                        PiecePrimitive.getId(p),
                        PiecePrimitive.getTop(p),
                        PiecePrimitive.getRight(p),
                        PiecePrimitive.getBottom(p),
                        PiecePrimitive.getLeft(p)
                    ));
                }
                up.hints = new ArrayList<>();
                up.currentBoard = new org.game.eternity2.model.UnifiedPuzzle.BoardData();
                up.currentBoard.placements = new ArrayList<>();
                for (PlacedPiece pp : placements) {
                    up.currentBoard.placements.add(new org.game.eternity2.model.UnifiedPuzzle.PlacementData(pp.col, pp.row, pp.pieceId, pp.rotation));
                    if (pp.isHint) {
                        up.hints.add(new org.game.eternity2.model.UnifiedPuzzle.HintData(pp.col, pp.row, pp.pieceId, pp.rotation));
                    }
                }
                up.patterns = up.getPatterns();
                PuzzleLoaderWriter.saveUnified(file.toPath(), up);
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
                org.game.eternity2.model.UnifiedPuzzle up = PuzzleLoaderWriter.loadUnified(file.toPath());
                sizeX = up.width;
                sizeY = up.height;
                placements.clear();
                pieceLibrary.clear();
                undoStack.clear();
                selectedRow = -1;
                selectedCol = -1;
                
                for (org.game.eternity2.model.UnifiedPuzzle.PieceData pd : up.pieces) {
                    pieceLibrary.add(PiecePrimitive.create(pd.id, pd.top, pd.right, pd.bottom, pd.left));
                }

                if (up.currentBoard != null && up.currentBoard.placements != null) {
                    for (org.game.eternity2.model.UnifiedPuzzle.PlacementData pd : up.currentBoard.placements) {
                        boolean isHint = false;
                        for (org.game.eternity2.model.UnifiedPuzzle.HintData hd : up.hints) {
                            if (hd.pieceId == pd.pieceId) {
                                isHint = true;
                                break;
                            }
                        }
                        placements.add(new PlacedPiece(pd.y, pd.x, pd.pieceId, pd.rotation, isHint));
                    }
                } else {
                    for (org.game.eternity2.model.UnifiedPuzzle.HintData hd : up.hints) {
                        placements.add(new PlacedPiece(hd.y, hd.x, hd.pieceId, hd.rotation, true));
                    }
                }
                
                updatePieceList();
                
                // Triggers visual updates and size adjustment
                String targetSize = sizeX + "x" + sizeY;
                updateSize(targetSize, false);
                
                if (up.patterns > 0) {
                    patternSpinner.getValueFactory().setValue(up.patterns);
                }
                
                drawGrid();
                pieceTableView.refresh();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to load design: " + ex.getMessage()).show();
            }
        }
    }

    private void loadDesignFromResource(String puzzleName) {
        try {
            org.game.eternity2.model.UnifiedPuzzle up = PuzzleLoaderWriter.loadSmart(puzzleName);
            sizeX = up.width;
            sizeY = up.height;
            placements.clear();
            pieceLibrary.clear();
            undoStack.clear();
            selectedRow = -1;
            selectedCol = -1;
            
            for (org.game.eternity2.model.UnifiedPuzzle.PieceData pd : up.pieces) {
                pieceLibrary.add(PiecePrimitive.create(pd.id, pd.top, pd.right, pd.bottom, pd.left));
            }

            if (up.currentBoard != null && up.currentBoard.placements != null) {
                for (org.game.eternity2.model.UnifiedPuzzle.PlacementData pd : up.currentBoard.placements) {
                    boolean isHint = false;
                    for (org.game.eternity2.model.UnifiedPuzzle.HintData hd : up.hints) {
                        if (hd.pieceId == pd.pieceId) {
                            isHint = true;
                            break;
                        }
                    }
                    placements.add(new PlacedPiece(pd.y, pd.x, pd.pieceId, pd.rotation, isHint));
                }
            } else {
                for (org.game.eternity2.model.UnifiedPuzzle.HintData hd : up.hints) {
                    placements.add(new PlacedPiece(hd.y, hd.x, hd.pieceId, hd.rotation, true));
                }
            }
            
            updatePieceList();
            
            // Triggers visual updates and size adjustment
            String targetSize = sizeX + "x" + sizeY;
            updateSize(targetSize, false);
            
            if (up.patterns > 0) {
                patternSpinner.getValueFactory().setValue(up.patterns);
            }
            
            drawGrid();
            pieceTableView.refresh();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to load resource design: " + ex.getMessage()).show();
        }
    }
}
