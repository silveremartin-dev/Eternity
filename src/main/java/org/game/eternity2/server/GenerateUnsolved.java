/*
 * MIT License
 *
 * Copyright (c) 2026 Silvere Martin-Michiellot, Antigravity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
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
