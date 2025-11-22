package org.game.eternity2.server;

/**
 * Minimal interface that the server uses to communicate with a UI.
 * Implementations can be Swing based (ServerGUI) or JavaFX based (ServerApp).
 */
public interface ServerUI {
    /** Append a message to the UI log. */
    void log(String msg);

    /** Update the server running status indicator. */
    void setServerStatus(boolean running);

    /** Update the displayed client count. */
    void updateClientCount(int count);
}
