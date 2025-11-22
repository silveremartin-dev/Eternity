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

        chart.getData().addAll(packetsSeries, clientsSeries);

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
                statistics.getPacketsSent() + statistics.getPacketsReceived()));

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
