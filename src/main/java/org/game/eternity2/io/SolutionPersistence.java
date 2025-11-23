package org.game.eternity2.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.EternityTileInterface;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading puzzle solutions in CSV format.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class SolutionPersistence {
    private static final Logger logger = LogManager.getLogger(SolutionPersistence.class);
    private static final String SOLUTIONS_DIR = "data/solutions";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static Path saveSolution(EternityBoardInterface board, int score) {
        try {
            Path solutionsPath = Paths.get(SOLUTIONS_DIR);
            if (!Files.exists(solutionsPath)) {
                Files.createDirectories(solutionsPath);
            }

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = String.format("solution_%s_score%d.csv", timestamp, score);
            Path filePath = solutionsPath.resolve(filename);

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.write("row,col,tileId,rotation\\n");

                int sizeX = board.getXBoardSize();
                int sizeY = board.getYBoardSize();

                for (int row = 0; row < sizeY; row++) {
                    for (int col = 0; col < sizeX; col++) {
                        EternityTileInterface tile = board.getTileAt(row, col);
                        if (tile != null) {
                            writer.write(String.format("%d,%d,%d,0\\n",
                                    row, col, tile.getBackValue()));
                        }
                    }
                }
            }

            logger.info("Solution saved to: {}", filePath);
            return filePath;

        } catch (IOException e) {
            logger.error("Failed to save solution", e);
            return null;
        }
    }

    public static List<TilePlacement> loadSolution(Path filePath) {
        List<TilePlacement> placements = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    int row = Integer.parseInt(parts[0].trim());
                    int col = Integer.parseInt(parts[1].trim());
                    int tileId = Integer.parseInt(parts[2].trim());
                    int rotation = Integer.parseInt(parts[3].trim());

                    placements.add(new TilePlacement(row, col, tileId, rotation));
                }
            }

            logger.info("Loaded {} tile placements from {}", placements.size(), filePath);

        } catch (IOException | NumberFormatException e) {
            logger.error("Failed to load solution from {}", filePath, e);
        }

        return placements;
    }

    public static List<Path> listSolutions() {
        List<Path> solutions = new ArrayList<>();
        Path solutionsPath = Paths.get(SOLUTIONS_DIR);

        if (!Files.exists(solutionsPath)) {
            return solutions;
        }

        try {
            Files.list(solutionsPath)
                    .filter(path -> path.toString().endsWith(".csv"))
                    .forEach(solutions::add);
        } catch (IOException e) {
            logger.error("Failed to list solutions", e);
        }

        return solutions;
    }

    public static class TilePlacement {
        private final int row;
        private final int col;
        private final int tileId;
        private final int rotation;

        public TilePlacement(int row, int col, int tileId, int rotation) {
            this.row = row;
            this.col = col;
            this.tileId = tileId;
            this.rotation = rotation;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getTileId() {
            return tileId;
        }

        public int getRotation() {
            return rotation;
        }
    }
}
