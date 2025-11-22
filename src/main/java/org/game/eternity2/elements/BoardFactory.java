package org.game.eternity2.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.elements.size4x4.EternityBoard4x4;
import org.game.eternity2.elements.size6x6.EternityBoard6x6;
import org.game.eternity2.elements.size12x6.EternityBoard12x6;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;

/**
 * Factory for creating boards of various sizes dynamically.
 * Supports square and non-square boards.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class BoardFactory {
    private static final Logger logger = LogManager.getLogger(BoardFactory.class);

    /**
     * Create a board with specified dimensions.
     *
     * @param sizeX Width of the board
     * @param sizeY Height of the board
     * @return Board instance
     * @throws IllegalArgumentException if size not supported
     */
    public static EternityBoardInterface createBoard(int sizeX, int sizeY) {
        logger.info("Creating board: {}x{}", sizeX, sizeY);

        // Square boards
        if (sizeX == sizeY) {
            return switch (sizeX) {
                case 4 -> new EternityBoard4x4();
                case 6 -> new EternityBoard6x6();
                case 16 -> new EternityBoard16x16();
                default -> throw new IllegalArgumentException(
                        "Unsupported square board size: " + sizeX + "x" + sizeY);
            };
        }

        // Non-square boards
        if (sizeX == 12 && sizeY == 6) {
            return new EternityBoard12x6();
        }

        // Future: Generic board implementation
        throw new IllegalArgumentException(
                "Unsupported board size: " + sizeX + "x" + sizeY +
                        ". Supported: 4x4, 6x6, 12x6, 16x16");
    }

    /**
     * Create a square board.
     *
     * @param size Size of the board (width = height)
     * @return Board instance
     */
    public static EternityBoardInterface createSquareBoard(int size) {
        return createBoard(size, size);
    }

    /**
     * Get list of supported board sizes.
     *
     * @return Array of [sizeX, sizeY] pairs
     */
    public static int[][] getSupportedSizes() {
        return new int[][] {
                { 4, 4 },
                { 6, 6 },
                { 12, 6 },
                { 16, 16 }
        };
    }

    /**
     * Check if a board size is supported.
     *
     * @param sizeX Width
     * @param sizeY Height
     * @return true if supported
     */
    public static boolean isSupported(int sizeX, int sizeY) {
        for (int[] size : getSupportedSizes()) {
            if (size[0] == sizeX && size[1] == sizeY) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get board description.
     *
     * @param sizeX Width
     * @param sizeY Height
     * @return Human-readable description
     */
    public static String getBoardDescription(int sizeX, int sizeY) {
        int totalTiles = sizeX * sizeY;
        String shape = (sizeX == sizeY) ? "Square" : "Rectangular";
        return String.format("%s %dx%d (%d tiles)", shape, sizeX, sizeY, totalTiles);
    }
}
