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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.game.eternity2.client.ClientStatistics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Real-time bar chart for client statistics.
 * Updates automatically every 2 seconds.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class ClientStatsChart {
    private final BarChart<String, Number> chart;
    private final XYChart.Series<String, Number> series;
    private final ScheduledExecutorService scheduler;

    public ClientStatsChart(ClientStatistics statistics) {
        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Metric");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");
        yAxis.setAutoRanging(true);

        // Create chart
        chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Client Statistics");
        chart.setAnimated(true);

        // Create series
        series = new XYChart.Series<>();
        series.setName("Current Values");

        chart.getData().add(series);

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

    private void updateChart(ClientStatistics statistics) {
        series.getData().clear();

        series.getData().add(new XYChart.Data<>("Jobs", statistics.getJobsCompleted()));
        series.getData().add(new XYChart.Data<>("Pieces", statistics.getPiecesPlaced()));
        series.getData().add(new XYChart.Data<>("Backtracks", statistics.getBacktrackCount()));
        series.getData().add(new XYChart.Data<>("Best Score", statistics.getBestScore()));
    }

    public BarChart<String, Number> getChart() {
        return chart;
    }

    public void stop() {
        scheduler.shutdown();
    }
}
