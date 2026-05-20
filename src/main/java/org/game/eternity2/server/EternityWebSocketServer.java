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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Silvere Martin-Michiellot
 * @author Antigravity
 * @since 1.0
 */
public class EternityWebSocketServer extends WebSocketServer {
    private static final Logger logger = LogManager.getLogger(EternityWebSocketServer.class);

    private final EternityServer gameServer;
    private final Gson gson = new Gson();
    private final Map<WebSocket, String> authenticatedUsers = new ConcurrentHashMap<>();

    public EternityWebSocketServer(int port, EternityServer gameServer) {
        super(new InetSocketAddress(port));
        this.gameServer = gameServer;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("New WebSocket connection: {}", conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("Closed WebSocket connection: {}", conn.getRemoteSocketAddress());
        authenticatedUsers.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String command = json.get("command").getAsString();

            switch (command) {
                case "LOGIN":
                    handleLogin(conn, json);
                    break;
                case "JOB_REQUEST":
                    handleJobRequest(conn, json);
                    break;
                case "SERVER_STATS":
                    handleServerStats(conn);
                    break;
                case "RESULT_SUBMISSION":
                    handleResultSubmission(conn, json);
                    break;
                default:
                    sendError(conn, "Unknown command: " + command);
            }
        } catch (Exception e) {
            logger.error("Error processing WebSocket message", e);
            sendError(conn, "Invalid message format");
        }
    }

    private void handleServerStats(WebSocket conn) {
        JobManager.JobStatistics stats = gameServer.getJobManager().getStatistics();
        ServerStatistics serverStats = gameServer.getStatistics();

        JsonObject response = new JsonObject();
        response.addProperty("command", "SERVER_STATS");
        response.addProperty("activeClients", serverStats.getActiveClients());
        response.addProperty("totalJobs", stats.total());
        response.addProperty("completedJobs", stats.completed());
        response.addProperty("completionPercentage", stats.getCompletionPercentage());
        org.game.eternity2.model.BoardPrimitive masterBoard = gameServer.getMasterBoard();
        response.addProperty("bestScore", masterBoard != null ? masterBoard.computeScore() : 0);
        response.addProperty("uptimeMs", serverStats.getUptimeMs());

        conn.send(gson.toJson(response));
    }

    private void handleLogin(WebSocket conn, JsonObject json) {
        String username = json.get("username").getAsString();
        // Simplified login for now, just accept any username
        authenticatedUsers.put(conn, username);

        JsonObject response = new JsonObject();
        response.addProperty("command", "LOGIN_SUCCESS");
        response.addProperty("message", "Welcome " + username);
        conn.send(gson.toJson(response));
    }

    private void handleJobRequest(WebSocket conn, JsonObject json) {
        if (!authenticatedUsers.containsKey(conn)) {
            sendError(conn, "Not authenticated");
            return;
        }

        // In a real implementation, we would fetch a job from gameServer.jobManager
        // For now, we'll send a dummy job or hook into the existing job system if
        // possible
        // Since JobManager is private in EternityServer, we might need to expose it or
        // add a method

        // Sending a placeholder response
        JsonObject response = new JsonObject();
        response.addProperty("command", "NO_JOB");
        response.addProperty("message", "WebSocket job dispatch not fully implemented yet");
        conn.send(gson.toJson(response));
    }

    private void handleResultSubmission(WebSocket conn, JsonObject json) {
        // Handle result
    }

    private void sendError(WebSocket conn, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("command", "ERROR");
        response.addProperty("message", message);
        conn.send(gson.toJson(response));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("WebSocket error", ex);
    }

    @Override
    public void onStart() {
        logger.info("WebSocket Server started on port: {}", getPort());
    }
}
