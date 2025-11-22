package org.game.eternity2.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JavaFX UI for Eternity server with statistics panel.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class ServerApp extends Application {
    private static final Logger logger = LogManager.getLogger(ServerApp.class);
    private EternityServer server;
    private TextArea logArea;
    private Label statusLabel;
    private Label clientCountLabel;
    private Label jobsLabel;
    private Label packetsLabel;
    private Button startBtn;
    private Button stopBtn;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Eternity Server - Distributed Solver");
        server = new EternityServer(EternityServer.DEFAULT_PORT);

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAlert("About", "Eternity Server v2.0\\nDistributed Puzzle Solver"));
        helpMenu.getItems().add(aboutItem);
        menuBar.getMenus().add(helpMenu);

        // Controls
        startBtn = new Button("Start Server");
        startBtn.setOnAction(e -> server.startServer());
        stopBtn = new Button("Stop Server");
        stopBtn.setDisable(true);
        stopBtn.setOnAction(e -> server.stopServer());
        HBox controls = new HBox(10, startBtn, stopBtn);
        controls.setPadding(new Insets(5));

        // Status bar
        statusLabel = new Label("● Stopped");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        clientCountLabel = new Label("Clients: 0");
        HBox statusBar = new HBox(20, statusLabel, clientCountLabel);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f5f5f5;");

        // Statistics panel
        Label statsTitle = new Label("Server Statistics");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        jobsLabel = new Label("Jobs: Initializing...");
        packetsLabel = new Label("Packets: 0 sent, 0 received");

        VBox statsPanel = new VBox(8, statsTitle, jobsLabel, packetsLabel);
        statsPanel.setPadding(new Insets(10));
        statsPanel.setStyle(
                "-fx-background-color: #e8f4f8; -fx-border-color: #4a90e2; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");

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
        VBox centerPanel = new VBox(10, statsPanel, logPanel);
        centerPanel.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(centerPanel);
        root.setBottom(new VBox(controls, statusBar));

        Scene scene = new Scene(root, 750, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Wire server callbacks
        server.setGui(new ServerUI() {
            @Override
            public void log(String msg) {
                Platform.runLater(() -> {
                    logArea.appendText(msg + "\\n");
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
        launch(args);
    }
}
