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
    private Button startBtn;
    private Button stopBtn;

    private Button browseBtn;
    private Label selectedFileLabel;
    private java.io.File selectedPuzzleFile;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Eternity Server - Distributed Solver");
        server = new EternityServer(EternityServer.DEFAULT_PORT);

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAlert("About", "Eternity Server v2.0\nDistributed Puzzle Solver"));
        helpMenu.getItems().add(aboutItem);
        menuBar.getMenus().add(helpMenu);

        Menu toolsMenu = new Menu("Tools");
        MenuItem designerItem = new MenuItem("Puzzle Designer");
        designerItem.setOnAction(e -> new PuzzleDesigner().show());
        toolsMenu.getItems().add(designerItem);
        menuBar.getMenus().add(toolsMenu);

        // Controls
        startBtn = new Button("Start Server");
        startBtn.setTooltip(new Tooltip("Start the server and begin accepting client connections"));

        stopBtn = new Button("Stop Server");
        stopBtn.setTooltip(new Tooltip("Stop the server and disconnect all clients"));
        stopBtn.setDisable(true);
        stopBtn.setOnAction(e -> {
            server.stopServer();
            // Re-enable controls will be handled in updateStatus or here?
            // Actually updateStatus handles start/stop buttons.
            // We need to handle config controls here.
        });

        HBox controls = new HBox(10, startBtn, stopBtn);
        controls.setPadding(new Insets(5));

        // Status bar
        statusLabel = new Label("● Stopped");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        clientCountLabel = new Label("Clients: 0");
        HBox statusBar = new HBox(20, statusLabel, clientCountLabel);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f5f5f5;");

        // Configuration Panel
        Label configTitle = new Label("Game Configuration");
        configTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label sizeLabel = new Label("Board Size:");
        ComboBox<String> sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll("4x4 (Demo)", "6x6 (Easy)", "12x6 (Medium)", "16x16 (Full)", "Custom (Load File)");
        sizeCombo.getSelectionModel().select(0); // Default 4x4 for quick demo

        browseBtn = new Button("Browse...");
        browseBtn.setDisable(true);
        selectedFileLabel = new Label("No file selected");

        sizeCombo.setOnAction(e -> {
            boolean isCustom = "Custom (Load File)".equals(sizeCombo.getValue());
            browseBtn.setDisable(!isCustom);
            if (!isCustom) {
                selectedFileLabel.setText("No file selected");
                selectedPuzzleFile = null;
            }
        });

        browseBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Open Puzzle File");
            fileChooser.setInitialDirectory(new java.io.File("."));
            fileChooser.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("Puzzle Files", "*.puzzle"));
            selectedPuzzleFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedPuzzleFile != null) {
                selectedFileLabel.setText(selectedPuzzleFile.getName());
            }
        });

        Label strategyLabel = new Label("Strategy:");
        ComboBox<String> strategyCombo = new ComboBox<>();
        strategyCombo.getItems().addAll("BorderFirst", "Scanline");
        strategyCombo.getSelectionModel().select(0);

        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(5);
        configGrid.add(sizeLabel, 0, 0);
        configGrid.add(sizeCombo, 1, 0);
        configGrid.add(browseBtn, 2, 0);
        configGrid.add(selectedFileLabel, 3, 0);
        configGrid.add(strategyLabel, 0, 1);
        configGrid.add(strategyCombo, 1, 1);

        VBox configPanel = new VBox(8, configTitle, configGrid);
        configPanel.setPadding(new Insets(10));
        configPanel.setStyle(
                "-fx-background-color: #fff3e0; -fx-border-color: #ff9800; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Statistics panel
        Label statsTitle = new Label("Server Statistics");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        jobsLabel = new Label("Jobs: Initializing...");
        packetsLabel = new Label("Packets: 0 sent, 0 received");

        VBox statsPanel = new VBox(8, statsTitle, jobsLabel, packetsLabel);
        statsPanel.setPadding(new Insets(10));
        statsPanel.setStyle(
                "-fx-background-color: #e8f4f8; -fx-border-color: #4a90e2; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Update Start Button Action
        startBtn.setOnAction(e -> {
            String selectedSize = sizeCombo.getSelectionModel().getSelectedItem();
            String selectedStrategy = strategyCombo.getSelectionModel().getSelectedItem();

            int x = 4, y = 4;
            java.util.List<Hint> hints = new java.util.ArrayList<>();

            if ("Custom (Load File)".equals(selectedSize)) {
                if (selectedPuzzleFile == null || !selectedPuzzleFile.exists()) {
                    showAlert("Error", "Please select a valid puzzle file.");
                    return;
                }
                // Parse file
                try (java.util.Scanner scanner = new java.util.Scanner(selectedPuzzleFile)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.startsWith("DIM=")) {
                            String[] parts = line.substring(4).split("x");
                            x = Integer.parseInt(parts[0]);
                            y = Integer.parseInt(parts[1]);
                        } else if (line.startsWith("HINT_")) {
                            String[] parts = line.split("=")[1].split(",");
                            int row = Integer.parseInt(parts[0]);
                            int col = Integer.parseInt(parts[1]);
                            int id = Integer.parseInt(parts[2]);
                            int rot = Integer.parseInt(parts[3]);
                            hints.add(new Hint(row, col, id, rot));
                        }
                    }
                } catch (Exception ex) {
                    showAlert("Error", "Failed to load puzzle file: " + ex.getMessage());
                    return;
                }
            } else {
                if (selectedSize.startsWith("6x6")) {
                    x = 6;
                    y = 6;
                } else if (selectedSize.startsWith("12x6")) {
                    x = 12;
                    y = 6;
                } else if (selectedSize.startsWith("16x16")) {
                    x = 16;
                    y = 16;
                }
            }

            server.initializeGame(x, y, selectedStrategy, hints);
            server.startServer();

            // Disable config while running
            sizeCombo.setDisable(true);
            browseBtn.setDisable(true);
            strategyCombo.setDisable(true);
        });

        stopBtn.setOnAction(e -> {
            server.stopServer();
            sizeCombo.setDisable(false);
            strategyCombo.setDisable(false);
            boolean isCustom = "Custom (Load File)".equals(sizeCombo.getValue());
            browseBtn.setDisable(!isCustom);
        });

        // Log area
        Label logTitle = new Label("Server Log");
        logTitle.setStyle("-fx-font-weight: bold;");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(350);
        logArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 11px;");

        VBox logPanel = new VBox(5, logTitle, logArea);
        logPanel.setPadding(new Insets(5));

        // Layout
        VBox centerPanel = new VBox(10, configPanel, statsPanel, logPanel);
        centerPanel.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(centerPanel);
        root.setBottom(new VBox(controls, statusBar));

        // Splash Screen
        Stage splashStage = new Stage();
        javafx.scene.image.Image splashImage = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/splash.png"));
        javafx.scene.image.ImageView splashView = new javafx.scene.image.ImageView(splashImage);
        Scene splashScene = new Scene(new javafx.scene.layout.StackPane(splashView));
        splashStage.setScene(splashScene);
        splashStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        splashStage.show();

        // Main Scene
        Scene scene = new Scene(root, 750, 600);
        primaryStage.setScene(scene);

        // Delay showing main stage
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        pause.setOnFinished(e -> {
            splashStage.close();
            primaryStage.show();
        });
        pause.play();

        // Wire server callbacks
        server.setGui(new ServerUI() {
            @Override
            public void log(String msg) {
                Platform.runLater(() -> {
                    logArea.appendText(msg + System.lineSeparator());
                    logArea.setScrollTop(Double.MAX_VALUE);
                });
            }

            @Override
            public void setServerStatus(boolean running) {
                Platform.runLater(() -> updateStatus(running));
            }

            @Override
            public void updateClientCount(int count) {
                Platform.runLater(() -> clientCountLabel.setText("Clients: " + count));
            }
        });
    }

    private void updateStatus(boolean running) {
        if (running) {
            statusLabel.setText("● Running");
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("● Stopped");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
        startBtn.setDisable(running);
        stopBtn.setDisable(!running);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        // Set log file identifier
        System.setProperty("appType", "server");
        launch(args);
    }
}
