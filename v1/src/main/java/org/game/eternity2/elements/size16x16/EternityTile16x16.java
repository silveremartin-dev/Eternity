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


import org.game.eternity2.elements.AbstractEternityTilesImageSingleton;
import org.game.eternity2.elements.AbstractEternityTile;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * One of the 256 tiles from the Eternity II box set.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityTile16x16 extends AbstractEternityTile {

    public EternityTile16x16(int backValue, @NotNull final EternityBasicPattern16x16 top, @NotNull final EternityBasicPattern16x16 right, @NotNull final EternityBasicPattern16x16 bottom, @NotNull final EternityBasicPattern16x16 left) {
        super(backValue, top, right, bottom, left, 256);
    }

    @Override
    public Image getImage() {
        return AbstractEternityTilesImageSingleton.getInstance().getImageForTile(this);
    }

}