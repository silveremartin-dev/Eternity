package org.game.eternity2.client;

/**
 * Interface for Client UI callbacks.
 * Allows EternityClient to interact with JavaFX UI (ClientApp).
 */
public interface ClientUI {
    void log(String message);

    void setConnected(boolean connected);

    void setJobStatus(String status);
}
