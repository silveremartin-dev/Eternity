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

import org.game.eternity2.io.AbstractEternityTilesReader;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EternityTiles16x16Reader extends AbstractEternityTilesReader {

    public final static String DEFAULT_PATH_16x16 = "xml/data/e2tiles16x16.xml";

    public final static Set<EternityTile16x16> EternityTiles16x16 = new EternityTiles16x16Reader(DEFAULT_PATH_16x16)
            .getTiles();

    public EternityTiles16x16Reader(@NotNull String path) {
        super(path);
    }

    public @NotNull Set<EternityTile16x16> getTiles() {
        List<EternityXMLTile> eternityXMLTiles;
        Set<EternityTile16x16> eternityTiles;
        boolean[] checkedTiles;
        eternityXMLTiles = getEternityXMLGameTiles().getEternityXMLTiles();
        eternityTiles = new HashSet<>();
        checkedTiles = new boolean[eternityXMLTiles.size()];
        for (int i = 0; i < eternityXMLTiles.size(); i++) {
            checkedTiles[i] = false;
        }
        for (EternityXMLTile currentEternityXMLTile : eternityXMLTiles) {
            EternityTile16x16 currentTile = new EternityTile16x16(currentEternityXMLTile.getNumber(),
                    EternityBasicPatterns16x16.getPatternFromInteger(currentEternityXMLTile.getTop()),
                    EternityBasicPatterns16x16.getPatternFromInteger(currentEternityXMLTile.getRight()),
                    EternityBasicPatterns16x16.getPatternFromInteger(currentEternityXMLTile.getBottom()),
                    EternityBasicPatterns16x16.getPatternFromInteger(currentEternityXMLTile.getLeft()));
            if (!checkedTiles[currentEternityXMLTile.getNumber()]) {
                eternityTiles.add(currentTile);
                checkedTiles[currentEternityXMLTile.getNumber()] = true;
            } else
                throw new IllegalArgumentException("Tiles must have a unique backValue number.");
        }
        return eternityTiles;
    }

}