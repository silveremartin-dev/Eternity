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
package org.game.eternity2.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.server.EternityPacket;
import org.game.eternity2.server.EternityUser;
import org.game.eternity2.server.Job;
import org.game.eternity2.client.grpc.EternityGrpcClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Core client logic for connecting to the Eternity server.
 * Now using Virtual Threads for improved scalability.
 * Supports both socket and gRPC modes.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.2 (Virtual Threads + gRPC)
 * @author Antigravity
 * @since 1.0
 */
public class EternityClient {
    private static final Logger logger = LogManager.getLogger(EternityClient.class);
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_PORT = 12345;
    private static final int DEFAULT_GRPC_PORT = 12347;

    // Set to true to use gRPC instead of socket
    private static final boolean USE_GRPC = false;

    private ClientUI ui;

    // Socket-based fields
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // gRPC-based fields
    private EternityGrpcClient grpcClient;

    private boolean isConnected;
    private EternityUser user;
    private ClientStatistics statistics;
    private JobExecutor executor;
    private int lastTransmittedScore = -1;

    public EternityClient() {
        this.user = new EternityUser("User_" + System.currentTimeMillis() % 1000, "password");
        this.statistics = new ClientStatistics();
        // Load stats on startup
        this.statistics.load(new java.io.File("data/client_stats.properties"));
        this.executor = new JobExecutor(statistics);
    }

    public void setUi(ClientUI ui) {
        this.ui = ui;
    }

    public void connect() {
        if (USE_GRPC) {
            connectGrpc();
        } else {
            connectSocket();
        }
    }

    private void connectGrpc() {
        // Virtual Thread for gRPC client connection
        Thread.ofVirtual().start(() -> {
            try {
                if (ui != null)
                    ui.log("Connecting to gRPC server at " + DEFAULT_SERVER_IP + ":" + DEFAULT_GRPC_PORT + "...");

                grpcClient = new EternityGrpcClient(DEFAULT_SERVER_IP, DEFAULT_GRPC_PORT);
                isConnected = true;

                if (ui != null) {
                    ui.setConnected(true);
                    ui.log("Connected to gRPC server (Virtual Thread).");
                }

                // Send login request via gRPC with FlatBuffers (empty payload for now)
                byte[] loginResponse = grpcClient.login(new byte[0]);
                logger.info("gRPC login completed, response size: {} bytes", loginResponse.length);
                if (ui != null)
                    ui.log("gRPC login successful, FlatBuffers mode active");

            } catch (Exception e) {
                if (ui != null)
                    ui.log("gRPC connection failed: " + e.getMessage());
                logger.error("gRPC connection error", e);
                disconnect();
            }
        });
    }

    private void connectSocket() {
        // Virtual Thread for client connection (lightweight, non-blocking)
        Thread.ofVirtual().start(() -> {
            try {
                if (ui != null)
                    ui.log("Connecting to " + DEFAULT_SERVER_IP + ":" + DEFAULT_PORT + "...");
                socket = new Socket(DEFAULT_SERVER_IP, DEFAULT_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                java.io.ObjectInputFilter filter = java.io.ObjectInputFilter.Config.createFilter(
                    "org.game.eternity2.**;java.lang.*;java.util.*;[J;[I;[Ljava.lang.String;;!*"
                );
                in.setObjectInputFilter(filter);
                isConnected = true;
                if (ui != null) {
                    ui.setConnected(true);
                    ui.log("Connected to server (Virtual Thread).");
                }

                // Send login packet
                sendPacket(new EternityPacket(user, EternityPacket.Command.LOGIN, null));
                // Request job
                sendPacket(new EternityPacket(user, EternityPacket.Command.JOB_REQUEST_NEW, null));

                // Start statistics reporter
                startStatisticsReporter();

                // Listen for packets
                while (isConnected) {
                    try {
                        Object obj = in.readObject();
                        if (obj instanceof EternityPacket) {
                            EternityPacket packet = (EternityPacket) obj;
                            if (ui != null) {
                                String packetIdShort = packet.getPacketId().substring(0, 8);
                                logger.debug("Received packet [{}] {}", packetIdShort, packet.getCommand());
                            }
                            boolean isStat = (packet.getCommand() == EternityPacket.Command.STATISTICS_UPDATE);
                            statistics.incrementPacketsReceived(isStat);
                            processPacket(packet);
                        }
                    } catch (ClassNotFoundException e) {
                        if (ui != null)
                            ui.log("Invalid packet received.");
                    }
                }
            } catch (IOException e) {
                if (ui != null)
                    ui.log("Connection failed: " + e.getMessage());
                disconnect();
            }
        });
    }

    private void startStatisticsReporter() {
        Thread.ofVirtual().start(() -> {
            while (isConnected) {
                try {
                    Thread.sleep(2000); // Every 2 seconds
                    if (isConnected) {
                        double pps = statistics.getPiecesPerSecond();
                        sendPacket(new EternityPacket(user, EternityPacket.Command.STATISTICS_UPDATE, pps));
                        
                        BoardPrimitive bestB = statistics.getBestBoard();
                        if (bestB != null && bestB.computeScore() > lastTransmittedScore) {
                            lastTransmittedScore = bestB.computeScore();
                            EternityPacket intermediatePacket = new EternityPacket(user, EternityPacket.Command.RESULT_SUBMISSION, bestB);
                            intermediatePacket.setStatus("INTERMEDIATE");
                            sendPacket(intermediatePacket);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.warn("Failed to send statistics update: {}", e.getMessage());
                }
            }
        });
    }

    public void disconnect() {
        isConnected = false;
        if (executor != null) {
            executor.cancel();
        }
        // Save stats on disconnect
        statistics.save(new java.io.File("data/client_stats.properties"));

        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (grpcClient != null) {
                grpcClient.shutdown();
            }
        } catch (Exception e) {
            logger.warn("Error during disconnect: {}", e.getMessage());
        }
        if (ui != null) {
            ui.setConnected(false);
            ui.log("Disconnected.");
        }
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    public void setUseGPU(boolean useGPU) {
        if (executor != null) {
            executor.setUseGPU(useGPU);
        }
    }

    private void processPacket(EternityPacket packet) {
        switch (packet.getCommand()) {
            case JOB_DISPATCH:
                // Old job format (now using primitives)
                if (packet.getPayload() instanceof BoardPrimitive) {
                    BoardPrimitive board = (BoardPrimitive) packet.getPayload();
                    if (ui != null)
                        ui.log("Received job: Board with score " + board.computeScore());
                    // Just send it back for now
                    sendPacket(new EternityPacket(user, EternityPacket.Command.RESULT_SUBMISSION, board));
                }
                break;

            case JOB_DISPATCH_NEW:
                // New job format with JobExecutor - process in Virtual Thread
                if (packet.getPayload() instanceof Job) {
                    Job job = (Job) packet.getPayload();
                    statistics.resetSession();
                    lastTransmittedScore = -1;
                    if (job.getInitialBoard() != null) {
                        statistics.setBoardDimensions(job.getInitialBoard().getWidth(),
                                job.getInitialBoard().getHeight());
                    }
                    if (ui != null) {
                        ui.log("Received job: " + job.getJobId() + " (" + job.getPositionsToFill().size()
                                + " positions)");
                        ui.setJobStatus("Processing job...");
                    }

                    // Process job in Virtual Thread (lightweight background processing)
                    Thread.ofVirtual().start(() -> {
                        try {
                            BoardPrimitive result = executor.executeJob(job);

                            if (result != null) {
                                if (ui != null) {
                                    ui.log("Job completed! Score: " + result.computeScore());
                                    ui.updateBestBoard(result);
                                }
                                sendPacket(new EternityPacket(user, EternityPacket.Command.RESULT_SUBMISSION, result));
                            } else {
                                if (ui != null)
                                    ui.log("Job completed: no solution found");
                                sendPacket(new EternityPacket(user, EternityPacket.Command.RESULT_SUBMISSION,
                                        job.getInitialBoard()));
                            }

                            if (ui != null)
                                ui.setJobStatus("Waiting for job...");

                            // Request next job
                            sendPacket(new EternityPacket(user, EternityPacket.Command.JOB_REQUEST_NEW, null));
                        } catch (Exception e) {
                            if (ui != null)
                                ui.log("Error processing job: " + e.getMessage());
                            logger.error("Job execution error", e);
                        }
                    });
                }
                break;

            case BEST_BOARD_UPDATE:
                if (packet.getPayload() instanceof BoardPrimitive) {
                    BoardPrimitive board = (BoardPrimitive) packet.getPayload();
                    statistics.updateBestBoard(board);
                    if (ui != null) {
                        ui.updateBestBoard(board);
                    }
                }
                break;

            case MESSAGE:
                String msg = (String) packet.getPayload();
                if (ui != null)
                    ui.log("Server: " + msg);

                if ("No jobs available".equals(msg)) {
                    if (ui != null)
                        ui.setJobStatus("Idle (No jobs available)");

                    // Retry after 5 seconds in Virtual Thread
                    Thread.ofVirtual().start(() -> {
                        try {
                            Thread.sleep(5000);
                            if (isConnected) {
                                sendPacket(new EternityPacket(user, EternityPacket.Command.JOB_REQUEST_NEW, null));
                            }
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
                break;

            case PUZZLE_DEFINITION:
                if (packet.getPayload() instanceof long[]) {
                    long[] pieces = (long[]) packet.getPayload();
                    executor.setAllPieces(pieces);
                    if (ui != null)
                        ui.log("Received puzzle definition: " + pieces.length + " pieces");
                }
                break;

            default:
                if (ui != null)
                    ui.log("Unknown command: " + packet.getCommand());
        }
    }

    public void sendPacket(EternityPacket packet) {
        if (!isConnected || out == null)
            return;
        try {
            boolean isStat = (packet.getCommand() == EternityPacket.Command.STATISTICS_UPDATE);
            statistics.incrementPacketsSent(isStat);
            synchronized (out) {
                out.writeObject(packet);
                out.flush();
            }
            if (ui != null) {
                String packetIdShort = packet.getPacketId().substring(0, 8);
                logger.debug("Sent packet [{}] {}", packetIdShort, packet.getCommand());
            }
        } catch (IOException e) {
            if (ui != null)
                ui.log("Error sending packet: " + e.getMessage());
            logger.error("Packet send error", e);
        }
    }

    public EternityUser getUser() {
        return user;
    }

    public ClientStatistics getStatistics() {
        return statistics;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public static void main(String[] args) {
        ClientApp.main(args);
    }
}