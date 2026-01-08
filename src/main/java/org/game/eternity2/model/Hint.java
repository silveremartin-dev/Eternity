package org.game.eternity2.model;

import java.io.Serializable;

/**
 * Represents a pre-placed piece on the board (a hint).
 */
public record Hint(int row, int col, int tileId, int rotation) implements Serializable {
    private static final long serialVersionUID = 1L;
}
