package org.game.eternity2.server.monitoring;

import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Simple HTTP server that exposes Prometheus metrics on /metrics endpoint.
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
