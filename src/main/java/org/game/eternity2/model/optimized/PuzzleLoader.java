package org.game.eternity2.model.optimized;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Loads puzzles from TheSil/edge_puzzle data format.
 * 
 * Format:
 * - pieces.txt: One piece per line: "id north east south west"
 * - Pattern 0 = border (grey edge)
 */
public class PuzzleLoader {

    private static final Logger LOGGER = LogManager.getLogger(PuzzleLoader.class);

    /**
     * Load pieces from a file.
     * 
     * @param path Path to pieces file
     * @return Array of piece primitives
     */
    public static long[] loadPieces(Path path) throws IOException {
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

        LOGGER.info("Loaded {} pieces from {}", pieces.size(), path);
        return pieces.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * Load pieces from classpath resource.
     */
    public static long[] loadPiecesFromResource(String resourcePath) throws IOException {
        try (InputStream is = PuzzleLoader.class.getResourceAsStream(resourcePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

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
     * Generate standard Eternity II pieces.
     * 4 corners, 56 edges, 196 inner = 256 total.
     */
    public static long[] generateEternity2Pieces() {
        List<Long> pieces = new ArrayList<>();
        int id = 0;

        // This is a placeholder - real E2 data should be loaded from file
        Random rand = new Random(42); // Fixed seed for reproducibility
        int numPatterns = 22; // E2 has 22 patterns + border (0)

        // 4 Corners (2 border edges each)
        for (int i = 0; i < 4; i++) {
            int[] edges = new int[4];
            int borderCount = 0;
            for (int j = 0; j < 4; j++) {
                if (borderCount < 2 && rand.nextBoolean()) {
                    edges[j] = 0;
                    borderCount++;
                } else {
                    edges[j] = rand.nextInt(numPatterns) + 1;
                }
            }
            // Ensure exactly 2 borders
            while (borderCount < 2) {
                int idx = rand.nextInt(4);
                if (edges[idx] != 0) {
                    edges[idx] = 0;
                    borderCount++;
                }
            }
            pieces.add(PiecePrimitive.create(id++, edges[0], edges[1], edges[2], edges[3]));
        }

        // 56 Edges (1 border edge each)
        for (int i = 0; i < 56; i++) {
            int borderSide = i % 4;
            int[] edges = new int[4];
            for (int j = 0; j < 4; j++) {
                edges[j] = (j == borderSide) ? 0 : rand.nextInt(numPatterns) + 1;
            }
            pieces.add(PiecePrimitive.create(id++, edges[0], edges[1], edges[2], edges[3]));
        }

        // 196 Inner (no border edges)
        for (int i = 0; i < 196; i++) {
            pieces.add(PiecePrimitive.create(id++,
                    rand.nextInt(numPatterns) + 1,
                    rand.nextInt(numPatterns) + 1,
                    rand.nextInt(numPatterns) + 1,
                    rand.nextInt(numPatterns) + 1));
        }

        LOGGER.info("Generated {} placeholder Eternity II pieces", pieces.size());
        return pieces.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * Save pieces to file.
     */
    public static void savePieces(Path path, long[] pieces) throws IOException {
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
