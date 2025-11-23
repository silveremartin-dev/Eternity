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

public class EternityWebSocketServer extends WebSocketServer {

    @SuppressWarnings("unused")
    private final EternityServer gameServer;
    private final Gson gson = new Gson();
    private final Map<WebSocket, String> authenticatedUsers = new ConcurrentHashMap<>();

    public EternityWebSocketServer(int port, EternityServer gameServer) {
        super(new InetSocketAddress(port));
        this.gameServer = gameServer;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New WebSocket connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed WebSocket connection: " + conn.getRemoteSocketAddress());
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
                case "RESULT_SUBMISSION":
                    handleResultSubmission(conn, json);
                    break;
                default:
                    sendError(conn, "Unknown command: " + command);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(conn, "Invalid message format");
        }
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
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket Server started on port: " + getPort());
    }
}
