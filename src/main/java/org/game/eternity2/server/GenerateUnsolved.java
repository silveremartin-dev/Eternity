package org.game.eternity2.server;

import org.game.eternity2.io.JsonSolutionPersistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Utility to generate empty puzzle files in JSON format.
 */
public class GenerateUnsolved {
    private static final Logger logger = LogManager.getLogger(GenerateUnsolved.class);

    public static void main(String[] args) {
        logger.info("Generating unsolved puzzle files...");

        generate(4, 4, "unsolved_4x4.json");
        generate(6, 6, "unsolved_6x6.json");
        generate(12, 6, "unsolved_12x6.json");
        generate(16, 16, "unsolved_16x16.json");

        logger.info("Generation complete.");
        System.exit(0);
    }

    private static void generate(int width, int height, String filename) {
        try {
            Path path = Paths.get("src/main/resources/xml/data", filename);
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            JsonSolutionPersistence.SolutionData data = new JsonSolutionPersistence.SolutionData();
            data.timestamp = LocalDateTime.now().toString();
            data.score = 0;
            data.boardSizeX = width;
            data.boardSizeY = height;
            data.tiles = new ArrayList<>();

            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(data);

            Files.writeString(path, json);
            logger.info("Generated: {}", path.toAbsolutePath());

        } catch (Exception e) {
            logger.error("Failed to generate puzzle file: " + filename, e);
        }
    }
}
