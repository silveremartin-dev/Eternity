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
        scrollPane.setStyle("-fx-background: #1a1a2e;");
        root.setCenter(scrollPane);

        // Right: Piece palette
        VBox palette = createPalette();
        root.setRight(palette);

        // Bottom: Status bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        // Initialize board
        initializeBoard();

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }

    private ToolBar createToolbar(Stage stage) {
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: #16213e;");

        // New puzzle
        Button newBtn = new Button("New");
        newBtn.setOnAction(e -> showNewPuzzleDialog());

        // Open
        Button openBtn = new Button("Open");
        openBtn.setOnAction(e -> openPuzzle(stage));

        // Save
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> savePuzzle(stage));

        // Import TheSil
        Button importBtn = new Button("Import TheSil");
        importBtn.setOnAction(e -> importTheSil(stage));

        // Generate
        Button generateBtn = new Button("Generate Random");
        generateBtn.setOnAction(e -> generateRandomPuzzle());

        // Validate
        Button validateBtn = new Button("Validate");
        validateBtn.setOnAction(e -> validatePuzzle());

        // Clear
        Button clearBtn = new Button("Clear Board");
        clearBtn.setOnAction(e -> clearBoard());

        toolbar.getItems().addAll(
                newBtn, openBtn, saveBtn,
                new Separator(),
                importBtn, generateBtn,
                new Separator(),
                validateBtn, clearBtn);

        return toolbar;
    }

    private VBox createPalette() {
        VBox palette = new VBox(10);
        palette.setPadding(new Insets(10));
        palette.setStyle("-fx-background-color: #16213e;");
        palette.setPrefWidth(200);

        Label title = new Label("Pieces");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        palette.getChildren().add(title);

        // Piece list will be populated when puzzle is loaded
        ListView<String> pieceList = new ListView<>();
        pieceList.setStyle("-fx-background-color: #1a1a2e;");
        pieceList.setPrefHeight(400);
        palette.getChildren().add(pieceList);

        // Piece info
        Label infoLabel = new Label("Select a piece");
        infoLabel.setStyle("-fx-text-fill: #94a3b8;");
        palette.getChildren().add(infoLabel);

        return palette;
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

    private StackPane createCell(int x, int y, double size) {
        StackPane cell = new StackPane();
        cell.setPrefSize(size, size);

        long piece = board.getPiece(x, y);

        if (piece == 0) {
            // Empty cell
            Rectangle bg = new Rectangle(size - 4, size - 4);
            bg.setFill(Color.web("#2d2d44"));
            bg.setStroke(Color.web("#444466"));
            bg.setStrokeWidth(1);
            bg.setArcWidth(4);
            bg.setArcHeight(4);
            cell.getChildren().add(bg);
        } else {
            // Cell with piece - show edges
            Pane pieceView = createPieceView(piece, size - 4);
            cell.getChildren().add(pieceView);
        }

        // Click handler
        final int fx = x, fy = y;
        cell.setOnMouseClicked(e -> onCellClicked(fx, fy));

        return cell;
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
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Puzzle Files", "*.txt", "*.puzzle"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            try {
                pieces = PuzzleLoaderWriter.loadPieces(Path.of(file.getPath()));
                // Determine board size from piece count
                int pieceCount = pieces.length;
                if (pieceCount == 16) {
                    boardWidth = 4;
                    boardHeight = 4;
                } else if (pieceCount == 36) {
                    boardWidth = 6;
                    boardHeight = 6;
                } else if (pieceCount == 72) {
                    boardWidth = 12;
                    boardHeight = 6;
                } else if (pieceCount == 256) {
                    boardWidth = 16;
                    boardHeight = 16;
                }
                initializeBoard();
                showAlert("Open", "Loaded " + pieces.length + " pieces from: " + file.getName());
            } catch (Exception e) {
                showAlert("Error", "Failed to load puzzle: " + e.getMessage());
            }
        }
    }

    private void savePuzzle(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Puzzle");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Puzzle Files", "*.txt", "*.json"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                PuzzleLoaderWriter.savePieces(Path.of(file.getPath()), pieces);
                showAlert("Save", "Saved " + pieces.length + " pieces to: " + file.getName());
            } catch (Exception e) {
                showAlert("Error", "Failed to save puzzle: " + e.getMessage());
            }
        }
    }

    private void importTheSil(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import TheSil Pieces");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            try {
                pieces = PuzzleLoaderWriter.loadPieces(Path.of(file.getPath()));
                showAlert("Import", "Imported " + pieces.length + " pieces");
            } catch (Exception e) {
                showAlert("Error", "Failed to import: " + e.getMessage());
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
