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
package org.game.eternity2.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.game.eternity2.server.ServerStatus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Tab for monitoring server statistics from the client.
 * Displays real-time server status, job progress, and global best solution.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class ServerMonitorTab extends Tab {
    private final Label statusLabel;
    private final Label clientsLabel;
    private final Label jobsTotalLabel;
    private final Label jobsCompletedLabel;
    private final Label jobsPendingLabel;
    private final Label bestScoreLabel;
    private final Label uptimeLabel;

    private final ScheduledExecutorService scheduler;
    private ServerStatus lastStatus;

    public ServerMonitorTab() {
        setText("Server Monitor");
        setClosable(false);

        // Create labels
        statusLabel = new Label("Status: Unknown");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        clientsLabel = new Label("Connected Clients: -");
        jobsTotalLabel = new Label("Total Jobs: -");
        jobsCompletedLabel = new Label("Completed: -");
        jobsPendingLabel = new Label("Pending: -");
        bestScoreLabel = new Label("Best Score: -");
        uptimeLabel = new Label("Server Uptime: -");

        // Create grid for stats
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        grid.add(new Label("Server Status:"), 0, 0);
        grid.add(statusLabel, 1, 0);

        grid.add(new Label("Connected Clients:"), 0, 1);
        grid.add(clientsLabel, 1, 1);

        grid.add(new Label("Total Jobs:"), 0, 2);
        grid.add(jobsTotalLabel, 1, 2);

        grid.add(new Label("Completed Jobs:"), 0, 3);
        grid.add(jobsCompletedLabel, 1, 3);

        grid.add(new Label("Pending Jobs:"), 0, 4);
        grid.add(jobsPendingLabel, 1, 4);

        grid.add(new Label("Best Score Found:"), 0, 5);
        grid.add(bestScoreLabel, 1, 5);

        grid.add(new Label("Server Uptime:"), 0, 6);
        grid.add(uptimeLabel, 1, 6);

        // Style grid
        grid.setStyle("-fx-background-color: #2d2d30; -fx-border-color: #454545; " +
                "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);
        container.getChildren().add(grid);

        setContent(container);

        // Schedule periodic updates
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::requestServerStatus, 0, 3, TimeUnit.SECONDS);
    }

    /**
     * Update display with server status.
     *
     * @param status Server status
     */
    public void updateStatus(ServerStatus status) {
        this.lastStatus = status;

        javafx.application.Platform.runLater(() -> {
            statusLabel.setText(status.isRunning() ? "● RUNNING" : "● STOPPED");
            statusLabel
                    .setStyle(status.isRunning() ? "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4ec9b0;"
                            : "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f48771;");

            clientsLabel.setText(String.valueOf(status.getConnectedClients()));
            jobsTotalLabel.setText(String.valueOf(status.getTotalJobs()));
            jobsCompletedLabel.setText(String.valueOf(status.getCompletedJobs()));
            jobsPendingLabel.setText(String.valueOf(status.getPendingJobs()));
            bestScoreLabel.setText(String.valueOf(status.getBestScore()));

            long uptimeSeconds = status.getUptimeMs() / 1000;
            long hours = uptimeSeconds / 3600;
            long minutes = (uptimeSeconds % 3600) / 60;
            long seconds = uptimeSeconds % 60;
            uptimeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        });
    }

    /**
     * Request server status from the server.
     * This should be called by the client to fetch latest stats.
     */
    private void requestServerStatus() {
        // This will be implemented by the client to send SERVER_STATUS_REQUEST packet
        // For now, just update with last known status
        if (lastStatus != null) {
            updateStatus(lastStatus);
        }
    }

    public void stop() {
        scheduler.shutdown();
    }
}
