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

package org.game.eternity2.elements.size16x16;

import org.game.eternity2.io.AbstractEternityBoardReader;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * A way to read a (possibly partial) solution to the Eternity II box set from a file.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityBoard16x16Reader extends AbstractEternityBoardReader {

    public final static String DEFAULT_PATH_HINTS_16x16 = "xml/data/e2hints16x16.xml";

    public final static String DEFAULT_PATH_PIECES_16x16 = "xml/data/e2pieces16x16.xml";

    //public final static String DEFAULT_PATH_SOLVED_16x16 = "xml/data/e2solvedboard16x16.xml";

    public final static EternityBoard16x16 hintEternityBoard16x16 = new EternityBoard16x16Reader(DEFAULT_PATH_HINTS_16x16).getBoard();

    public final static EternityBoard16x16 piecesEternityBoard16x16 = new EternityBoard16x16Reader(DEFAULT_PATH_PIECES_16x16).getBoard();

    //public final static EternityBoard16x16 solvedEternityBoard16x16 = new EternityBoard16x16Reader(DEFAULT_PATH_SOLVED_16x16).getBoard();

    public EternityBoard16x16Reader(@NotNull String path) {
        super(path);
    }

    public final EternityBoard16x16 getBoard() {
        List<EternityXMLTile> eternityXMLTiles;
        EternityBoard16x16 eternityBoard;
        boolean[] checkedTiles;
        eternityXMLTiles = getEternityXMLGameBoard().getEternityXMLTiles();
        eternityBoard = new EternityBoard16x16();
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
            EternityTile16x16 eternityTile16x16 = EternityTiles16x16.TILESSTORE.getTileAt(tileNumber % 16, tileNumber / 16);
            if (number > 0 && number < 257) {
                if (!checkedTiles[tileNumber]) {
                    if (rotation > -1 && rotation < 4) {
                        if ((xPosition > -1) && (xPosition < 16)) {
                            if ((yPosition > -1) && (yPosition < 16)) {
                                if (eternityBoard.isTileFree(xPosition, yPosition)) {
                                    for (int j = 0; j < rotation; j++) {
                                        eternityTile16x16.rotateClockwise();
                                    }
                                    eternityBoard.setTileAt(xPosition, yPosition, eternityTile16x16);
                                    checkedTiles[tileNumber] = true;
                                } else
                                    throw new IllegalArgumentException("Coordinate Y must be between 0 and getYBoardSize().");
                            } else
                                throw new IllegalArgumentException("Coordinate X must be between 0 and getXBoardSize().");
                        } else throw new IllegalArgumentException("Rotation must be between 0 and 3.");
                    } else throw new IllegalArgumentException("Tiles must have a unique backValue number.");
                } else throw new IllegalArgumentException("Back value must be an integer between 1 and 256.");
            }
        }
        return eternityBoard;
    }

}