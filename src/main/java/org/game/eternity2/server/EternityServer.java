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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.Hint;
import org.game.eternity2.model.PiecePrimitive;
import org.game.eternity2.server.strategy.BorderFirstStrategy;
import org.game.eternity2.server.redis.ConstraintCache;
import org.game.eternity2.server.redis.RedisConnectionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.game.eternity2.server.grpc.EternityServiceImpl;

/**
 * The server to be used to dispatch packets between clients.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.2
  * @author Antigravity
  * @since 1.0
 */
public class EternityServer {
    private static final Logger logger = LogManager.getLogger(EternityServer.class);
    public static final int DEFAULT_PORT = 12345;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private int port;
    private ServerUI gui;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private ExecutorService clientExecutor;
    private List<ClientHandler> clients;

    private BoardPrimitive masterBoard;
    private JobManager jobManager;
    private JsonUserDatabase userDatabase;
    private ServerStatistics statistics;
    private EternityWebSocketServer webSocketServer;
    private Server grpcServer;

    private RedisConnectionManager redisManager;
    private ConstraintCache constraintCache;
    private java.util.Map<String, Double> clientThroughput = new java.util.concurrent.ConcurrentHashMap<>();
    private long[] activePieces;

    public EternityServer(int port) {
        this.port = port;
        this.clients = new java.util.ArrayList<>();
        // masterBoard will be initialized in initializeGame
        this.jobManager = new JobManager();
        this.userDatabase = new JsonUserDatabase();
        this.statistics = new ServerStatistics();

        // Start throughput reporter
        Thread throughputThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    double totalPps = clientThroughput.values().stream().mapToDouble(d -> d).sum();
                    statistics.setPiecesPerSecond((int) totalPps);
                    if (gui != null) {
                        gui.updateThroughput(totalPps);
                    }
                    // Decay old throughputs? Or let clients update them.
                    // Actually, let's clear it if no update for 5 seconds.
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        throughputThread.setDaemon(true);
        throughputThread.start();

        // Start WebSocket server on port + 1
        this.webSocketServer = new EternityWebSocketServer(port + 1, this);
        this.webSocketServer.start();

        // Initialize Redis components with environment-based configuration
        try {
            String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
            int redisPort = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
            this.redisManager = new RedisConnectionManager(redisHost, redisPort);
            this.constraintCache = new ConstraintCache(redisManager);
            logger.info("Redis connection established to {}:{} and Constraint Cache initialized", redisHost, redisPort);
        } catch (Exception e) {
            logger.warn("Redis not available. Constraint Cache will be disabled.", e);
        }
    }
 
    public boolean isRunning() {
        return isRunning;
    }

    public void initializeGame(int sizeX, int sizeY, String strategyName, List<Hint> hints, long[] pieces) {
        this.activePieces = pieces;
        // Create board using primitives
        this.masterBoard = new BoardPrimitive(sizeX, sizeY);

        if (hints == null) {
            hints = new ArrayList<>();
        }

        // Apply hints onto masterBoard
        if (pieces != null) {
            for (Hint hint : hints) {
                long matchedPiece = 0;
                for (long p : pieces) {
                    if (PiecePrimitive.getId(p) == hint.tileId()) {
                        matchedPiece = p;
                        break;
                    }
                }
                if (matchedPiece != 0) {
                    long rotatedPiece = matchedPiece;
                    for (int r = 0; r < hint.rotation(); r++) {
                        rotatedPiece = PiecePrimitive.rotateCW(rotatedPiece);
                    }
                    masterBoard.placePiece(hint.col(), hint.row(), rotatedPiece);
                }
            }
        }

        // Select strategy
        WorkStrategy strategy;
        if ("Scanline".equalsIgnoreCase(strategyName)) {
            strategy = new BorderFirstStrategy(); // Placeholder
        } else {
            strategy = new BorderFirstStrategy();
        }

        // Initialize the job manager
        jobManager.initializeJobs(masterBoard, hints, strategy);

        // Try to load checkpoint
        try {
            java.nio.file.Path checkpointPath = java.nio.file.Path.of("data/master_board_checkpoint.json");
            if (java.nio.file.Files.exists(checkpointPath)) {
                BoardPrimitive checkpoint = org.game.eternity2.io.PuzzleLoaderWriter.loadSolution(checkpointPath, pieces);
                if (checkpoint.getWidth() == sizeX && checkpoint.getHeight() == sizeY) {
                    this.masterBoard = checkpoint;
                    statistics.updateBestBoard(masterBoard);
                    logger.info("Loaded master board checkpoint with score {}", masterBoard.computeScore());
                    if (gui != null) {
                        gui.log(timestamp() + " Resumed from checkpoint (Score: " + masterBoard.computeScore() + ")");
                        gui.updateBestBoard(masterBoard);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not load checkpoint: {}", e.getMessage());
        }

        if (gui != null) {
            gui.updateBestBoard(masterBoard);
        }

        logger.info("Game initialized: {}x{} board, Strategy: {}, Jobs: {}",
                sizeX, sizeY, strategyName, jobManager.getStatistics().getTotalJobs());
    }

    public void startServer() {
        if (isRunning) {
            return;
        }
        isRunning = true;

        // Virtual thread per task executor
        this.clientExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.isRunning = true;
        
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                if (gui != null) {
                    gui.log(timestamp() + " Server started on port " + port + " (Fixed Thread Pool)");
                    gui.setServerStatus(true);
                }
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        if (gui != null) {
                            gui.log(timestamp() + " New connection from " + clientSocket.getInetAddress());
                        }
                        ClientHandler handler = new ClientHandler(clientSocket);

                        // Thread-safe concurrent update
                        synchronized (clients) {
                            clients.add(handler);
                            if (gui != null) {
                                gui.updateClientCount(clients.size());
                            }
                        }

                        // Submit to Virtual Thread pool
                        clientExecutor.submit(handler);
                    } catch (IOException e) {
                        if (isRunning && gui != null) {
                            gui.log(timestamp() + " Error accepting connection: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                if (gui != null) {
                    gui.log(timestamp() + " Could not listen on port " + port);
                }
                stopServer();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Start gRPC Server
        try {
            int grpcPort = port + 2;
            grpcServer = ServerBuilder.forPort(grpcPort)
                    .addService(new EternityServiceImpl(this))
                    .build()
                    .start();
            logger.info("gRPC Server started on port " + grpcPort);
            if (gui != null) {
                gui.log(timestamp() + " gRPC Server started on port " + grpcPort);
            }
        } catch (IOException e) {
            logger.error("Failed to start gRPC server", e);
        }
    }

    public void stopServer() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (clientExecutor != null) clientExecutor.shutdown();
            // Close Virtual Thread executor (graceful shutdown)
            
            if (webSocketServer != null) {
                try {
                    webSocketServer.stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (grpcServer != null) {
                grpcServer.shutdown();
                try {
                    if (!grpcServer.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        grpcServer.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    grpcServer.shutdownNow();
                }
            }

            // Thread-safe client iteration and cleanup
            List<ClientHandler> clientsCopy;
            synchronized (clients) {
                clientsCopy = new ArrayList<>(clients);
                clients.clear();
            }

            for (ClientHandler client : clientsCopy) {
                client.close();
            }

            if (gui != null) {
                gui.updateClientCount(0);
                gui.log(timestamp() + " Server stopped.");
                gui.setServerStatus(false);
            }
        } catch (IOException e) {
            if (gui != null) {
                gui.log(timestamp() + " Error stopping server: " + e.getMessage());
            }
        }
    }

    public void setGui(ServerUI gui) {
        this.gui = gui;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public ServerStatistics getStatistics() {
        return statistics;
    }

    public BoardPrimitive getMasterBoard() {
        BoardPrimitive board = this.masterBoard;
        if (board == null) return null;
        synchronized (board) {
            return board;
        }
    }

    private String timestamp() {
        return "[" + LocalTime.now().format(TIME_FORMATTER) + "]";
    }

    /** Handles communication with a single client. */
    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private boolean connected;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.connected = true;
            statistics.incrementActiveClients();
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                while (connected && isRunning) {
                    try {
                        Object obj = in.readObject();
                        if (obj instanceof EternityPacket) {
                            processPacket((EternityPacket) obj);
                        }
                    } catch (ClassNotFoundException e) {
                        if (gui != null) {
                            gui.log(timestamp() + " Invalid packet from " + socket.getInetAddress());
                        }
                    }
                }
            } catch (IOException e) {
                if (gui != null) {
                    gui.log(timestamp() + " Client disconnected: " + socket.getInetAddress());
                }
            } finally {
                close();
            }
        }

        private void processPacket(EternityPacket packet) throws IOException {
            statistics.incrementPacketsReceived();

            String packetIdShort = packet.getPacketId().substring(0, 8);

            if (gui != null) {
                gui.log(String.format("%s [%s] %s from %s",
                        timestamp(), packetIdShort, packet.getCommand(), packet.getUser().getLogin()));
            }

            switch (packet.getCommand()) {
                case LOGIN:
                    if (!userDatabase.authenticateUser(packet.getUser().getLogin(),
                            packet.getUser().getPassword())) {
                        if (userDatabase.registerUser(packet.getUser().getLogin(),
                                packet.getUser().getPassword())) {
                            if (gui != null) {
                                gui.log(timestamp() + " User auto-registered: " + packet.getUser().getLogin());
                            }
                            username = packet.getUser().getLogin();
                            sendPacket(new EternityPacket(packet.getUser(),
                                    EternityPacket.Command.MESSAGE,
                                    "Welcome (auto-registered) " + packet.getUser().getLogin()));
                        } else {
                            sendPacket(new EternityPacket(packet.getUser(),
                                    EternityPacket.Command.MESSAGE, "Authentication failed"));
                        }
                    } else {
                        if (gui != null) {
                            gui.log(timestamp() + " User logged in: " + packet.getUser().getLogin());
                        }
                        username = packet.getUser().getLogin();
                        sendPacket(new EternityPacket(packet.getUser(),
                                EternityPacket.Command.MESSAGE, "Welcome " + packet.getUser().getLogin()));
                    }
                    if (username != null && activePieces != null) {
                        sendPacket(new EternityPacket(packet.getUser(), EternityPacket.Command.PUZZLE_DEFINITION, activePieces));
                    }
                    break;

                case REGISTER:
                    boolean registered = userDatabase.registerUser(
                            packet.getUser().getLogin(), packet.getUser().getPassword());
                    String regMsg = registered ? "Registration successful" : "User already exists";
                    sendPacket(new EternityPacket(packet.getUser(),
                            EternityPacket.Command.MESSAGE, regMsg));
                    break;

                case CHANGE_PASSWORD:
                    if (packet.getPayload() instanceof String) {
                        String newPassword = (String) packet.getPayload();
                        boolean changed = userDatabase.changePassword(
                                packet.getUser().getLogin(),
                                packet.getUser().getPassword(),
                                newPassword);
                        String msg = changed ? "Password changed" : "Password change failed";
                        sendPacket(new EternityPacket(packet.getUser(),
                                EternityPacket.Command.MESSAGE, msg));
                    }
                    break;

                case DELETE_ACCOUNT:
                    boolean deleted = userDatabase.deleteUser(
                            packet.getUser().getLogin(), packet.getUser().getPassword());
                    String delMsg = deleted ? "Account deleted" : "Account deletion failed";
                    sendPacket(new EternityPacket(packet.getUser(),
                            EternityPacket.Command.MESSAGE, delMsg));
                    break;

                case RESULT_SUBMISSION:
                case JOB_REQUEST:
                case JOB_REQUEST_NEW:
                    if (packet.getCommand() == EternityPacket.Command.RESULT_SUBMISSION) {
                        BoardPrimitive resultBoard = null;
                        if (packet.getPayload() instanceof BoardPrimitive) {
                            resultBoard = (BoardPrimitive) packet.getPayload();
                        }
                        if (!"INTERMEDIATE".equals(packet.getStatus())) {
                            jobManager.markClientJobCompleted(packet.getUser().getLogin(), resultBoard);
                        }
                    }

                    if (packet.getPayload() instanceof BoardPrimitive) {
                        BoardPrimitive resultBoard = (BoardPrimitive) packet.getPayload();
                        synchronized (masterBoard) {
                            if (resultBoard.computeScore() > masterBoard.computeScore()) {
                                masterBoard = resultBoard;
                                statistics.updateBestBoard(masterBoard);
                                statistics.incrementPiecesSolved(resultBoard.getPlacedCount());
                                if (gui != null) {
                                    String formatted = org.game.eternity2.util.BoardRenderer.formatScore(masterBoard.computeScore(), masterBoard.getWidth(), masterBoard.getHeight());
                                    gui.log(timestamp() + " [NEW BEST] " + formatted + " from " + packet.getUser().getLogin());
                                    gui.updateBestBoard(masterBoard);
                                }
                                // Save checkpoint
                                try {
                                    java.io.File dataDir = new java.io.File("data");
                                    if (!dataDir.exists()) dataDir.mkdirs();
                                    org.game.eternity2.io.PuzzleLoaderWriter.saveSolution(
                                        java.nio.file.Path.of("data/master_board_checkpoint.json"), masterBoard);
                                } catch (IOException e) {
                                    logger.error("Failed to save checkpoint", e);
                                }
                            }
                        }
                    }
                    
                    // ONLY dispatch new job if it's a request, NOT on result submission
                    // because the client will send a separate JOB_REQUEST_NEW after submission
                    if (packet.getCommand() != EternityPacket.Command.RESULT_SUBMISSION) {
                        Job job = jobManager.getNextJob(packet.getUser().getLogin());
                        if (job != null) {
                            sendPacket(new EternityPacket(packet.getUser(), EternityPacket.Command.JOB_DISPATCH_NEW, job));
                        } else {
                            sendPacket(new EternityPacket(packet.getUser(), EternityPacket.Command.MESSAGE, "No jobs available"));
                        }
                    }
                    break;

                case SERVER_STATUS_REQUEST:
                    ServerStatus status = new ServerStatus(
                            true,
                            statistics.getActiveClients(),
                            statistics.getTotalClients(),
                            statistics.getTotalComputeTimeMs(),
                            statistics.getUptimeMs(),
                            statistics.getAverageComputeTimePerClient(),
                            statistics.getPiecesPerSecond(),
                            masterBoard != null ? masterBoard.computeScore() : 0,
                            statistics.getPacketsSent(),
                            statistics.getPacketsReceived());
                    sendPacket(new EternityPacket(packet.getUser(),
                            EternityPacket.Command.SERVER_STATUS_RESPONSE, status));
                    break;

                case MESSAGE:
                    if (gui != null) {
                        gui.log(timestamp() + " Message from " + packet.getUser().getLogin()
                                + ": " + packet.getPayload());
                    }
                    break;

                case STATISTICS_UPDATE:
                    if (packet.getPayload() instanceof Double) {
                        double clientPps = (Double) packet.getPayload();
                        clientThroughput.put(packet.getUser().getLogin(), clientPps);
                    }
                    break;

                default:
                    if (gui != null) {
                        gui.log(timestamp() + " Unknown command: " + packet.getCommand());
                    }
            }
        }

        public void sendPacket(EternityPacket packet) throws IOException {
            statistics.incrementPacketsSent();
            out.writeObject(packet);
            out.flush();
        }

        public void close() {
            connected = false;
            statistics.decrementActiveClients();
            if (username != null) {
                jobManager.markClientJobFailed(username);
            }
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ignored) {
            }
            clients.remove(this);
            if (gui != null) {
                gui.updateClientCount(clients.size());
            }
        }
    }

    public static void main(String[] args) {
        ServerApp.main(args);
    }
}
