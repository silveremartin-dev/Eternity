package org.game.eternity2.server;

import org.game.eternity2.elements.AbstractEternityBoard;
import org.game.eternity2.elements.size12x6.EternityBoard12x6;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.elements.size4x4.EternityBoard4x4;
import org.game.eternity2.elements.size6x6.EternityBoard6x6;
import org.game.eternity2.io.JsonSolutionPersistence;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenerateUnsolved {
    private static final Logger logger = LogManager.getLogger(GenerateUnsolved.class);

    public static void main(String[] args) {
        logger.info("Generating unsolved puzzle files...");

        generate(new EternityBoard4x4(), "unsolved_4x4.puzzle");
        generate(new EternityBoard6x6(), "unsolved_6x6.puzzle");
        generate(new EternityBoard12x6(), "unsolved_12x6.puzzle");
        generate(new EternityBoard16x16(), "unsolved_16x16.puzzle");

        logger.info("Generation complete.");
        System.exit(0);
    }

    private static void generate(AbstractEternityBoard board, String filename) {
        try {
            // Ensure board is empty (it should be by default)
            // Save to xml/data directory
            Path path = Paths.get("src/main/resources/xml/data", filename);

            // JsonSolutionPersistence saves to "solutions" dir by default in saveSolution
            // We need to manually use the logic or modify saveSolution to accept a path.
            // saveSolution returns a Path, but it constructs it internally.
            // Let's look at JsonSolutionPersistence again.
            // It uses SOLUTIONS_DIR = "solutions".
            // I should probably duplicate the saving logic here to save to the specific
            // target directory.

            JsonSolutionPersistence.SolutionData data = new JsonSolutionPersistence.SolutionData();
            data.timestamp = java.time.LocalDateTime.now().toString();
            data.score = 0;
            data.boardSizeX = board.getXBoardSize();
            data.boardSizeY = board.getYBoardSize();
            data.tiles = new java.util.ArrayList<>();

            // Empty board -> empty tiles list

            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(data);

            java.nio.file.Files.writeString(path, json);
            logger.info("Generated: {}", path.toAbsolutePath());

        } catch (Exception e) {
            logger.error("Failed to generate puzzle file", e);
        }
    }
}
