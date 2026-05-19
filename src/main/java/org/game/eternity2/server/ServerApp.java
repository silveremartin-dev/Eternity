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

import javafx.application.Application;
import org.game.eternity2.model.Hint;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX UI for Eternity server with statistics panel.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class ServerApp extends Application {

    private EternityServer server;
    private TextArea logArea;
    private Label statusLabel;
    private Label clientCountLabel;
    private Label jobsLabel;
    private Label packetsLabel;
    private Label solvingTimeLabel;
    private Label bestScoreLabel;
    private Button startBtn;
    private Button stopBtn;
    private GridPane boardDisplay;
    private ScrollPane boardScroll;
    private javafx.scene.chart.LineChart<Number, Number> throughputChart;
    private javafx.scene.chart.XYChart.Series<Number, Number> throughputSeries;
    private javafx.scene.chart.XYChart.Series<Number, Number> bestScoreSeries;
    private long startTimeMillis;
    private double zoomFactor = 1.0;

    private Button browseBtn;
    private Label selectedFileLabel;
    private java.io.File selectedPuzzleFile;

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Eternity Server - Distributed Solver");
        try {
            primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/images/server_icon.png").toExternalForm()));
        } catch (Exception e) {}
        
        server = new EternityServer(EternityServer.DEFAULT_PORT);

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu toolsMenu = new Menu("Tools");
        MenuItem designerItem = new MenuItem("Puzzle Designer");
        designerItem.setOnAction(e -> new PuzzleDesigner().show());
        toolsMenu.getItems().add(designerItem);
        menuBar.getMenus().add(toolsMenu);

        // Controls
        startBtn = new Button("Start Server");
        stopBtn = new Button("Stop Server");
        stopBtn.setDisable(true);

        HBox controls = new HBox(10, startBtn, stopBtn);
        controls.setPadding(new Insets(5));

        // Status bar
        statusLabel = new Label("● Stopped");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        clientCountLabel = new Label("Clients: 0");
        HBox statusBar = new HBox(20, statusLabel, clientCountLabel);
        statusBar.setPadding(new Insets(5));

        // Configuration Panel
        // Configuration Panel
        Label configTitle = new Label("Configuration");
        configTitle.setStyle("-fx-font-weight: bold;");
        ComboBox<String> puzzleCombo = new ComboBox<>();
        
        // Dynamic scanning of resources
        java.io.File puzzlesDir = new java.io.File("src/main/resources/puzzles/");
        refreshPuzzleList(puzzleCombo, puzzlesDir);
        
        puzzleCombo.getItems().add("Custom (.json)");
        puzzleCombo.getSelectionModel().select(0);

        browseBtn = new Button("Browse...");
        browseBtn.setDisable(true);
        selectedFileLabel = new Label("Selected: " + puzzleCombo.getValue());

        Button consolidateBtn = new Button("Consolidate All");
        consolidateBtn.setStyle("-fx-base: #e1f5fe;");
        consolidateBtn.setOnAction(e -> {
            try {
                org.game.eternity2.io.PuzzleLoaderWriter.consolidateResources(puzzlesDir.toPath());
                refreshPuzzleList(puzzleCombo, puzzlesDir);
                new Alert(Alert.AlertType.INFORMATION, "Consolidation complete! Legacy files merged into JSON.").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Consolidation failed: " + ex.getMessage()).show();
            }
        });

        Button saveBestBtn = new Button("Save Best Solution");
        saveBestBtn.setStyle("-fx-base: #e8f5e9;");
        saveBestBtn.setOnAction(e -> {
            org.game.eternity2.model.BoardPrimitive best = server.getMasterBoard();
            if (best == null || best.computeScore() == 0) {
                new Alert(Alert.AlertType.WARNING, "No solution found yet to save.").show();
                return;
            }
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Current Best Solution");
            fileChooser.setInitialFileName("puzzle_" + puzzleCombo.getValue() + "_best.json");
            java.io.File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    org.game.eternity2.io.PuzzleLoaderWriter.saveUnifiedSolution(file.toPath(), best, 22); // Assuming 22 patterns for E2
                    new Alert(Alert.AlertType.INFORMATION, "Best solution saved to " + file.getName()).show();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage()).show();
                }
            }
        });

        puzzleCombo.setOnAction(e -> {
            boolean isCustom = "Custom (.json)".equals(puzzleCombo.getValue());
            browseBtn.setDisable(!isCustom);
            selectedFileLabel.setText(isCustom ? "No file selected" : "Selected: " + puzzleCombo.getValue());
        });

        browseBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setInitialDirectory(new java.io.File("."));
            selectedPuzzleFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedPuzzleFile != null) selectedFileLabel.setText(selectedPuzzleFile.getName());
        });

        VBox configPanel = new VBox(8, configTitle, 
            new HBox(10, puzzleCombo, browseBtn, selectedFileLabel),
            new HBox(10, consolidateBtn, saveBestBtn));
        configPanel.setPadding(new Insets(10));
        configPanel.setStyle("-fx-background-color: #fff3e0; -fx-border-color: #ff9800;");

        // Statistics panel
        Label statsTitle = new Label("Server Statistics");
        statsTitle.setStyle("-fx-font-weight: bold;");
        jobsLabel = new Label("Jobs: 0/0");
        packetsLabel = new Label("Packets: 0/0");
        bestScoreLabel = new Label("Best Score: 0/0 (0.0%)");
        solvingTimeLabel = new Label("Solving Time: 00:00:00");

        VBox statsPanel = new VBox(8, statsTitle, jobsLabel, packetsLabel, bestScoreLabel, solvingTimeLabel);
        statsPanel.setPadding(new Insets(10));
        statsPanel.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #4a90e2;");

        // Throughput Chart
        javafx.scene.chart.NumberAxis xAxis = new javafx.scene.chart.NumberAxis();
        xAxis.setLabel("Time (s)");
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        yAxis.setLabel("Value");

        throughputChart = new javafx.scene.chart.LineChart<>(xAxis, yAxis);
        throughputChart.setTitle("System Performance");
        throughputChart.setCreateSymbols(false);
        throughputChart.setPrefHeight(250);

        throughputSeries = new javafx.scene.chart.XYChart.Series<>();
        throughputSeries.setName("Throughput (PPS)");
        bestScoreSeries = new javafx.scene.chart.XYChart.Series<>();
        bestScoreSeries.setName("Best Score");
        throughputChart.getData().addAll(throughputSeries, bestScoreSeries);

        // Start Server Action
        startBtn.setOnAction(e -> {
            startTimeMillis = System.currentTimeMillis();
            String selectedPuzzle = puzzleCombo.getValue();
            
            try {
                org.game.eternity2.model.UnifiedPuzzle up;
                if ("Custom (.json)".equals(selectedPuzzle)) {
                    if (selectedPuzzleFile == null) return;
                    try {
                        up = org.game.eternity2.io.PuzzleLoaderWriter.loadUnified(selectedPuzzleFile.toPath());
                    } catch (Exception ex) {
                        long[] pieces = org.game.eternity2.io.PuzzleLoaderWriter.loadPieces(selectedPuzzleFile.toPath());
                        up = new org.game.eternity2.model.UnifiedPuzzle();
                        up.pieces = new java.util.ArrayList<>();
                        for (long p : pieces) {
                            up.pieces.add(new org.game.eternity2.model.UnifiedPuzzle.PieceData(
                                org.game.eternity2.model.PiecePrimitive.getId(p),
                                org.game.eternity2.model.PiecePrimitive.getTop(p),
                                org.game.eternity2.model.PiecePrimitive.getRight(p),
                                org.game.eternity2.model.PiecePrimitive.getBottom(p),
                                org.game.eternity2.model.PiecePrimitive.getLeft(p)
                            ));
                        }
                        if (pieces.length == 16) { up.width = 4; up.height = 4; }
                        else if (pieces.length == 256) { up.width = 16; up.height = 16; }
                    }
                } else {
                    up = org.game.eternity2.io.PuzzleLoaderWriter.loadSmart(selectedPuzzle);
                }

                long[] pieces = org.game.eternity2.io.PuzzleLoaderWriter.toPrimitives(up);
                java.util.List<org.game.eternity2.model.Hint> hints = new java.util.ArrayList<>();
                for (org.game.eternity2.model.UnifiedPuzzle.HintData hd : up.hints) {
                    hints.add(new org.game.eternity2.model.Hint(hd.x, hd.y, hd.pieceId, hd.rotation));
                }

                server.initializeGame(up.width, up.height, "Scanline", hints, pieces);
                server.startServer();
                updateStatus(true);
            } catch (Exception ex) { 
                ex.printStackTrace(); 
                javafx.application.Platform.runLater(() -> logArea.appendText("Error loading puzzle: " + ex.getMessage() + "\n"));
            }
        });

        stopBtn.setOnAction(e -> {
            server.stopServer();
            updateStatus(false);
        });

        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 11px;");

        // Board Display
        boardDisplay = new GridPane();
        boardDisplay.setStyle("-fx-background-color: #eeeeee;");
        javafx.scene.Group boardGroup = new javafx.scene.Group(boardDisplay);
        boardScroll = new ScrollPane(boardGroup);
        boardScroll.setFitToWidth(true);
        boardScroll.setFitToHeight(true);
        boardScroll.setStyle("-fx-background: #eeeeee;");
        
        boardScroll.setOnScroll(e -> {
            if (e.isControlDown()) {
                double delta = e.getDeltaY();
                if (delta > 0) zoomFactor *= 1.1;
                else zoomFactor /= 1.1;
                boardGroup.setScaleX(zoomFactor);
                boardGroup.setScaleY(zoomFactor);
                e.consume();
            }
        });

        VBox rightPanel = new VBox(10, new Label("Best Solution Found"), boardScroll);
        VBox.setVgrow(boardScroll, javafx.scene.layout.Priority.ALWAYS);

        // Layout
        VBox leftPanel = new VBox(10, configPanel, statsPanel, throughputChart, logArea);
        HBox mainContent = new HBox(10, leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(rightPanel, javafx.scene.layout.Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(mainContent);
        root.setBottom(new VBox(controls, statusBar));

        primaryStage.setScene(new Scene(root, 1100, 850));
        primaryStage.show();

        server.setGui(new ServerUI() {
            @Override
            public void log(String msg) {
                Platform.runLater(() -> {
                    logArea.appendText(msg + "\n");
                    logArea.setScrollTop(Double.MAX_VALUE);
                });
            }
            @Override
            public void setServerStatus(boolean running) { Platform.runLater(() -> updateStatus(running)); }
            @Override
            public void updateClientCount(int count) { Platform.runLater(() -> clientCountLabel.setText("Clients: " + count)); }
            @Override
            public void updateBestBoard(org.game.eternity2.model.BoardPrimitive board) {
                Platform.runLater(() -> {
                    org.game.eternity2.util.BoardRenderer.renderBoard(boardDisplay, board, 600, 600);
                    bestScoreLabel.setText("Best Score: " + org.game.eternity2.util.BoardRenderer.formatScore(board.computeScore(), board.getWidth(), board.getHeight()));
                    double timeSec = (System.currentTimeMillis() - startTimeMillis) / 1000.0;
                    bestScoreSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(timeSec, board.computeScore()));
                });
            }
            @Override
            public void updateThroughput(double totalPps) {
                Platform.runLater(() -> {
                    double timeSec = (System.currentTimeMillis() - startTimeMillis) / 1000.0;
                    throughputSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(timeSec, totalPps));
                    if (throughputSeries.getData().size() > 100) throughputSeries.getData().remove(0);
                });
            }
        });

        startUIUpdater();
    }

    private void startUIUpdater() {
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            if (server != null && server.isRunning()) {
                Platform.runLater(() -> {
                    ServerStatistics stats = server.getStatistics();
                    JobManager.JobStatistics jobStats = server.getJobManager().getStatistics();
                    jobsLabel.setText(String.format("Jobs: %d/%d (%.1f%%)", jobStats.completed(), jobStats.total(), jobStats.getCompletionPercentage()));
                    packetsLabel.setText(String.format("Packets: %d sent, %d received", stats.getPacketsSent(), stats.getPacketsReceived()));
                    
                    long elapsed = System.currentTimeMillis() - startTimeMillis;
                    long s = (elapsed / 1000) % 60;
                    long m = (elapsed / (1000 * 60)) % 60;
                    long h = (elapsed / (1000 * 60 * 60));
                    solvingTimeLabel.setText(String.format("Solving Time: %02d:%02d:%02d", h, m, s));
                });
            }
        }, 0, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void updateStatus(boolean running) {
        statusLabel.setText(running ? "● Running" : "● Stopped");
        statusLabel.setStyle("-fx-text-fill: " + (running ? "green" : "red") + "; -fx-font-weight: bold;");
        startBtn.setDisable(running);
        stopBtn.setDisable(!running);
    }

    public static void main(String[] args) {
        System.setProperty("appType", "server");
        launch(args);
    }

    private void refreshPuzzleList(ComboBox<String> combo, java.io.File dir) {
        combo.getItems().clear();
        if (dir.exists()) {
            java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (java.io.File f : files) {
                    String name = f.getName().replace(".json", "");
                    if (!combo.getItems().contains(name)) combo.getItems().add(name);
                }
            }
            java.io.File[] legacy = dir.listFiles((d, name) -> name.endsWith(".puzzle"));
            if (legacy != null) {
                for (java.io.File f : legacy) {
                    String name = f.getName().replace(".puzzle", "");
                    if (!combo.getItems().contains(name)) combo.getItems().add(name);
                }
            }
        }
        if (combo.getItems().isEmpty()) {
            combo.getItems().addAll("16x16_eternity2", "4x4_demo", "6x6_training");
        }
    }
}
