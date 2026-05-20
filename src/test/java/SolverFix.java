import org.game.eternity2.model.*;
import org.game.eternity2.io.*;
import org.game.eternity2.solver.*;
import java.nio.file.Path;

public class SolverFix {
    public static void main(String[] args) throws Exception {
        solveAndSave("12x6_empty_board", "puzzle_12x6_solved.json");
    }

    private static void solveAndSave(String inFile, String outFile) throws Exception {
        System.out.println("Loading " + inFile + "...");
        UnifiedPuzzle up = PuzzleLoaderWriter.loadSmart(inFile);
        
        long[] pieces = PuzzleLoaderWriter.toPrimitives(up);
        BoardPrimitive board = new BoardPrimitive(up.width, up.height);
        
        // Setup initial board with hints
        for (UnifiedPuzzle.HintData hd : up.hints) {
            long p = 0;
            for (long piece : pieces) {
                if (PiecePrimitive.getId(piece) == hd.pieceId) {
                    p = piece;
                    break;
                }
            }
            if (p != 0) {
                for(int i=0; i<hd.rotation; i++) p = PiecePrimitive.rotateCW(p);
                board.placePiece(hd.x, hd.y, p);
            }
        }
        
        System.out.println("Solving " + inFile + "...");
        BasicEternitySolver solver = new BasicEternitySolver(pieces);
        BoardPrimitive solution = solver.computeTessellation(board);
        
        if (solution != null && solution.isComplete()) {
            System.out.println("Solved! Saving to " + outFile + "...");
            Path outPath = Path.of("src/main/resources/puzzles/" + outFile);
            PuzzleLoaderWriter.saveUnifiedSolution(outPath, up, solution, up.patterns);
            System.out.println("Saved successfully.");
        } else {
            System.out.println("Failed to solve " + inFile);
        }
    }
}
