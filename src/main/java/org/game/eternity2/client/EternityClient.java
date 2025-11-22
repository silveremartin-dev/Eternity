package org.game.eternity2.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.server.EternityPacket;
import org.game.eternity2.server.EternityUser;
import org.game.eternity2.server.Job;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Core client logic for connecting to the Eternity server.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class EternityClient {
    private static final Logger logger = LogManager.getLogger(EternityClient.class);
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_PORT = 12345;

    private ClientUI ui;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isConnected;
    private EternityUser user;
    private ClientStatistics statistics;
    private JobExecutor executor;

    public EternityClient() {
        this.user = new EternityUser("User_" + System.currentTimeMillis() % 1000, "password");
        this.statistics = new ClientStatistics();
        this.executor = new JobExecutor(statistics);
    }

    public void setUi(ClientUI ui) {
        this.ui = ui;
    }

    public void connect() {
        new Thread(() -> {
            try {
                if (ui != null)
                    ui.log("Connecting to " + DEFAULT_SERVER_IP + ":" + DEFAULT_PORT + "...");
                socket = new Socket(DEFAULT_SERVER_IP, DEFAULT_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                isConnected = true;
                if (ui != null) {
                    ui.setConnected(true);
                    ui.log("Connected to server.");
                }

                // Send login packet
                sendPacket(new EternityPacket(user, EternityPacket.Command.LOGIN, null));
                // Request job
                sendPacket(new EternityPacket(user, EternityPacket.Command.JOB_REQUEST_NEW, null));

                // Listen for packets
                while (isConnected) {
                    try {
                        Object obj = in.readObject();
                        if (obj instanceof EternityPacket) {
                            processPacket((EternityPacket) obj);
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
        }).start();
    }

    public void disconnect() {
        isConnected = false;
        try {
            if (socket != null)
                socket.close();
        } catch (IOException ignored) {
        }
        if (ui != null) {
            ui.setConnected(false);
            ui.log("Disconnected.");
        }
    }

    private void processPacket(EternityPacket packet) {
        switch (packet.getCommand()) {
            case JOB_DISPATCH:
                // Old job format (just a board)
                if (packet.getPayload() instanceof EternityBoard16x16) {
                    EternityBoard16x16 board = (EternityBoard16x16) packet.getPayload();
                    if (ui != null)
                        ui.log("Received old job format: Board with score " + board.computeScore());
                    // Just send it back for now
                    sendPacket(new EternityPacket(user, EternityPacket.Command.RESULT_SUBMISSION, board));
                }
                break;

            case JOB_DISPATCH_NEW:
                // New job format with JobExecutor
                if (packet.getPayload() instanceof Job) {
                    Job job = (Job) packet.getPayload();
                    if (ui != null) {
                        ui.log("Received job: " + job.getJobId() + " (" + job.getPositionsToFill().size()
                                + " positions)");
                        ui.setJobStatus("Processing job...");
                    }

                    // Process job in background thread
                    new Thread(() -> {
                        try {
                            EternityBoardInterface result = executor.executeJob(job);

                            if (result != null) {
                                if (ui != null)
                                    ui.log("Job completed! Score: " + result.computeScore());
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
                    }).start();
                }
                break;

            case MESSAGE:
                if (ui != null)
                    ui.log("Server: " + packet.getPayload());
                break;

            default:
                if (ui != null)
                    ui.log("Unknown command: " + packet.getCommand());
        }
    }

    public void sendPacket(EternityPacket packet) {
        if (!isConnected)
            return;
        try {
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
package org.game.eternity2.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.server.EternityPacket;
import org.game.eternity2.server.EternityUser;
import org.game.eternity2.server.Job;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Core client logic for connecting to the Eternity server.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class EternityClient {
    private static final Logger logger = LogManager.getLogger(EternityClient.class);
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_PORT = 12345;

    private ClientUI ui;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isConnected;
    private EternityUser user;
    private ClientStatistics statistics;
    private JobExecutor executor;

    public EternityClient() {
        this.user = new EternityUser("User_" + System.currentTimeMillis() % 1000, "password");
        this.statistics = new ClientStatistics();
        this.executor = new JobExecutor(statistics);
    }

    public void setUi(ClientUI ui) {
        this.ui = ui;
    }

    public void connect() {
        new Thread(() -> {
            try {
                if (ui != null)
                    ui.log("Connecting to " + DEFAULT_SERVER_IP + ":" + DEFAULT_PORT + "...");
                socket = new Socket(DEFAULT_SERVER_IP, DEFAULT_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                isConnected = true;
                if (ui != null) {
                    ui.setConnected(true);
                    ui.log("Connected to server.");
                }

                // Send login packet
                sendPacket(new EternityPacket(user, EternityPacket.Command.LOGIN, null));
                // Request job
                sendPacket(new EternityPacket(user, EternityPacket.Command.JOB_REQUEST_NEW, null));

                // Listen for packets
                while (isConnected) {
                    try {
                        Object obj = in.readObject();
                        if (obj instanceof EternityPacket) {
                            processPacket((EternityPacket) obj);
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
        }).start();
    }

    public void disconnect() {
        isConnected = false;
        try {
            if (socket != null)
                socket.close();
        } catch (IOException ignored) {
        }
        if (ui != null) {
            ui.setConnected(false);
            ui.log("Disconnected.");
        }
    }

    private void processPacket(EternityPacket packet) {
        switch (packet.getCommand()) {
            case JOB_DISPATCH:
                // Old job format (just a board)
                if (packet.getPayload() instanceof EternityBoard16x16) {
                    EternityBoard16x16 board = (EternityBoard16x16) packet.getPayload();
                    if (ui != null)
                        ui.log("Received old job format: Board with score " + board.computeScore());
                    // Just send it back for now
                    sendPacket(new EternityPacket(user, EternityPacket.Command.RESULT_SUBMISSION, board));
                }
                break;

            case JOB_DISPATCH_NEW:
                // New job format with JobExecutor
                if (packet.getPayload() instanceof Job) {
                    Job job = (Job) packet.getPayload();
                    if (ui != null) {
                        ui.log("Received job: " + job.getJobId() + " (" + job.getPositionsToFill().size()
                                + " positions)");
                        ui.setJobStatus("Processing job...");
                    }

                    // Process job in background thread
                    new Thread(() -> {
                        try {
                            EternityBoardInterface result = executor.executeJob(job);

                            if (result != null) {
                                if (ui != null)
                                    ui.log("Job completed! Score: " + result.computeScore());
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
                    }).start();
                }
                break;

            case MESSAGE:
                if (ui != null)
                    ui.log("Server: " + packet.getPayload());
                break;

            default:
                if (ui != null)
                    ui.log("Unknown command: " + packet.getCommand());
        }
    }

    public void sendPacket(EternityPacket packet) {
        if (!isConnected)
            return;
        try {
            out.writeObject(packet);
            out.flush();
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