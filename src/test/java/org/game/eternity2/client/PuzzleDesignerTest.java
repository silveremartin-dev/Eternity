package org.game.eternity2.client;

import org.game.eternity2.server.PuzzleDesigner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;
import javafx.application.Platform;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class PuzzleDesignerTest {

    @Start
    public void start(Stage stage) {
        // Just to initialize JavaFX toolkit
    }

    @Test
    public void testPuzzleDesignerInstantiation() {
        Platform.runLater(() -> {
            try {
                PuzzleDesigner designer = new PuzzleDesigner();
                assertNotNull(designer);
                assertEquals("Eternity II - Puzzle Designer", designer.getTitle());
            } catch (Exception e) {
                fail("Should not throw exception: " + e.getMessage());
            }
        });
    }
}
