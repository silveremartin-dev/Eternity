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

package org.game.eternity2.elements.size6x6;

import org.game.eternity2.io.AbstractEternityBoardReader;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * A way to read a (possibly partial) solution to the Eternity II box set from a file.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityBoard6x6Reader extends AbstractEternityBoardReader {

    public final static String DEFAULT_PATH_HINTS_6x6 = "xml/data/e2hints6x6.xml";

    public final static String DEFAULT_PATH_PIECES_6x6 = "xml/data/e2pieces6x6.xml";

    public final static String DEFAULT_PATH_SOLVED_6x6 = "xml/data/e2solvedboard6x6.xml";

    public final static EternityBoard6x6 hintEternityBoard6x6 = new EternityBoard6x6Reader(DEFAULT_PATH_HINTS_6x6).getBoard();

    public final static EternityBoard6x6 piecesEternityBoard6x6 = new EternityBoard6x6Reader(DEFAULT_PATH_PIECES_6x6).getBoard();

    public final static EternityBoard6x6 solvedEternityBoard6x6 = new EternityBoard6x6Reader(DEFAULT_PATH_SOLVED_6x6).getBoard();

    public EternityBoard6x6Reader(@NotNull String path) {
        super(path);
    }
    
    public final @NotNull EternityBoard6x6 getBoard() {
        List<EternityXMLTile> eternityXMLTiles;
        EternityBoard6x6 eternityBoard;
        boolean[] checkedTiles;
        eternityXMLTiles = getEternityXMLGameBoard().getEternityXMLTiles();
        eternityBoard = new EternityBoard6x6();
        checkedTiles = new boolean[eternityXMLTiles.size()];
        for (int i = 0; i< eternityXMLTiles.size(); i++) {
            checkedTiles[i]=false;
        }
        for (EternityXMLTile currentEternityXMLTile : eternityXMLTiles) {
            int number = currentEternityXMLTile.getNumber();
            int tileNumber = number - 1;
            int xPosition = currentEternityXMLTile.getXPosition();
            int yPosition = currentEternityXMLTile.getYPosition();
            int rotation = currentEternityXMLTile.getRotation();
            EternityTile6x6 eternityTile6x6 = EternityTiles6x6.TILESSTORE.getTileAt(tileNumber % 6, tileNumber / 6);
            if (number > 0 && number < 37) {
                if (!checkedTiles[tileNumber]) {
                    if (rotation > -1 && rotation < 4) {
                        if ((xPosition > -1) && (xPosition < 6)) {
                            if ((yPosition > -1) && (yPosition < 6)) {
                                if (eternityBoard.isTileFree(xPosition, yPosition)) {
                                    for (int j = 0; j < rotation; j++) {
                                        eternityTile6x6.rotateClockwise();
                                    }
                                    eternityBoard.setTileAt(xPosition, yPosition, eternityTile6x6);
                                    checkedTiles[tileNumber] = true;
                                } else
                                    throw new IllegalArgumentException("Coordinate Y must be between 0 and getYBoardSize().");
                            } else
                                throw new IllegalArgumentException("Coordinate X must be between 0 and getXBoardSize().");
                        } else throw new IllegalArgumentException("Rotation must be between 0 and 3.");
                    } else throw new IllegalArgumentException("Tiles must have a unique backValue number.");
                } else throw new IllegalArgumentException("Back value must be an integer between 1 and 36.");
            }
        }
        return eternityBoard;
    }

}