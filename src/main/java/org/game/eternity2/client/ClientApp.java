package org.game.eternity2.client;

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
 * JavaFX UI for Eternity client with enhanced statistics.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class ClientApp extends Application implements ClientUI {
    private static final Logger logger = LogManager.getLogger(ClientApp.class);

    private TextArea logArea;
    private Label statusLabel;
    private Label jobStatusLabel;
    private Label statsLabel;
    private Button connectBtn;
    private Button disconnectBtn;
    private EternityClient client;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Eternity Client - Solver");
        client = new EternityClient();
        client.setUi(this);

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAlert("About", "Eternity Client v2.0\\nDistributed Solver"));
        helpMenu.getItems().add(aboutItem);
        menuBar.getMenus().add(helpMenu);

        // Controls
        connectBtn = new Button("Connect to Server");
        connectBtn.setOnAction(e -> client.connect());
        disconnectBtn = new Button("Disconnect");
        disconnectBtn.setOnAction(e -> client.disconnect());
        disconnectBtn.setDisable(true);
        HBox controls = new HBox(10, connectBtn, disconnectBtn);
        controls.setPadding(new Insets(5));

        // Status bar
        statusLabel = new Label("● Disconnected");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        jobStatusLabel = new Label("No job");
        HBox statusBar = new HBox(20, statusLabel, jobStatusLabel);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f5f5f5;");

        // Statistics panel
        Label statsTitle = new Label("Client Statistics");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        statsLabel = new Label("Jobs completed: 0");

        VBox statsPanel = new VBox(8, statsTitle, statsLabel);
        statsPanel.setPadding(new Insets(10));
        statsPanel.setStyle(
                "-fx-background-color: #e8f8e8; -fx-border-color: #4caf50; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Log area
        Label logTitle = new Label("Client Log");
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
    }

    @Override
    public void log(String msg) {
        Platform.runLater(() -> {
            logArea.appendText(msg + "\\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
        logger.info(msg);
    }

    @Override
    public void setConnected(boolean connected) {
        Platform.runLater(() -> {
            if (connected) {
                statusLabel.setText("● Connected");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                statusLabel.setText("● Disconnected");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
            connectBtn.setDisable(connected);
            disconnectBtn.setDisable(!connected);
        });
    }

    @Override
    public void setJobStatus(String status) {
        Platform.runLater(() -> jobStatusLabel.setText(status));
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
