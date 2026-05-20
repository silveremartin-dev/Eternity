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
package org.game.eternity2.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JavaFX UI for Eternity client with enhanced statistics.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 * @author Antigravity
 * @since 1.0
 */
public class ClientApp extends Application implements ClientUI {
    private static final Logger logger = LogManager.getLogger(ClientApp.class);

    private TextArea logArea;
    private Label statusLabel;
    private Label jobStatusLabel;
    private Label statsLabel;
    private Label bestScoreLabel;
    private Button connectBtn;
    private Button disconnectBtn;
    private CheckBox gpuCheckBox;
    private EternityClient client;
    private GridPane boardGrid;
    private ScrollPane boardScroll;
    private javafx.scene.chart.LineChart<Number, Number> throughputChart;
    private javafx.scene.chart.LineChart<Number, Number> scoreChart;
    private javafx.scene.chart.XYChart.Series<Number, Number> throughputSeries;
    private javafx.scene.chart.XYChart.Series<Number, Number> bestScoreSeries;
    private long startTime;
    private double zoomFactor = 1.0;
    private ClientStatistics statistics;
    private int lastRenderedScore = -1;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Eternity Client - Solver");
        startTime = System.currentTimeMillis();

        client = new EternityClient();
        client.setUi(this);
        statistics = client.getStatistics();
        statistics.load(new java.io.File("data/client_stats.properties"));

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu toolsMenu = new Menu("Tools");
        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.setOnAction(e -> showAlert("Settings", "Configuration file located at: client-config.properties"));
        toolsMenu.getItems().add(settingsItem);
        menuBar.getMenus().add(toolsMenu);

        // Controls
        connectBtn = new Button("Connect");
        connectBtn.setOnAction(e -> {
            throughputSeries.getData().clear();
            bestScoreSeries.getData().clear();
            startTime = System.currentTimeMillis();
            lastRenderedScore = -1;
            client.connect();
        });

        disconnectBtn = new Button("Disconnect");
        disconnectBtn.setOnAction(e -> client.disconnect());
        disconnectBtn.setDisable(true);

        gpuCheckBox = new CheckBox("GPU (TornadoVM)");
        gpuCheckBox.setOnAction(e -> client.setUseGPU(gpuCheckBox.isSelected()));

        HBox controls = new HBox(10, connectBtn, disconnectBtn, gpuCheckBox);
        controls.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        controls.setPadding(new Insets(5));

        // Performance chart
        javafx.scene.chart.NumberAxis xAxis1 = new javafx.scene.chart.NumberAxis();
        xAxis1.setLabel("Time (s)");
        javafx.scene.chart.NumberAxis yAxis1 = new javafx.scene.chart.NumberAxis();
        yAxis1.setLabel("Throughput (PPS)");
        
        throughputChart = new javafx.scene.chart.LineChart<>(xAxis1, yAxis1);
        throughputChart.setTitle("Performance");
        throughputChart.setCreateSymbols(false);
        throughputChart.setLegendVisible(true);
        throughputChart.setPrefHeight(180);

        throughputSeries = new javafx.scene.chart.XYChart.Series<>();
        throughputSeries.setName("Throughput (PPS)");
        throughputChart.getData().add(throughputSeries);

        // Score chart
        javafx.scene.chart.NumberAxis xAxis2 = new javafx.scene.chart.NumberAxis();
        xAxis2.setLabel("Time (s)");
        javafx.scene.chart.NumberAxis yAxis2 = new javafx.scene.chart.NumberAxis();
        yAxis2.setLabel("Best Score");

        scoreChart = new javafx.scene.chart.LineChart<>(xAxis2, yAxis2);
        scoreChart.setTitle("Best Score Progression");
        scoreChart.setCreateSymbols(false);
        scoreChart.setLegendVisible(true);
        scoreChart.setPrefHeight(180);

        bestScoreSeries = new javafx.scene.chart.XYChart.Series<>();
        bestScoreSeries.setName("Best Score");
        scoreChart.getData().add(bestScoreSeries);

        VBox chartContainer = new VBox(10, throughputChart, scoreChart);

        // Statistics panel
        statsLabel = new Label("Waiting for connection...");
        statsLabel.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        VBox statsPanel = new VBox(statsLabel);
        statsPanel.setPadding(new Insets(10));
        statsPanel.setStyle("-fx-background-color: #e8f8e8; -fx-border-color: #4caf50;");

        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 11px;");

        // Board Display
        bestScoreLabel = new Label("Best Score: 0");
        bestScoreLabel.setStyle("-fx-font-weight: bold;");
        boardGrid = new GridPane();
        boardGrid.setStyle("-fx-background-color: #eeeeee;");

        javafx.scene.Group boardGroup = new javafx.scene.Group(boardGrid);
        boardScroll = new ScrollPane(boardGroup);
        boardScroll.setFitToWidth(true);
        boardScroll.setFitToHeight(true);
        boardScroll.setStyle("-fx-background: #eeeeee;");

        // Zoom support
        boardScroll.setOnScroll(e -> {
            if (e.isControlDown()) {
                double delta = e.getDeltaY();
                if (delta > 0)
                    zoomFactor *= 1.1;
                else
                    zoomFactor /= 1.1;
                boardGroup.setScaleX(zoomFactor);
                boardGroup.setScaleY(zoomFactor);
                e.consume();
            }
        });

        VBox rightPanel = new VBox(10, bestScoreLabel, boardScroll);
        VBox.setVgrow(boardScroll, javafx.scene.layout.Priority.ALWAYS);

        // Status bar
        statusLabel = new Label("● Disconnected");
        statusLabel.setStyle("-fx-text-fill: red;");
        jobStatusLabel = new Label("No job");
        HBox statusBar = new HBox(20, statusLabel, jobStatusLabel);
        statusBar.setPadding(new Insets(5));

        // Layout
        VBox leftPanel = new VBox(10, statsPanel, chartContainer, logArea);
        HBox mainContent = new HBox(10, leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(rightPanel, javafx.scene.layout.Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(mainContent);
        root.setBottom(new VBox(controls, statusBar));

        Scene scene = new Scene(root, 1000, 700);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/chart.css").toExternalForm());
        } catch (Exception ex) {
            logger.warn("Could not load chart.css style: {}", ex.getMessage());
        }
        primaryStage.setScene(scene);
        try {
            java.io.InputStream iconStream = getClass().getResourceAsStream("/images/client_icon.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(iconStream));
            } else {
                logger.warn("Client icon resource not found: /images/client_icon.png");
            }
        } catch (Exception e) {
            logger.error("Failed to load client icon", e);
        }
        primaryStage.show();
        try {
            org.game.eternity2.util.BoardRenderer.renderBoard(boardGrid, new org.game.eternity2.model.BoardPrimitive(
                    statistics.getBoardWidth(), statistics.getBoardHeight()), 600, 600);
            bestScoreLabel.setText("Best Score: -");
        } catch (Exception ignored) {
        }
        startStatsPoller();
    }

    @Override
    public void stop() {
        if (statistics != null) {
            statistics.save(new java.io.File("data/client_stats.properties"));
        }
        if (client != null)
            client.disconnect();
    }

    private void startStatsPoller() {
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors
                .newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                });

        scheduler.scheduleAtFixedRate(() -> {
            if (client != null && client.isConnected()) {
                Platform.runLater(() -> {
                    ClientStatistics stats = client.getStatistics();
                    statsLabel.setText(String.format(
                            "SESSION:\nCompleted Jobs: %d | Pieces: %d\nBacktracks: %d | Best: %s\nPackets: %d data (S: %d, R: %d) / %d stat (S: %d, R: %d)\n\nTOTAL:\nCompleted Jobs: %d | Pieces: %d\nBacktracks: %d",
                            stats.getJobsCompleted(), stats.getPiecesPlaced(), stats.getBacktrackCount(),
                            org.game.eternity2.util.BoardRenderer.formatScore(stats.getBestScore(),
                                    stats.getBoardWidth(), stats.getBoardHeight()),
                            stats.getDataPacketsSent() + stats.getDataPacketsReceived(), stats.getDataPacketsSent(), stats.getDataPacketsReceived(),
                            stats.getStatPacketsSent() + stats.getStatPacketsReceived(), stats.getStatPacketsSent(), stats.getStatPacketsReceived(),
                            stats.getTotalJobsCompleted(), stats.getTotalPiecesPlaced(),
                            stats.getTotalBacktrackCount()));

                    // Update throughput chart
                    double timeSec = (System.currentTimeMillis() - startTime) / 1000.0;
                    throughputSeries.getData()
                            .add(new javafx.scene.chart.XYChart.Data<>(timeSec, stats.getPiecesPerSecond()));
                    if (throughputSeries.getData().size() > 100)
                        throughputSeries.getData().remove(0);

                    // Render intermediate local best board immediately on the grid
                    org.game.eternity2.model.BoardPrimitive bestB = stats.getBestBoard();
                    if (bestB != null && bestB.computeScore() > lastRenderedScore) {
                        lastRenderedScore = bestB.computeScore();
                        org.game.eternity2.util.BoardRenderer.renderBoard(boardGrid, bestB, 600, 600);
                        bestScoreLabel.setText("Best Score: " + org.game.eternity2.util.BoardRenderer
                                .formatScore(bestB.computeScore(), bestB.getWidth(), bestB.getHeight()));
                        
                        bestScoreSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(timeSec, bestB.computeScore()));
                        if (bestScoreSeries.getData().size() > 100)
                            bestScoreSeries.getData().remove(0);
                    }
                });
            }
        }, 0, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Override
    public void log(String msg) {
        Platform.runLater(() -> {
            logArea.appendText(msg + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    @Override
    public void setConnected(boolean connected) {
        Platform.runLater(() -> {
            statusLabel.setText(connected ? "● Connected" : "● Disconnected");
            statusLabel.setStyle("-fx-text-fill: " + (connected ? "green" : "red") + ";");
            connectBtn.setDisable(connected);
            disconnectBtn.setDisable(!connected);
        });
    }

    @Override
    public void setJobStatus(String status) {
        Platform.runLater(() -> jobStatusLabel.setText(status));
    }

    @Override
    public void updateBestBoard(org.game.eternity2.model.BoardPrimitive board) {
        Platform.runLater(() -> {
            lastRenderedScore = board.computeScore();
            org.game.eternity2.util.BoardRenderer.renderBoard(boardGrid, board, 600, 600);
            bestScoreLabel.setText("Best Score: " + org.game.eternity2.util.BoardRenderer
                    .formatScore(board.computeScore(), board.getWidth(), board.getHeight()));

            double timeSec = (System.currentTimeMillis() - startTime) / 1000.0;
            bestScoreSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(timeSec, board.computeScore()));
            if (bestScoreSeries.getData().size() > 100)
                bestScoreSeries.getData().remove(0);
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        System.setProperty("appType", "client");
        launch(args);
    }
}
