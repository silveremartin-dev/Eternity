package org.game.eternity2.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import org.game.eternity2.model.UnifiedPuzzle;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Unified I/O for Eternity II puzzles.
 */
public class PuzzleLoaderWriter {

    private static final Logger LOGGER = LogManager.getLogger(PuzzleLoaderWriter.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static UnifiedPuzzle loadUnified(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            UnifiedPuzzle up = GSON.fromJson(reader, UnifiedPuzzle.class);
            if (up != null && up.patterns <= 0) {
                up.getPatterns();
            }
            return up;
        }
    }

    private static InputStream getInputStream(String resourcePath) throws IOException {
        InputStream is = PuzzleLoaderWriter.class.getResourceAsStream(resourcePath);
        if (is != null) {
            return is;
        }
        
        // Try without leading slash
        String pathNoSlash = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        is = PuzzleLoaderWriter.class.getClassLoader().getResourceAsStream(pathNoSlash);
        if (is != null) {
            return is;
        }
        is = PuzzleLoaderWriter.class.getResourceAsStream(pathNoSlash);
        if (is != null) {
            return is;
        }
        
        // Try direct file path
        File file = new File(resourcePath);
        if (file.exists() && file.isFile()) {
            return new FileInputStream(file);
        }
        
        // Try file in src/main/resources
        File srcFile = new File("src/main/resources" + (resourcePath.startsWith("/") ? "" : "/") + resourcePath);
        if (srcFile.exists() && srcFile.isFile()) {
            return new FileInputStream(srcFile);
        }
        
        // Try file in target/classes
        File targetFile = new File("target/classes" + (resourcePath.startsWith("/") ? "" : "/") + resourcePath);
        if (targetFile.exists() && targetFile.isFile()) {
            return new FileInputStream(targetFile);
        }
        
        throw new FileNotFoundException("Resource or file not found: " + resourcePath);
    }

    public static UnifiedPuzzle loadUnifiedFromResource(String resourcePath) throws IOException {
        try (InputStream is = getInputStream(resourcePath)) {
            try (Reader reader = new InputStreamReader(is)) {
                UnifiedPuzzle up = GSON.fromJson(reader, UnifiedPuzzle.class);
                if (up != null && up.patterns <= 0) {
                    up.getPatterns();
                }
                return up;
            }
        }
    }

    public static void saveUnified(Path path, UnifiedPuzzle puzzle) throws IOException {
        if (puzzle != null) {
            puzzle.getPatterns();
        }
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(puzzle, writer);
        }
    }

    public static long[] loadPiecesFromResource(String resourcePath) throws IOException {
        try (InputStream is = getInputStream(resourcePath)) {
            try (Reader reader = new InputStreamReader(is)) {
                List<Long> pieces = new ArrayList<>();
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray jsonPieces = root.getAsJsonArray("pieces");
                for (JsonElement el : jsonPieces) {
                    JsonObject p = el.getAsJsonObject();
                    pieces.add(PiecePrimitive.create(
                        p.get("id").getAsInt(),
                        p.get("top").getAsInt(),
                        p.get("right").getAsInt(),
                        p.get("bottom").getAsInt(),
                        p.get("left").getAsInt()
                    ));
                }
                return pieces.stream().mapToLong(Long::longValue).toArray();
            }
        }
    }

    public static long[] generateEternity2Pieces() {
        String[] paths = {
            "/puzzles/puzzle_16x16_empty_board.json"
        };
        for (String path : paths) {
            try {
                return loadPiecesFromResource(path);
            } catch (Exception e) {
                // Try next
            }
        }
        LOGGER.error("Failed to load Eternity II pieces from any fallback path");
        return new long[0];
    }

    public static UnifiedPuzzle loadFromSeparateFiles(String puzzleName) throws IOException {
        UnifiedPuzzle up = new UnifiedPuzzle();
        String base = "/puzzles/";
        
        // Load pieces
        try (InputStream is = getInputStream(base + "puzzle_" + puzzleName + ".json")) {
            JsonObject root = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
            JsonArray pieces = root.getAsJsonArray("pieces");
            for (JsonElement el : pieces) {
                JsonObject p = el.getAsJsonObject();
                up.pieces.add(new UnifiedPuzzle.PieceData(
                    p.get("id").getAsInt(), p.get("top").getAsInt(), p.get("right").getAsInt(),
                    p.get("bottom").getAsInt(), p.get("left").getAsInt()
                ));
            }
            int count = up.pieces.size();
            up.width = (int)Math.sqrt(count);
            up.height = up.width;
            if (count == 72) { up.width = 12; up.height = 6; }
        }

        // Load hints
        try {
            try (InputStream is = getInputStream(base + "hints_" + puzzleName + ".json")) {
                JsonObject root = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
                JsonArray hintsArray = null;
                if (root.has("hints")) {
                    hintsArray = root.getAsJsonArray("hints");
                } else if (root.has("tiles")) {
                    hintsArray = root.getAsJsonArray("tiles");
                }
                if (hintsArray != null) {
                    for (JsonElement el : hintsArray) {
                        JsonObject h = el.getAsJsonObject();
                        UnifiedPuzzle.HintData hd = new UnifiedPuzzle.HintData();
                        hd.x = h.get("x").getAsInt();
                        hd.y = h.get("y").getAsInt();
                        hd.pieceId = h.get("id").getAsInt();
                        hd.rotation = h.has("rotation") ? h.get("rotation").getAsInt() : 0;
                        up.hints.add(hd);
                    }
                }
            }
        } catch (Exception e) {
            // Hints are optional
        }

        // Load solution
        try {
            try (InputStream is = getInputStream(base + "solved_" + puzzleName + ".json")) {
                JsonObject root = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
                JsonArray placementsArray = null;
                if (root.has("placements")) {
                    placementsArray = root.getAsJsonArray("placements");
                } else if (root.has("tiles")) {
                    placementsArray = root.getAsJsonArray("tiles");
                }
                if (placementsArray != null) {
                    up.currentBoard = new UnifiedPuzzle.BoardData();
                    for (JsonElement el : placementsArray) {
                        JsonObject p = el.getAsJsonObject();
                        UnifiedPuzzle.PlacementData pd = new UnifiedPuzzle.PlacementData();
                        pd.x = p.get("x").getAsInt();
                        pd.y = p.get("y").getAsInt();
                        pd.pieceId = p.get("id").getAsInt();
                        pd.rotation = p.has("rotation") ? p.get("rotation").getAsInt() : 0;
                        up.currentBoard.placements.add(pd);
                    }
                }
            }
        } catch (Exception e) {
            // Solution is optional
        }
        
        // Calculate patterns
        up.patterns = up.getPatterns();
        return up;
    }

    public static UnifiedPuzzle loadSmart(String puzzleName) throws IOException {
        if (puzzleName == null) {
            throw new IllegalArgumentException("Puzzle name cannot be null");
        }
        
        // Trim any whitespace
        puzzleName = puzzleName.trim();
        
        String baseName = puzzleName;
        if (puzzleName.startsWith("unified_")) {
            baseName = puzzleName.substring(8);
        }
        
        List<String> pathsToTry = new ArrayList<>();
        pathsToTry.add("/puzzles/" + puzzleName + ".json");
        pathsToTry.add("/puzzles/unified_" + baseName + ".json");
        pathsToTry.add("/puzzles/unified_" + puzzleName + ".json");
        pathsToTry.add("/puzzles/" + baseName + ".json");
        
        Exception lastException = null;
        for (String path : pathsToTry) {
            try {
                UnifiedPuzzle up = loadUnifiedFromResource(path);
                if (up != null) {
                    if (up.patterns <= 0) {
                        up.patterns = up.getPatterns();
                    }
                    return up;
                }
            } catch (Exception e) {
                lastException = e;
            }
        }
        
        // If resource loading of unified fails, try loading from separate files
        LOGGER.warn("Failed to load unified puzzle from resources for " + puzzleName + ", trying separate files", lastException);
        try {
            return loadFromSeparateFiles(baseName);
        } catch (Exception e) {
            try {
                return loadFromSeparateFiles(puzzleName);
            } catch (Exception ex) {
                throw new IOException("Failed to load puzzle: " + puzzleName + " (tried unified resources and separate files)", ex);
            }
        }
    }


    public static void consolidateResources(Path resourceDir) throws IOException {
        File dir = resourceDir.toFile();
        if (!dir.exists() || !dir.isDirectory()) return;

        Set<String> names = new HashSet<>();
        File[] files = dir.listFiles((d, n) -> n.startsWith("puzzle_") && n.endsWith(".json"));
        if (files != null) for (File f : files) names.add(f.getName().replace("puzzle_", "").replace(".json", ""));
        
        File[] legacy = dir.listFiles((d, n) -> n.endsWith(".puzzle"));
        if (legacy != null) for (File f : legacy) names.add(f.getName().replace(".puzzle", ""));

        for (String name : names) {
            Path unified = resourceDir.resolve("unified_" + name + ".json");
            try {
                UnifiedPuzzle up = loadFromSeparateFiles(name);
                saveUnified(unified, up);
                
                // Final cleanup
                new File(dir, "puzzle_" + name + ".json").delete();
                new File(dir, "hints_" + name + ".json").delete();
                new File(dir, "solved_" + name + ".json").delete();
                new File(dir, name + ".puzzle").delete();
                new File(dir, name + ".int").delete();
                new File(dir, name + ".solved").delete();
                System.out.println("Cleaned up: " + name);
            } catch (Exception e) {
                System.err.println("Failed: " + name);
            }
        }
    }

    public static long[] loadPieces(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            List<Long> pieces = new ArrayList<>();
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray jsonPieces = root.getAsJsonArray("pieces");
            for (JsonElement el : jsonPieces) {
                JsonObject p = el.getAsJsonObject();
                pieces.add(PiecePrimitive.create(
                    p.get("id").getAsInt(), p.get("top").getAsInt(), p.get("right").getAsInt(),
                    p.get("bottom").getAsInt(), p.get("left").getAsInt()
                ));
            }
            return pieces.stream().mapToLong(Long::longValue).toArray();
        }
    }

    public static void saveUnifiedSolution(Path path, UnifiedPuzzle original, BoardPrimitive board, int patterns) throws IOException {
        if (original == null) {
            throw new IllegalArgumentException("Original puzzle cannot be null");
        }
        UnifiedPuzzle up = new UnifiedPuzzle();
        up.width = board.getWidth();
        up.height = board.getHeight();
        up.patterns = patterns;
        
        // Copy pieces and hints from original
        up.pieces = new ArrayList<>(original.pieces);
        up.hints = new ArrayList<>(original.hints);
        
        // Output the placements!
        up.currentBoard = new UnifiedPuzzle.BoardData();
        for (int y = 0; y < up.height; y++) {
            for (int x = 0; x < up.width; x++) {
                long p = board.getPiece(x, y);
                if (p != 0) {
                    UnifiedPuzzle.PlacementData pd = new UnifiedPuzzle.PlacementData();
                    pd.x = x;
                    pd.y = y;
                    pd.pieceId = PiecePrimitive.getId(p);
                    pd.rotation = PiecePrimitive.getRotation(p);
                    up.currentBoard.placements.add(pd);
                }
            }
        }
        saveUnified(path, up);
    }

    public static void saveSolution(Path path, BoardPrimitive board) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("boardSizeX", board.getWidth());
        root.addProperty("boardSizeY", board.getHeight());
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
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(root, writer);
        }
    }

    public static void savePieces(Path path, long[] pieces) throws IOException {
        JsonObject root = new JsonObject();
        JsonArray jsonPieces = new JsonArray();
        for (long piece : pieces) {
            JsonObject p = new JsonObject();
            p.addProperty("id", PiecePrimitive.getId(piece));
            p.addProperty("top", PiecePrimitive.getTop(piece));
            p.addProperty("right", PiecePrimitive.getRight(piece));
            p.addProperty("bottom", PiecePrimitive.getBottom(piece));
            p.addProperty("left", PiecePrimitive.getLeft(piece));
            jsonPieces.add(p);
        }
        root.add("pieces", jsonPieces);
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(root, writer);
        }
    }

    public static BoardPrimitive loadSolution(Path path, long[] library) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            int width = root.has("boardSizeX") ? root.get("boardSizeX").getAsInt() : root.get("width").getAsInt();
            int height = root.has("boardSizeY") ? root.get("boardSizeY").getAsInt() : root.get("height").getAsInt();
            BoardPrimitive board = new BoardPrimitive(width, height);
            if (root.has("tiles")) {
                Map<Integer, Long> pieceMap = new HashMap<>();
                if (library != null) for (long p : library) pieceMap.put(PiecePrimitive.getId(p), p);
                for (JsonElement el : root.getAsJsonArray("tiles")) {
                    JsonObject t = el.getAsJsonObject();
                    int x = t.get("x").getAsInt();
                    int y = t.get("y").getAsInt();
                    int id = t.get("id").getAsInt();
                    int rot = t.get("rotation").getAsInt();
                    Long p = pieceMap.get(id);
                    if (p != null) {
                        long rp = p;
                        for (int i = 0; i < rot; i++) rp = PiecePrimitive.rotateCW(rp);
                        board.placePiece(x, y, rp);
                    }
                }
            }
            return board;
        }
    }

    public static long[] toPrimitives(UnifiedPuzzle puzzle) {
        long[] result = new long[puzzle.pieces.size()];
        for (int i = 0; i < puzzle.pieces.size(); i++) {
            UnifiedPuzzle.PieceData p = puzzle.pieces.get(i);
            result[i] = PiecePrimitive.create(p.id, p.top, p.right, p.bottom, p.left);
        }
        return result;
    }
}
