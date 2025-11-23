package org.game.eternity2.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.elements.AbstractEternityBoard;
import org.game.eternity2.elements.EternityTileInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern JSON-based solution persistence.
 * Provides human-readable storage format.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class JsonSolutionPersistence {
    private static final Logger logger = LogManager.getLogger(JsonSolutionPersistence.class);
    private static final String SOLUTIONS_DIR = "data/solutions";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Save a solution to JSON format.
     *
     * @param board The board to save
     * @param score The board's score
     * @return Path to saved file
     */
    public static Path saveSolution(AbstractEternityBoard board, int score) {
        try {
            Path solutionsPath = Paths.get(SOLUTIONS_DIR);
            if (!Files.exists(solutionsPath)) {
                Files.createDirectories(solutionsPath);
            }

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = String.format("solution_%s_score%d.json", timestamp, score);
            Path filePath = solutionsPath.resolve(filename);

            SolutionData data = new SolutionData();
            data.timestamp = LocalDateTime.now().toString();
            data.score = score;
            data.boardSizeX = board.getXBoardSize();
            data.boardSizeY = board.getYBoardSize();
            data.tiles = new ArrayList<>();

            for (int row = 0; row < data.boardSizeY; row++) {
                for (int col = 0; col < data.boardSizeX; col++) {
                    EternityTileInterface tile = board.getTileAt(row, col);
                    if (tile != null) {
                        TileData tileData = new TileData();
                        tileData.row = row;
                        tileData.col = col;
                        tileData.tileId = tile.getBackValue();
                        tileData.rotation = tile.getRotation();
                        data.tiles.add(tileData);
                    }
                }
            }

            String json = gson.toJson(data);
            Files.writeString(filePath, json);

            logger.info("Solution saved to JSON: {}", filePath);
            return filePath;

        } catch (IOException e) {
            logger.error("Failed to save solution to JSON", e);
            return null;
        }
    }

    /**
     * Load a solution from JSON format.
     *
     * @param filePath Path to JSON file
     * @return Solution data
     */
    public static SolutionData loadSolution(Path filePath) {
        try {
            String json = Files.readString(filePath);
            SolutionData data = gson.fromJson(json, SolutionData.class);
            logger.info("Loaded solution from JSON: {} tiles", data.tiles.size());
            return data;
        } catch (IOException e) {
            logger.error("Failed to load solution from JSON", e);
            return null;
        }
    }

    /**
     * List all JSON solutions.
     *
     * @return List of solution files
     */
    public static List<Path> listSolutions() {
        List<Path> solutions = new ArrayList<>();
        Path solutionsPath = Paths.get(SOLUTIONS_DIR);

        if (!Files.exists(solutionsPath)) {
            return solutions;
        }

        try {
            Files.list(solutionsPath)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(solutions::add);
        } catch (IOException e) {
            logger.error("Failed to list JSON solutions", e);
        }

        return solutions;
    }

    /**
     * Solution data structure for JSON serialization.
     */
    public static class SolutionData {
        public String timestamp;
        public int score;
        public int boardSizeX;
        public int boardSizeY;
        public List<TileData> tiles;
    }

    /**
     * Tile data for JSON serialization.
     */
    public static class TileData {
        public int row;
        public int col;
        public int tileId;
        public int rotation;
    }
}
