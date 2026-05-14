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
package org.game.eternity2.editor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import org.game.eternity2.io.PuzzleLoaderWriter;

import java.io.File;
import java.nio.file.Path;

/**
 * JavaFX Puzzle Editor for creating and editing Eternity II puzzles.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class PuzzleEditor extends Application {

    private int boardWidth = 4;
    private int boardHeight = 4;
    private GridPane boardGrid;
    private long[] pieces;
    private BoardPrimitive board;
    private int selectedPieceIndex = -1;

    // Edge colors (22 patterns + border)
    private static final Color[] EDGE_COLORS = new Color[23];
    static {
        EDGE_COLORS[0] = Color.GRAY; // Border
        EDGE_COLORS[1] = Color.RED;
        EDGE_COLORS[2] = Color.BLUE;
        EDGE_COLORS[3] = Color.GREEN;
        EDGE_COLORS[4] = Color.YELLOW;
        EDGE_COLORS[5] = Color.ORANGE;
        EDGE_COLORS[6] = Color.PURPLE;
        EDGE_COLORS[7] = Color.CYAN;
        EDGE_COLORS[8] = Color.MAGENTA;
        EDGE_COLORS[9] = Color.PINK;
        EDGE_COLORS[10] = Color.LIME;
        EDGE_COLORS[11] = Color.TEAL;
        EDGE_COLORS[12] = Color.NAVY;
        EDGE_COLORS[13] = Color.MAROON;
        EDGE_COLORS[14] = Color.OLIVE;
        EDGE_COLORS[15] = Color.AQUA;
        EDGE_COLORS[16] = Color.FUCHSIA;
        EDGE_COLORS[17] = Color.SILVER;
        EDGE_COLORS[18] = Color.CORAL;
        EDGE_COLORS[19] = Color.SALMON;
        EDGE_COLORS[20] = Color.KHAKI;
        EDGE_COLORS[21] = Color.PLUM;
        EDGE_COLORS[22] = Color.GOLD;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Eternity II Puzzle Editor");

        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Top toolbar
        ToolBar toolbar = createToolbar(stage);
        root.setTop(toolbar);

        // Center: Board grid
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(2);
        boardGrid.setVgap(2);
        boardGrid.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(boardGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        root.setCenter(scrollPane);

        // Right: Piece palette
        VBox palette = createPalette();
        root.setRight(palette);

        // Left: Workspace & Tools
        VBox leftWorkspace = createLeftWorkspace();
        root.setLeft(leftWorkspace);

        // Bottom: Status bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        // Initialize board
        initializeBoard();

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);

        // Add Icon
        try {
            stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/editor_icon.png")));
        } catch (Exception e) {
            // Ignore if icon missing
        }

        stage.show();
    }

    private ToolBar createToolbar(Stage stage) {
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: #16213e;");

        // New puzzle
        Button newBtn = new Button("New");
        newBtn.setOnAction(e -> showNewPuzzleDialog());

        // Open
        Button openBtn = new Button("Open Puzzle");
        openBtn.setOnAction(e -> openPuzzle(stage));

        // Save
        Button saveBtn = new Button("Save Pieces");
        saveBtn.setOnAction(e -> savePuzzle(stage));

        // Validate
        Button validateBtn = new Button("Check Compatibility");
        validateBtn.setOnAction(e -> validatePuzzle());

        // Clear
        Button clearBtn = new Button("Clear Board");
        clearBtn.setOnAction(e -> clearBoard());

        toolbar.getItems().addAll(
                newBtn, openBtn, saveBtn,
                new Separator(),
                validateBtn, clearBtn);

        Button saveSolutionBtn = new Button("Save Layout (.json)");
        saveSolutionBtn.setOnAction(e -> saveSolution(stage));
        
        Button loadEternityBtn = new Button("Load EII Set");
        loadEternityBtn.setOnAction(e -> loadEternity2Pieces());

        toolbar.getItems().addAll(new Separator(), saveSolutionBtn, loadEternityBtn);

        return toolbar;
    }

    private VBox createLeftWorkspace() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #16213e;");
        container.setPrefWidth(220);

        VBox editor = createPieceEditor();
        
        VBox boardControls = new VBox(10);
        Label title = new Label("Board Properties");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        GridPane grid = new GridPane();
        grid.setHgap(5); grid.setVgap(5);
        TextField wField = new TextField(String.valueOf(boardWidth));
        TextField hField = new TextField(String.valueOf(boardHeight));
        wField.setMaxWidth(50); hField.setMaxWidth(50);
        grid.add(new Label("W:"), 0, 0); grid.add(wField, 1, 0);
        grid.add(new Label("H:"), 0, 1); grid.add(hField, 1, 1);
        for(javafx.scene.Node n : grid.getChildren()) if(n instanceof Label) ((Label)n).setTextFill(Color.WHITE);
        
        Button resizeBtn = new Button("Resize Board");
        resizeBtn.setOnAction(e -> {
            try {
                boardWidth = Integer.parseInt(wField.getText());
                boardHeight = Integer.parseInt(hField.getText());
                initializeBoard();
            } catch (Exception ex) {}
        });
        resizeBtn.setMaxWidth(Double.MAX_VALUE);
        
        boardControls.getChildren().addAll(title, grid, resizeBtn);
        
        container.getChildren().addAll(editor, new Separator(), boardControls);
        return container;
    }

    private ListView<Long> pieceListView;

    private VBox createPalette() {
        VBox palette = new VBox(10);
        palette.setPadding(new Insets(10));
        palette.setStyle("-fx-background-color: #16213e;");
        palette.setPrefWidth(250);

        Label title = new Label("Piece Library");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        palette.getChildren().add(title);

        pieceListView = new ListView<>();
        pieceListView.setStyle("-fx-background-color: #1a1a2e;");
        pieceListView.setPrefHeight(600);
        pieceListView.setCellFactory(lv -> new ListCell<Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Pane view = createPieceView(item, 40);
                    setGraphic(view);
                    setText("ID: " + PiecePrimitive.getId(item));
                    setTextFill(Color.WHITE);
                }
            }
        });

        pieceListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                selectedPieceIndex = pieceListView.getSelectionModel().getSelectedIndex();
            }
        });

        palette.getChildren().add(pieceListView);
        return palette;
    }

    private TextField idField, topField, rightField, bottomField, leftField;
    private StackPane previewContainer;

    private VBox createPieceEditor() {
        VBox editor = new VBox(10);
        editor.setPadding(new Insets(10));
        editor.setStyle("-fx-background-color: #1a2a4a; -fx-background-radius: 5;");

        Label title = new Label("Piece Designer");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        previewContainer = new StackPane();
        previewContainer.setPrefSize(80, 80);
        updatePreview();

        GridPane grid = new GridPane();
        grid.setHgap(5); grid.setVgap(5);

        idField = new TextField("1");
        topField = new TextField("0");
        rightField = new TextField("0");
        bottomField = new TextField("0");
        leftField = new TextField("0");
        
        // Update preview on text change
        idField.textProperty().addListener((o, ov, nv) -> updatePreview());
        topField.textProperty().addListener((o, ov, nv) -> updatePreview());
        rightField.textProperty().addListener((o, ov, nv) -> updatePreview());
        bottomField.textProperty().addListener((o, ov, nv) -> updatePreview());
        leftField.textProperty().addListener((o, ov, nv) -> updatePreview());

        grid.add(new Label("ID:"), 0, 0); grid.add(idField, 1, 0);
        grid.add(new Label("T:"), 0, 1); grid.add(topField, 1, 1);
        grid.add(new Label("R:"), 0, 2); grid.add(rightField, 1, 2);
        grid.add(new Label("B:"), 0, 3); grid.add(bottomField, 1, 3);
        grid.add(new Label("L:"), 0, 4); grid.add(leftField, 1, 4);

        for (javafx.scene.Node node : grid.getChildren()) {
            if (node instanceof Label) ((Label) node).setTextFill(Color.WHITE);
        }

        Button addBtn = new Button("Add to Library");
        addBtn.setOnAction(e -> addPieceToLibrary());
        addBtn.setMaxWidth(Double.MAX_VALUE);

        editor.getChildren().addAll(title, previewContainer, grid, addBtn);
        return editor;
    }

    private void updatePreview() {
        try {
            int id = Integer.parseInt(idField.getText());
            int t = Integer.parseInt(topField.getText());
            int r = Integer.parseInt(rightField.getText());
            int b = Integer.parseInt(bottomField.getText());
            int l = Integer.parseInt(leftField.getText());
            long p = PiecePrimitive.create(id, t, r, b, l);
            previewContainer.getChildren().clear();
            previewContainer.getChildren().add(createPieceView(p, 80));
        } catch (Exception e) {}
    }

    private void addPieceToLibrary() {
        try {
            int id = Integer.parseInt(idField.getText());
            int t = Integer.parseInt(topField.getText());
            int r = Integer.parseInt(rightField.getText());
            int b = Integer.parseInt(bottomField.getText());
            int l = Integer.parseInt(leftField.getText());

            long piece = PiecePrimitive.create(id, t, r, b, l);
            long[] newPieces = new long[pieces.length + 1];
            System.arraycopy(pieces, 0, newPieces, 0, pieces.length);
            newPieces[pieces.length] = piece;
            pieces = newPieces;

            updatePieceList();
            idField.setText(String.valueOf(id + 1));
        } catch (Exception e) {
            showAlert("Error", "Invalid piece values");
        }
    }

    private void updatePieceList() {
        pieceListView.getItems().clear();
        for (long p : pieces) {
            pieceListView.getItems().add(p);
        }
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: #0f0f1a;");

        Label sizeLabel = new Label("Size: " + boardWidth + "x" + boardHeight);
        sizeLabel.setStyle("-fx-text-fill: #94a3b8;");

        Label piecesLabel = new Label("Pieces: 0");
        piecesLabel.setStyle("-fx-text-fill: #94a3b8;");

        Label placedLabel = new Label("Placed: 0");
        placedLabel.setStyle("-fx-text-fill: #94a3b8;");

        statusBar.getChildren().addAll(sizeLabel, piecesLabel, placedLabel);
        return statusBar;
    }

    private void initializeBoard() {
        board = new BoardPrimitive(boardWidth, boardHeight);
        pieces = new long[0];
        updateBoardView();
    }

    private void updateBoardView() {
        boardGrid.getChildren().clear();

        double cellSize = Math.min(500.0 / boardWidth, 500.0 / boardHeight);

        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                StackPane cell = createCell(x, y, cellSize);
                boardGrid.add(cell, x, y);
            }
        }
    }



    private Pane createPieceView(long piece, double size) {
        Pane pane = new Pane();
        pane.setPrefSize(size, size);

        // Background
        Rectangle bg = new Rectangle(size, size);
        bg.setFill(Color.web("#3d3d5c"));
        bg.setArcWidth(4);
        bg.setArcHeight(4);
        pane.getChildren().add(bg);

        // Edge triangles
        double half = size / 2;
        // Note: edge size calculated but visual rendering uses half

        // Top edge
        javafx.scene.shape.Polygon top = new javafx.scene.shape.Polygon(
                0, 0, size, 0, half, half);
        top.setFill(getEdgeColor(PiecePrimitive.getTop(piece)));
        pane.getChildren().add(top);

        // Right edge
        javafx.scene.shape.Polygon right = new javafx.scene.shape.Polygon(
                size, 0, size, size, half, half);
        right.setFill(getEdgeColor(PiecePrimitive.getRight(piece)));
        pane.getChildren().add(right);

        // Bottom edge
        javafx.scene.shape.Polygon bottom = new javafx.scene.shape.Polygon(
                0, size, size, size, half, half);
        bottom.setFill(getEdgeColor(PiecePrimitive.getBottom(piece)));
        pane.getChildren().add(bottom);

        // Left edge
        javafx.scene.shape.Polygon left = new javafx.scene.shape.Polygon(
                0, 0, 0, size, half, half);
        left.setFill(getEdgeColor(PiecePrimitive.getLeft(piece)));
        pane.getChildren().add(left);

        // Center with ID
        Label idLabel = new Label(String.valueOf(PiecePrimitive.getId(piece)));
        idLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");
        idLabel.setLayoutX(half - 5);
        idLabel.setLayoutY(half - 7);
        pane.getChildren().add(idLabel);

        return pane;
    }

    private Color getEdgeColor(int pattern) {
        if (pattern >= 0 && pattern < EDGE_COLORS.length) {
            return EDGE_COLORS[pattern];
        }
        return Color.WHITE;
    }

    private void onCellClicked(int x, int y) {
        if (selectedPieceIndex >= 0 && selectedPieceIndex < pieces.length) {
            board.placePiece(x, y, pieces[selectedPieceIndex]);
            updateBoardView();
        }
    }

    private void onCellRightClicked(int x, int y) {
        long piece = board.getPiece(x, y);
        if (piece != 0) {
            board.placePiece(x, y, PiecePrimitive.rotateCW(piece));
            updateBoardView();
        }
    }

    private StackPane createCell(int x, int y, double size) {
        StackPane cell = new StackPane();
        cell.setPrefSize(size, size);

        long piece = board.getPiece(x, y);

        if (piece == 0) {
            Rectangle bg = new Rectangle(size - 2, size - 2);
            bg.setFill(Color.web("#2d2d44"));
            bg.setStroke(Color.web("#444466"));
            bg.setStrokeWidth(1);
            cell.getChildren().add(bg);
        } else {
            Pane pieceView = createPieceView(piece, size - 2);
            cell.getChildren().add(pieceView);
            
            // Highlight if invalid
            if (!board.isValid(x, y)) {
                Rectangle border = new Rectangle(size, size);
                border.setFill(Color.TRANSPARENT);
                border.setStroke(Color.RED);
                border.setStrokeWidth(3);
                cell.getChildren().add(border);
            }
        }

        cell.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                if (e.isShiftDown()) {
                    // Middle-click / Shift-click behavior: Toggle "Hint" (just visual for now or logic if needed)
                    cell.setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
                } else {
                    onCellClicked(x, y);
                }
            } else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                onCellRightClicked(x, y);
            }
        });

        return cell;
    }
    private void showNewPuzzleDialog() {
        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("New Puzzle");
        dialog.setHeaderText("Create a new puzzle");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField widthField = new TextField("4");
        TextField heightField = new TextField("4");

        grid.add(new Label("Width:"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("Height:"), 0, 1);
        grid.add(heightField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new int[] {
                        Integer.parseInt(widthField.getText()),
                        Integer.parseInt(heightField.getText())
                };
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dims -> {
            boardWidth = dims[0];
            boardHeight = dims[1];
            initializeBoard();
        });
    }

    private void openPuzzle(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Puzzle");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Puzzle", "*.json"),
                new FileChooser.ExtensionFilter("Legacy Puzzle", "*.txt", "*.puzzle")
        );
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            try {
                if (file.getName().endsWith(".json")) {
                    // Try to load as solution first to get board state
                    board = PuzzleLoaderWriter.loadSolution(Path.of(file.getPath()), null);
                    boardWidth = board.getWidth();
                    boardHeight = board.getHeight();
                    // Extract pieces from board
                    java.util.List<Long> pieceList = new java.util.ArrayList<>();
                    for(int y=0; y<boardHeight; y++) {
                        for(int x=0; x<boardWidth; x++) {
                            long p = board.getPiece(x, y);
                            if (p != 0) pieceList.add(p);
                        }
                    }
                    pieces = pieceList.stream().mapToLong(l -> l).toArray();
                } else {
                    pieces = PuzzleLoaderWriter.loadPieces(Path.of(file.getPath()));
                    // Infer size
                    int count = pieces.length;
                    if (count == 16) { boardWidth = 4; boardHeight = 4; }
                    else if (count == 36) { boardWidth = 6; boardHeight = 6; }
                    else if (count == 256) { boardWidth = 16; boardHeight = 16; }
                    board = new BoardPrimitive(boardWidth, boardHeight);
                }
                updatePieceList();
                updateBoardView();
                showAlert("Open", "Loaded puzzle successfully");
            } catch (Exception e) {
                showAlert("Error", "Failed to load puzzle: " + e.getMessage());
            }
        }
    }

    private void saveSolution(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Solution");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                PuzzleLoaderWriter.saveSolution(Path.of(file.getPath()), board);
                showAlert("Save", "Saved solution to: " + file.getName());
            } catch (Exception e) {
                showAlert("Error", "Failed to save solution: " + e.getMessage());
            }
        }
    }

    private void saveHints(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Hints");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                // Same format as solution
                PuzzleLoaderWriter.saveSolution(Path.of(file.getPath()), board);
                showAlert("Save", "Saved hints to: " + file.getName());
            } catch (Exception e) {
                showAlert("Error", "Failed to save hints: " + e.getMessage());
            }
        }
    }

    private void loadEternity2Pieces() {
        try {
            pieces = PuzzleLoaderWriter.generateEternity2Pieces();
            boardWidth = 16;
            boardHeight = 16;
            initializeBoard();
            updatePieceList();
            showAlert("Eternity II", "Loaded 256 official Eternity II pieces");
        } catch (Exception e) {
            showAlert("Error", "Failed to load Eternity II pieces");
        }
    }

    private void savePuzzle(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Puzzle");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Puzzle", "*.json"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                PuzzleLoaderWriter.savePieces(Path.of(file.getPath()), pieces);
                showAlert("Save", "Saved pieces to: " + file.getName());
            } catch (Exception e) {
                showAlert("Error", "Failed to save puzzle: " + e.getMessage());
            }
        }
    }

    private void generateRandomPuzzle() {
        pieces = PuzzleLoaderWriter.generateEternity2Pieces();
        boardWidth = 16;
        boardHeight = 16;
        initializeBoard();
        showAlert("Generate", "Generated " + pieces.length + " random pieces for 16x16 board");
    }

    private void validatePuzzle() {
        boolean valid = board.isValid();
        if (valid) {
            showAlert("Validation", "Board is valid!");
        } else {
            showAlert("Validation", "Board has constraint violations");
        }
    }

    private void clearBoard() {
        initializeBoard();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
