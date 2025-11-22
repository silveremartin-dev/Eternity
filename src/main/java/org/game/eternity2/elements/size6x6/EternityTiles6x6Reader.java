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


import org.game.eternity2.io.AbstractEternityTilesReader;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A way to read the tiles of the Eternity II box set from a file.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public final class EternityTiles6x6Reader extends AbstractEternityTilesReader {

    public final static String DEFAULT_PATH_6x6 = "xml/data/e2tiles6x6.xml";

    public final static Set<EternityTile6x6> EternityTiles6x6 = new EternityTiles6x6Reader(DEFAULT_PATH_6x6).getTiles();

    public EternityTiles6x6Reader(@NotNull String path) {
        super(path);
    }

    public @NotNull Set<EternityTile6x6> getTiles() {
        Set<EternityTile6x6> eternityTiles;
        eternityTiles = new HashSet<>();
        boolean[] checkedTiles;
        List<EternityXMLTile> eternityXMLTiles = getEternityXMLGameTiles().getEternityXMLTiles();
        checkedTiles = new boolean[eternityXMLTiles.size()];
        for (int i = 0; i< eternityXMLTiles.size(); i++) {
            checkedTiles[i]=false;
        }
        for (EternityXMLTile currentEternityXMLTile : eternityXMLTiles) {
            EternityTile6x6 currentTile = new EternityTile6x6(currentEternityXMLTile.getNumber(),
                    EternityBasicPatterns6x6.getPatternFromInteger(currentEternityXMLTile.getTop()),
                    EternityBasicPatterns6x6.getPatternFromInteger(currentEternityXMLTile.getRight()),
                    EternityBasicPatterns6x6.getPatternFromInteger(currentEternityXMLTile.getBottom()),
                    EternityBasicPatterns6x6.getPatternFromInteger(currentEternityXMLTile.getLeft()));
            if (!checkedTiles[currentEternityXMLTile.getNumber()]) {
                eternityTiles.add(currentTile);
                checkedTiles[currentEternityXMLTile.getNumber()] = true;
            } else throw new IllegalArgumentException("Tiles must have a unique backValue number.");
        }
        return eternityTiles;
    }

}