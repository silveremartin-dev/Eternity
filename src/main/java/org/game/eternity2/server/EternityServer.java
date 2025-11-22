package org.game.eternity2.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.elements.Hint;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.server.strategy.BorderFirstStrategy;

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

/**
 * The server to be used to dispatch packets between clients.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
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
    private EternityBoard16x16 masterBoard;
    private JobManager jobManager;
    private UserDatabase userDatabase;
    private ServerStatistics statistics;

    public EternityServer(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        this.masterBoard = new EternityBoard16x16();
        this.jobManager = new JobManager();
        this.userDatabase = new UserDatabase();
        this.statistics = new ServerStatistics();

        // Initialize jobs
        initializeJobs();
    }

    private void initializeJobs() {
        // Create a simple test puzzle
        EternityBoard16x16 puzzle = new EternityBoard16x16();
        List<Hint> hints = new ArrayList<>();

        // Use BorderFirstStrategy
        WorkStrategy strategy = new BorderFirstStrategy();

        // Initialize the job manager
        jobManager.initializeJobs(puzzle, hints, strategy);

        logger.info("JobManager initialized with {} jobs", jobManager.getStatistics().getTotalJobs());
    }

    public void startServer() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        clientExecutor = Executors.newCachedThreadPool();
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                if (gui != null) {
                    gui.log(timestamp() + " Server started on port " + port);
                    gui.setServerStatus(true);
                }
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        if (gui != null) {
                            gui.log(timestamp() + " New connection from " + clientSocket.getInetAddress());
                        }
                        ClientHandler handler = new ClientHandler(clientSocket);
                        clients.add(handler);
                        if (gui != null) {
                            gui.updateClientCount(clients.size());
                        }
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
        }).start();
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
            if (clientExecutor != null) {
                clientExecutor.shutdownNow();
            }
            // Copy list to avoid ConcurrentModificationException
            List<ClientHandler> clientsCopy = new ArrayList<>(clients);
            for (ClientHandler client : clientsCopy) {
                client.close();
            }
            clients.clear();
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

    private String timestamp() {
        return "[" + LocalTime.now().format(TIME_FORMATTER) + "]";
    }

    /** Handles communication with a single client. */
    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private boolean connected;

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
            String clientId = socket.getInetAddress().toString();
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
                        sendPacket(new EternityPacket(packet.getUser(),
                                EternityPacket.Command.MESSAGE, "Welcome " + packet.getUser().getLogin()));
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

                case JOB_REQUEST:
                case JOB_REQUEST_NEW:
                    if (gui != null) {
                        gui.log(timestamp() + " Job requested by " + packet.getUser().getLogin());
                    }
                    Job job = jobManager.getNextJob(clientId);
                    if (job != null) {
                        sendPacket(new EternityPacket(packet.getUser(),
                                EternityPacket.Command.JOB_DISPATCH_NEW, job));
                    } else {
                        // No jobs available, send empty board for old clients
                        sendPacket(new EternityPacket(packet.getUser(),
                                EternityPacket.Command.JOB_DISPATCH, masterBoard.clone()));
                    }
                    break;

                case RESULT_SUBMISSION:
                    if (gui != null) {
                        gui.log(timestamp() + " Result received from " + packet.getUser().getLogin());
                    }
                    if (packet.getPayload() instanceof EternityBoard16x16) {
                        EternityBoard16x16 resultBoard = (EternityBoard16x16) packet.getPayload();
                        synchronized (masterBoard) {
                            if (resultBoard.computeScore() > masterBoard.computeScore()) {
                                masterBoard = resultBoard;
                                statistics.incrementPiecesSolved(resultBoard.numTiles());
                                if (gui != null) {
                                    gui.log(timestamp() + " New best score: " + masterBoard.computeScore());
                                }
                            }
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
                            masterBoard.computeScore(),
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

                default:
                    if (gui != null) {
                        gui.log(timestamp() + " Unknown command: " + packet.getCommand());
                    }
            }

            statistics.incrementPacketsSent();
        }

        public void sendPacket(EternityPacket packet) throws IOException {
            out.writeObject(packet);
            out.flush();
        }

        public void close() {
            connected = false;
            statistics.decrementActiveClients();
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
