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
 * Loads and saves puzzles from JSON or Text format.
 */
public class PuzzleLoaderWriter {

    private static final Logger LOGGER = LogManager.getLogger(PuzzleLoaderWriter.class);
    private static final Gson GSON = new Gson();

    /**
     * Load pieces from a file (JSON or text).
     * 
     * @param path Path to puzzle file
     * @return Array of piece primitives
     */
    public static long[] loadPieces(Path path) throws IOException {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.endsWith(".json")) {
            return loadPiecesFromJson(path);
        } else {
            return loadPiecesFromText(path);
        }
    }

    private static long[] loadPiecesFromJson(Path path) throws IOException {
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

    private static long[] loadPiecesFromText(Path path) throws IOException {
        List<Long> pieces = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                String[] parts = line.split("\\s+");
                if (parts.length < 5)
                    continue;

                int id = Integer.parseInt(parts[0]);
                int north = Integer.parseInt(parts[1]);
                int east = Integer.parseInt(parts[2]);
                int south = Integer.parseInt(parts[3]);
                int west = Integer.parseInt(parts[4]);

                pieces.add(PiecePrimitive.create(id, north, east, south, west));
            }
        }

        LOGGER.info("Loaded {} pieces from text {}", pieces.size(), path);
        return pieces.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * Load pieces from classpath resource.
     */
    public static long[] loadPiecesFromResource(String resourcePath) throws IOException {
        if (resourcePath.endsWith(".json")) {
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
        } else {
            try (InputStream is = PuzzleLoaderWriter.class.getResourceAsStream(resourcePath)) {
                if (is == null)
                    throw new FileNotFoundException("Resource not found: " + resourcePath);

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    List<Long> pieces = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#"))
                            continue;

                        String[] parts = line.split("\\s+");
                        if (parts.length < 5)
                            continue;

                        int id = Integer.parseInt(parts[0]);
                        int north = Integer.parseInt(parts[1]);
                        int east = Integer.parseInt(parts[2]);
                        int south = Integer.parseInt(parts[3]);
                        int west = Integer.parseInt(parts[4]);

                        pieces.add(PiecePrimitive.create(id, north, east, south, west));
                    }

                    LOGGER.info("Loaded {} pieces from resource {}", pieces.size(), resourcePath);
                    return pieces.stream().mapToLong(Long::longValue).toArray();
                }
            }
        }
    }

    /**
     * Load board state from a file.
     * Format: width height, then grid of piece IDs (-1 for empty).
     */
    public static BoardPrimitive loadBoard(Path path, long[] pieces) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine().trim();
            String[] dims = header.split("\\s+");
            int width = Integer.parseInt(dims[0]);
            int height = Integer.parseInt(dims[1]);

            BoardPrimitive board = new BoardPrimitive(width, height);
            Map<Integer, Long> pieceById = new HashMap<>();
            for (long piece : pieces) {
                pieceById.put(PiecePrimitive.getId(piece), piece);
            }

            for (int y = 0; y < height; y++) {
                String line = reader.readLine().trim();
                String[] ids = line.split("\\s+");
                for (int x = 0; x < width && x < ids.length; x++) {
                    int id = Integer.parseInt(ids[x]);
                    if (id >= 0 && pieceById.containsKey(id)) {
                        board.placePiece(x, y, pieceById.get(id));
                    }
                }
            }

            LOGGER.info("Loaded board {}x{} with {} pieces from {}",
                    width, height, board.getPlacedCount(), path);
            return board;
        }
    }

    /**
     * Generate standard Eternity II pieces by loading the official 16x16 puzzle
     * file.
     */
    public static long[] generateEternity2Pieces() {
        try {
            // Load from the official JSON puzzle file
            try {
                return loadPiecesFromResource("/puzzles/puzzle_16x16_eternity2.json");
            } catch (Exception e) {
                // Try file system (dev mode fallback)
                Path p = Paths.get("src/main/resources/puzzles/puzzle_16x16_eternity2.json");
                if (Files.exists(p)) {
                    return loadPiecesFromJson(p);
                }
                throw e;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load Eternity II pieces, falling back to empty set", e);
            return new long[0];
        }
    }

    /**
     * Save pieces to file.
     */
    public static void savePieces(Path path, long[] pieces) throws IOException {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.endsWith(".json")) {
            savePiecesToJson(path, pieces);
        } else {
            savePiecesToText(path, pieces);
        }
    }

    private static void savePiecesToJson(Path path, long[] pieces) throws IOException {
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

                // Infer type for completeness
                int borderCount = 0;
                if (PiecePrimitive.getTop(piece) == 0)
                    borderCount++;
                if (PiecePrimitive.getRight(piece) == 0)
                    borderCount++;
                if (PiecePrimitive.getBottom(piece) == 0)
                    borderCount++;
                if (PiecePrimitive.getLeft(piece) == 0)
                    borderCount++;

                String type = "inner";
                if (borderCount == 1)
                    type = "edge";
                else if (borderCount == 2)
                    type = "corner";
                p.addProperty("type", type);

                jsonPieces.add(p);
            }
            root.add("pieces", jsonPieces);

            Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            gson.toJson(root, writer);
        }
        LOGGER.info("Saved {} pieces to JSON {}", pieces.length, path);
    }

    private static void savePiecesToText(Path path, long[] pieces) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("# Eternity II Pieces - Format: id north east south west\n");
            for (long piece : pieces) {
                writer.write(String.format("%d %d %d %d %d\n",
                        PiecePrimitive.getId(piece),
                        PiecePrimitive.getTop(piece),
                        PiecePrimitive.getRight(piece),
                        PiecePrimitive.getBottom(piece),
                        PiecePrimitive.getLeft(piece)));
            }
        }
        LOGGER.info("Saved {} pieces to {}", pieces.length, path);
    }
}
