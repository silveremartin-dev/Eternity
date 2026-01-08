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
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
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
