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

package org.game.eternity2.elements.size12x6;

import org.game.eternity2.elements.size12x6.EternityBoard12x6;
import org.game.eternity2.elements.size12x6.EternityTile12x6;
import org.game.eternity2.elements.size12x6.EternityTiles12x6;
import org.game.eternity2.io.AbstractEternityBoardReader;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * A way to read a (possibly partial) solution to the Eternity II box set from a file.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityBoard12x6Reader extends AbstractEternityBoardReader {

    public final static String DEFAULT_PATH_HINTS_12x6 = "xml/data/e2hints12x6.xml";

    public final static String DEFAULT_PATH_PIECES_12x6 = "xml/data/e2pieces12x6.xml";

    public final static String DEFAULT_PATH_SOLVED_12x6 = "xml/data/e2solvedboard12x6.xml";

    public final static EternityBoard12x6 hintEternityBoard12x6 = new EternityBoard12x6Reader(DEFAULT_PATH_HINTS_12x6).getBoard();

    public final static EternityBoard12x6 piecesEternityBoard12x6 = new EternityBoard12x6Reader(DEFAULT_PATH_PIECES_12x6).getBoard();

    public final static EternityBoard12x6 solvedEternityBoard12x6 = new EternityBoard12x6Reader(DEFAULT_PATH_SOLVED_12x6).getBoard();

    public EternityBoard12x6Reader(@NotNull String path) {
        super(path);
    }
    
    public final EternityBoard12x6 getBoard() {
        List<EternityXMLTile> eternityXMLTiles;
        EternityBoard12x6 eternityBoard;
        boolean[] checkedTiles;
        eternityXMLTiles = getEternityXMLGameBoard().getEternityXMLTiles();
        eternityBoard = new EternityBoard12x6();
        checkedTiles = new boolean[eternityXMLTiles.size()];
        for (int i = 0; i< eternityXMLTiles.size(); i++) {
            checkedTiles[i]=false;
        }
        for (int i = 0; i < eternityXMLTiles.size(); i++) {
            EternityXMLTile currentEternityXMLTile = eternityXMLTiles.get(i);
            int number = currentEternityXMLTile.getNumber();
            int tileNumber = number-1;
            int xPosition = currentEternityXMLTile.getXPosition();
            int yPosition = currentEternityXMLTile.getYPosition();
            int rotation = currentEternityXMLTile.getRotation();
            EternityTile12x6 eternityTile12x6 = EternityTiles12x6.TILESSTORE.getTileAt(tileNumber % 12, tileNumber / 6);
            if (number > 0 && number < 73) {
                if (!checkedTiles[tileNumber]) {
                    if (rotation > -1 && rotation < 4) {
                        if ((xPosition > -1) && (xPosition < 12)) {
                            if ((yPosition > -1) && (yPosition < 6)) {
                                if (eternityBoard.isTileFree(xPosition, yPosition)) {
                                    for (int j = 0; j < rotation; j++) {
                                        eternityTile12x6.rotateClockwise();
                                    }
                                    eternityBoard.setTileAt(xPosition, yPosition, eternityTile12x6);
                                    checkedTiles[tileNumber] = true;
                                } else
                                    throw new IllegalArgumentException("Coordinate Y must be between 0 and getYBoardSize().");
                            } else
                                throw new IllegalArgumentException("Coordinate X must be between 0 and getXBoardSize().");
                        } else throw new IllegalArgumentException("Rotation must be between 0 and 3.");
                    } else throw new IllegalArgumentException("Tiles must have a unique backValue number.");
                } else throw new IllegalArgumentException("Back value must be an integer between 1 and 72.");
            }
        }
        return eternityBoard;
    }

}