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
package org.game.eternity2.server.monitoring;

import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Simple HTTP server that exposes Prometheus metrics on /metrics endpoint.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class MetricsServer implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(MetricsServer.class);
    private final HttpServer server;
    private final int port;

    public MetricsServer(int port) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        // /metrics endpoint for Prometheus
        server.createContext("/metrics", exchange -> {
            String response = MetricsProvider.getInstance().scrape();
            exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        // /health endpoint for liveness probe
        server.createContext("/health", exchange -> {
            String response = "{\"status\":\"UP\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        // /ready endpoint for readiness probe
        server.createContext("/ready", exchange -> {
            String response = "{\"status\":\"READY\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.setExecutor(null);
    }

    /**
     * Start the metrics HTTP server.
     */
    public void start() {
        server.start();
        LOGGER.info("Metrics server started on port {}", port);
        LOGGER.info("  /metrics - Prometheus metrics");
        LOGGER.info("  /health  - Liveness probe");
        LOGGER.info("  /ready   - Readiness probe");
    }

    @Override
    public void close() {
        server.stop(1);
        LOGGER.info("Metrics server stopped");
    }
}
