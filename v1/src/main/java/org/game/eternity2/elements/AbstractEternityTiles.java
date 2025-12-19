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

package org.game.eternity2.elements;


import org.game.eternity2.elements.size16x16.EternityBasicPatterns16x16;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.elements.size16x16.EternityTile16x16;

import java.util.HashSet;
import java.util.Set;

/**
 * A blank tile.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class AbstractEternityTiles {

    public final static AbstractEternityTile EternityTile0 = new AbstractEternityTile(0, AbstractEternityBasicPatterns.EternityBasicPatternGray, AbstractEternityBasicPatterns.EternityBasicPatternGray, AbstractEternityBasicPatterns.EternityBasicPatternGray, AbstractEternityBasicPatterns.EternityBasicPatternGray, 0);

    public final static AbstractEternityBoard TILESSTORE = new AbstractEternityBoard(1, 1);

    //TILESTORE is the full set of available tiles. It has no null tiles or gray tile (EternityTile0). Its numTiles() always equals getXBoardSize() x getYBoardSize()
    static {
        TILESSTORE.setTileAt(0, 0, EternityTile0);
    }

    //returns the duplicate tiles of the TILESSTORE if any
    //this may be a normal situation as some puzzles may have two tiles with different backValue and still the same front face (maybe once rotated)
    public static Set<AbstractEternityTile> duplicates() {
        Set<AbstractEternityTile> result;
        result = new HashSet<>();
        for (int i=0; i<TILESSTORE.tiles.length; i++) {
            for(int j=i+1; j<TILESSTORE.tiles.length;j++) {
                if (TILESSTORE.tiles[i].equals(TILESSTORE.tiles[j])) {
                    result.add((TILESSTORE.tiles[i]));
                    result.add((TILESSTORE.tiles[j]));
                }
            }
        }
        return result;
    }
}