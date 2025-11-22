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


import org.game.eternity2.elements.AbstractEternityTile;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * One of the 72 tiles from the Eternity II 6x12 puzzle.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityTile12x6 extends AbstractEternityTile {

    public EternityTile12x6(int backValue, @NotNull final EternityBasicPattern12x6 top, @NotNull final EternityBasicPattern12x6 right, @NotNull final EternityBasicPattern12x6 bottom, @NotNull final EternityBasicPattern12x6 left) {
        super(backValue, top, right, bottom, left, 72);
    }

    @Override
    public Image getImage() {
        return EternityTiles6x12ImageSingleton.getInstance().getImageForTile(this);
    }

}