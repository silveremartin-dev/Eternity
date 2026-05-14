package org.game.eternity2.io;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Loads and saves puzzles exclusively in JSON format.
 * 
 * @author Silvere Martin-Michiellot
 * @author Antigravity
 * @since 1.0
 */
public class PuzzleLoaderWriter {

    private static final Logger LOGGER = LogManager.getLogger(PuzzleLoaderWriter.class);

    /**
     * Load pieces from a JSON file.
     * 
     * @param path Path to puzzle file
     * @return Array of piece primitives
     */
    public static long[] loadPieces(Path path) throws IOException {
        List<Long> pieces = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray jsonPieces = root.getAsJsonArray("pieces");

            for (JsonElement el : jsonPieces) {
                JsonObject p = el.getAsJsonObject();
                int id = p.get("id").getAsInt();
                int top = p.get("top").getAsInt();
                int right = p.get("right").getAsInt();
                int bottom = p.get("bottom").getAsInt();
                int left = p.get("left").getAsInt();

                pieces.add(PiecePrimitive.create(id, top, right, bottom, left));
            }
        }
        LOGGER.info("Loaded {} pieces from JSON {}", pieces.size(), path);
        return pieces.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * Load pieces from classpath resource (JSON only).
     */
    public static long[] loadPiecesFromResource(String resourcePath) throws IOException {
        try (InputStream is = PuzzleLoaderWriter.class.getResourceAsStream(resourcePath)) {
            if (is == null)
                throw new FileNotFoundException("Resource not found: " + resourcePath);

            try (Reader reader = new InputStreamReader(is)) {
                List<Long> pieces = new ArrayList<>();
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray jsonPieces = root.getAsJsonArray("pieces");

                for (JsonElement el : jsonPieces) {
                    JsonObject p = el.getAsJsonObject();
                    int id = p.get("id").getAsInt();
                    int top = p.get("top").getAsInt();
                    int right = p.get("right").getAsInt();
                    int bottom = p.get("bottom").getAsInt();
                    int left = p.get("left").getAsInt();

                    pieces.add(PiecePrimitive.create(id, top, right, bottom, left));
                }
                LOGGER.info("Loaded {} pieces from JSON resource {}", pieces.size(), resourcePath);
                return pieces.stream().mapToLong(Long::longValue).toArray();
            }
        }
    }

    /**
     * Generate standard Eternity II pieces by loading the official 16x16 puzzle JSON.
     */
    public static long[] generateEternity2Pieces() {
        try {
            return loadPiecesFromResource("/puzzles/puzzle_16x16_eternity2.json");
        } catch (Exception e) {
            LOGGER.error("Failed to load Eternity II pieces", e);
            return new long[0];
        }
    }

    /**
     * Save board solution to JSON.
     */
    public static void saveSolution(Path path, BoardPrimitive board) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            JsonObject root = new JsonObject();
            root.addProperty("timestamp", java.time.LocalDateTime.now().toString());
            root.addProperty("boardSizeX", board.getWidth());
            root.addProperty("boardSizeY", board.getHeight());
            root.addProperty("score", board.computeScore());

            JsonArray tiles = new JsonArray();
            for (int y = 0; y < board.getHeight(); y++) {
                for (int x = 0; x < board.getWidth(); x++) {
                    long piece = board.getPiece(x, y);
                    if (piece != 0) {
                        JsonObject t = new JsonObject();
                        t.addProperty("x", x);
                        t.addProperty("y", y);
                        t.addProperty("id", PiecePrimitive.getId(piece));
                        t.addProperty("rotation", PiecePrimitive.getRotation(piece));
                        tiles.add(t);
                    }
                }
            }
            root.add("tiles", tiles);

            Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            gson.toJson(root, writer);
        }
        LOGGER.info("Saved solution to JSON {}", path);
    }

    /**
     * Load board solution from JSON.
     */
    public static BoardPrimitive loadSolution(Path path, long[] library) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            
            // Add robustness: check for multiple possible keys for dimensions
            int width = 0;
            if (root.has("boardSizeX")) width = root.get("boardSizeX").getAsInt();
            else if (root.has("width")) width = root.get("width").getAsInt();
            else if (root.has("sizeX")) width = root.get("sizeX").getAsInt();
            
            int height = 0;
            if (root.has("boardSizeY")) height = root.get("boardSizeY").getAsInt();
            else if (root.has("height")) height = root.get("height").getAsInt();
            else if (root.has("sizeY")) height = root.get("sizeY").getAsInt();
            
            JsonArray tiles = root.has("tiles") ? root.getAsJsonArray("tiles") : new JsonArray();

            BoardPrimitive board = new BoardPrimitive(width, height);
            
            // If library is null or no tiles, we only want the board dimensions/metadata
            if (library == null || tiles.isEmpty()) return board;

            Map<Integer, Long> pieceMap = new HashMap<>();
            for (long p : library) {
                pieceMap.put(PiecePrimitive.getId(p), p);
            }

            for (JsonElement el : tiles) {
                JsonObject t = el.getAsJsonObject();
                int x = t.has("x") ? t.get("x").getAsInt() : 0;
                int y = t.has("y") ? t.get("y").getAsInt() : 0;
                int id = t.has("id") ? t.get("id").getAsInt() : 0;
                int rotation = t.has("rotation") ? t.get("rotation").getAsInt() : 0;

                Long piece = pieceMap.get(id);
                if (piece != null) {
                    long rotatedPiece = piece;
                    for (int i = 0; i < rotation; i++) {
                        rotatedPiece = PiecePrimitive.rotateCW(rotatedPiece);
                    }
                    board.placePiece(x, y, rotatedPiece);
                }
            }
            return board;
        }
    }

    /**
     * Save pieces to JSON file.
     */
    public static void savePieces(Path path, long[] pieces) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            JsonObject root = new JsonObject();
            JsonArray jsonPieces = new JsonArray();

            for (long piece : pieces) {
                JsonObject p = new JsonObject();
                p.addProperty("id", PiecePrimitive.getId(piece));
                p.addProperty("top", PiecePrimitive.getTop(piece));
                p.addProperty("right", PiecePrimitive.getRight(piece));
                p.addProperty("bottom", PiecePrimitive.getBottom(piece));
                p.addProperty("left", PiecePrimitive.getLeft(piece));

                int borderCount = 0;
                if (PiecePrimitive.getTop(piece) == 0) borderCount++;
                if (PiecePrimitive.getRight(piece) == 0) borderCount++;
                if (PiecePrimitive.getBottom(piece) == 0) borderCount++;
                if (PiecePrimitive.getLeft(piece) == 0) borderCount++;

                String type = "inner";
                if (borderCount == 1) type = "edge";
                else if (borderCount == 2) type = "corner";
                p.addProperty("type", type);

                jsonPieces.add(p);
            }
            root.add("pieces", jsonPieces);

            Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            gson.toJson(root, writer);
        }
        LOGGER.info("Saved {} pieces to JSON {}", pieces.length, path);
    }
}
