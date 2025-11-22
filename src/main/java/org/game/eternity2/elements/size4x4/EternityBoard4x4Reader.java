/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.game.eternity2.elements.size4x4;

import org.game.eternity2.io.AbstractEternityBoardReader;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;


/**
 * A way to read a (possibly partial) solution to the Eternity II box set from a file.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityBoard4x4Reader extends AbstractEternityBoardReader {

    public final static String DEFAULT_PATH_HINTS_4x4 = "xml/data/e2hints4x4.xml";

    public final static String DEFAULT_PATH_PIECES_4x4 = "xml/data/e2pieces4x4.xml";

    public final static String DEFAULT_PATH_SOLVED_4x4 = "xml/data/e2solvedboard4x4.xml";

    public final static EternityBoard4x4 hintEternityBoard4x4 = new EternityBoard4x4Reader(DEFAULT_PATH_HINTS_4x4).getBoard();

    public final static EternityBoard4x4 piecesEternityBoard4x4 = new EternityBoard4x4Reader(DEFAULT_PATH_PIECES_4x4).getBoard();

    public final static EternityBoard4x4 solvedEternityBoard4x4 = new EternityBoard4x4Reader(DEFAULT_PATH_SOLVED_4x4).getBoard();

    public EternityBoard4x4Reader(@NotNull String path) {
        super(path);
    }
    
    public final EternityBoard4x4 getBoard() {
        List<EternityXMLTile> eternityXMLTiles;
        EternityBoard4x4 eternityBoard;
        boolean[] checkedTiles;
        eternityXMLTiles = getEternityXMLGameBoard().getEternityXMLTiles();
        eternityBoard = new EternityBoard4x4();
        checkedTiles = new boolean[eternityXMLTiles.size()];
        IntStream.range(0, eternityXMLTiles.size()).forEach(i -> checkedTiles[i] = false);
        for (EternityXMLTile currentEternityXMLTile : eternityXMLTiles) {
            int number = currentEternityXMLTile.getNumber();
            int tileNumber = number - 1;
            int xPosition = currentEternityXMLTile.getXPosition();
            int yPosition = currentEternityXMLTile.getYPosition();
            int rotation = currentEternityXMLTile.getRotation();
            EternityTile4x4 eternityTile4x4 = EternityTiles4x4.TILESSTORE.getTileAt(tileNumber % 4, tileNumber / 4);
            if (number > 0 && number < 17) {
                if (!checkedTiles[tileNumber]) {
                    if (rotation > -1 && rotation < 4) {
                        if ((xPosition > -1) && (xPosition < 4)) {
                            if ((yPosition > -1) && (yPosition < 4)) {
                                if (eternityBoard.isTileFree(xPosition, yPosition)) {
                                    for (int j = 0; j < rotation; j++) {
                                        eternityTile4x4.rotateClockwise();
                                    }
                                    eternityBoard.setTileAt(xPosition, yPosition, eternityTile4x4);
                                    checkedTiles[tileNumber] = true;
                                } else
                                    throw new IllegalArgumentException("Coordinate Y must be between 0 and getYBoardSize().");
                            } else
                                throw new IllegalArgumentException("Coordinate X must be between 0 and getXBoardSize().");
                        } else throw new IllegalArgumentException("Rotation must be between 0 and 3.");
                    } else throw new IllegalArgumentException("Tiles must have a unique backValue number.");
                } else throw new IllegalArgumentException("Back value must be an integer between 1 and 16.");
            }
        }
        return eternityBoard;
    }

}