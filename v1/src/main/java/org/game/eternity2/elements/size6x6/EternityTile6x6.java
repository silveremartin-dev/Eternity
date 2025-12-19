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


import org.game.eternity2.elements.AbstractEternityTile;
import org.game.eternity2.elements.size16x16.EternityBasicPattern16x16;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * One of the 36 tiles from the Eternity II 6x6 puzzle.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityTile6x6 extends AbstractEternityTile {

    public EternityTile6x6(int backValue, @NotNull final EternityBasicPattern6x6 top, @NotNull final EternityBasicPattern6x6 right, @NotNull final EternityBasicPattern6x6 bottom, @NotNull final EternityBasicPattern6x6 left) {
        super(backValue, top, right, bottom, left, 36);
    }

    @Override
    public Image getImage() {
        return EternityTiles6x6ImageSingleton.getInstance().getImageForTile(this);
    }

}