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

public final class EternityTiles4x4Reader extends AbstractEternityTilesReader {

    public final static String DEFAULT_PATH_4x4 = "xml/data/e2tiles4x4.xml";

    public final static Set<EternityTile4x4> EternityTiles4x4 = new EternityTiles4x4Reader(DEFAULT_PATH_4x4).getTiles();

    public EternityTiles4x4Reader(@NotNull String path) {
        super(path);
    }

    public @NotNull Set<EternityTile4x4> getTiles() {
        Set<EternityTile4x4> eternityTiles;
        eternityTiles = new HashSet<>();
        boolean[] checkedTiles;
        List<EternityXMLTile> eternityXMLTiles = getEternityXMLGameTiles().getEternityXMLTiles();
        checkedTiles = new boolean[eternityXMLTiles.size()];
        for (int i = 0; i< eternityXMLTiles.size(); i++) {
            checkedTiles[i]=false;
        }
        for (EternityXMLTile currentEternityXMLTile : eternityXMLTiles) {
            EternityTile4x4 currentTile = new EternityTile4x4(currentEternityXMLTile.getNumber(),
                    EternityBasicPatterns4x4.getPatternFromInteger(currentEternityXMLTile.getTop()),
                    EternityBasicPatterns4x4.getPatternFromInteger(currentEternityXMLTile.getRight()),
                    EternityBasicPatterns4x4.getPatternFromInteger(currentEternityXMLTile.getBottom()),
                    EternityBasicPatterns4x4.getPatternFromInteger(currentEternityXMLTile.getLeft()));
            if (!checkedTiles[currentEternityXMLTile.getNumber()]) {
                eternityTiles.add(currentTile);
                checkedTiles[currentEternityXMLTile.getNumber()] = true;
            } else throw new IllegalArgumentException("Tiles must have a unique backValue number.");
        }
        return eternityTiles;
    }

}