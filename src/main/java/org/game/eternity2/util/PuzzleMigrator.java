package org.game.eternity2.util;

import com.google.gson.*;
import org.game.eternity2.io.PuzzleLoaderWriter;
import org.game.eternity2.model.UnifiedPuzzle;
import org.game.eternity2.model.PiecePrimitive;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Utility to migrate old separate puzzle/hint files to the new UnifiedPuzzle format.
 */
public class PuzzleMigrator {

    public static void main(String[] args) {
        String resourcesPath = "src/main/resources/puzzles/";
        String outputPath = "src/main/resources/puzzles/unified/";
        
        try {
            Files.createDirectories(Paths.get(outputPath));
            
            // List of puzzles to migrate
            String[] sizes = {"4x4", "6x6", "12x6", "16x16"};
            
            for (String size : sizes) {
                migrate(resourcesPath, outputPath, size);
            }
            
            System.out.println("Migration complete!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void migrate(String base, String out, String size) throws IOException {
        Path puzzlePath = Paths.get(base, "puzzle_" + size + ".json");
        Path hintsPath = Paths.get(base, "hints_" + size + ".json");
        
        if (!Files.exists(puzzlePath)) return;
        
        System.out.println("Migrating " + size + "...");
        
        UnifiedPuzzle unified = new UnifiedPuzzle();
        String[] dims = size.split("x");
        unified.width = Integer.parseInt(dims[0]);
        unified.height = Integer.parseInt(dims[1]);
        
        // Load pieces
        try (Reader reader = Files.newBufferedReader(puzzlePath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray pieces = root.getAsJsonArray("pieces");
            for (JsonElement el : pieces) {
                JsonObject p = el.getAsJsonObject();
                unified.pieces.add(new UnifiedPuzzle.PieceData(
                    p.get("id").getAsInt(),
                    p.get("top").getAsInt(),
                    p.get("right").getAsInt(),
                    p.get("bottom").getAsInt(),
                    p.get("left").getAsInt()
                ));
            }
        }
        
        // Load hints
        if (Files.exists(hintsPath)) {
            try (Reader reader = Files.newBufferedReader(hintsPath)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has("hints")) {
                    JsonArray hints = root.getAsJsonArray("hints");
                    for (JsonElement el : hints) {
                        JsonObject h = el.getAsJsonObject();
                        UnifiedPuzzle.HintData hd = new UnifiedPuzzle.HintData();
                        hd.x = h.get("x").getAsInt();
                        hd.y = h.get("y").getAsInt();
                        hd.pieceId = h.get("id").getAsInt();
                        hd.rotation = h.has("rotation") ? h.get("rotation").getAsInt() : 0;
                        unified.hints.add(hd);
                    }
                }
            }
        }
        
        // Save unified
        PuzzleLoaderWriter.saveUnified(Paths.get(out, size + "_unified.json"), unified);
    }
}
