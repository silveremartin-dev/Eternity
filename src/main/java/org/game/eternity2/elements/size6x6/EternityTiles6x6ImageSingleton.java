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

import org.game.eternity2.elements.AbstractEternityTilesImageSingleton;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * A cache to hold pre generated images of the tiles.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityTiles6x6ImageSingleton extends AbstractEternityTilesImageSingleton {

    private static class EternityTilesImageSingletonHolder {
        private final static EternityTiles6x6ImageSingleton instance = new EternityTiles6x6ImageSingleton();

    }

    private static Image[] tilesImage;

    private EternityTiles6x6ImageSingleton() {
        tilesImage = new Image[36];
        for (int i=0; i<6;i++) {
            for (int j=0; j<6;j++) {
                tilesImage[i+j*6] = computeImageForTile(EternityTiles6x6.TILESSTORE.getTileAt(i, j));
            }
        }
    }

    public static EternityTiles6x6ImageSingleton getInstance() {
        return EternityTilesImageSingletonHolder.instance;
    }

    public Image getImageForTile(@NotNull EternityTile6x6 eternityTile) {
        return tilesImage[eternityTile.getBackValue()];
    }

}
