package org.game.eternity2.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists board solutions in JSON format.
 * Updated to use BoardPrimitive.
 */
public class JsonSolutionPersistence {

    private static final String SOLUTIONS_DIR = "solutions";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class SolutionData {
        public String timestamp;
        public int score;
        public int boardSizeX;
        public int boardSizeY;
        public List<TileData> tiles;
    }

    public static class TileData {
        public int x;
        public int y;
        public int id;
        public int rotation;
    }

    public static Path saveSolution(BoardPrimitive board) throws IOException {
        Path dir = Paths.get(SOLUTIONS_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        SolutionData data = new SolutionData();
        data.timestamp = LocalDateTime.now().toString();
        data.score = board.computeScore();
        data.boardSizeX = board.getWidth();
        data.boardSizeY = board.getHeight();
        data.tiles = new ArrayList<>();

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                long piece = board.getPiece(x, y);
                if (piece != 0) {
                    TileData td = new TileData();
                    td.x = x;
                    td.y = y;
                    td.id = PiecePrimitive.getId(piece);
                    td.rotation = PiecePrimitive.getRotation(piece);
                    data.tiles.add(td);
                }
            }
        }

        String filename = String.format("solution_%dx%d_%d_%d.json",
                board.getWidth(), board.getHeight(), data.score, System.currentTimeMillis());
        Path file = dir.resolve(filename);
        Files.writeString(file, GSON.toJson(data));
        return file;
    }
}
