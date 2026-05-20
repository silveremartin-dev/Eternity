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

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.game.eternity2.server.ServerStatistics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Real-time chart for server statistics.
 * Updates automatically every 2 seconds.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class ServerStatsChart {
    private final LineChart<Number, Number> chart;
    private final XYChart.Series<Number, Number> packetsSeries;
    private final XYChart.Series<Number, Number> clientsSeries;
    private final ScheduledExecutorService scheduler;
    private int timeCounter = 0;

    public ServerStatsChart(ServerStatistics statistics) {
        // Create axes
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");
        xAxis.setAutoRanging(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");
        yAxis.setAutoRanging(true);

        // Create chart
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Server Statistics");
        chart.setCreateSymbols(false);
        chart.setAnimated(false);

        // Create series
        packetsSeries = new XYChart.Series<>();
        packetsSeries.setName("Packets");

        clientsSeries = new XYChart.Series<>();
        clientsSeries.setName("Clients");

        chart.getData().add(packetsSeries);
        chart.getData().add(clientsSeries);

        // Schedule updates
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> updateChart(statistics));
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void updateChart(ServerStatistics statistics) {
        timeCounter += 2;

        // Add new data points
        packetsSeries.getData().add(new XYChart.Data<>(
                timeCounter,
                statistics.getDataPacketsSent() + statistics.getStatPacketsSent() + 
                statistics.getDataPacketsReceived() + statistics.getStatPacketsReceived()));

        clientsSeries.getData().add(new XYChart.Data<>(
                timeCounter,
                statistics.getConnectedClients()));

        // Keep only last 30 points (1 minute)
        if (packetsSeries.getData().size() > 30) {
            packetsSeries.getData().remove(0);
        }
        if (clientsSeries.getData().size() > 30) {
            clientsSeries.getData().remove(0);
        }
    }

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public void stop() {
        scheduler.shutdown();
    }
}
