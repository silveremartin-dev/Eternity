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


import org.game.eternity2.elements.AbstractEternityTile;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * One of the 16 tiles from the Eternity II online applet.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityTile4x4 extends AbstractEternityTile {

    public EternityTile4x4(int backValue, @NotNull final EternityBasicPattern4x4 top, @NotNull final EternityBasicPattern4x4 right, @NotNull final EternityBasicPattern4x4 bottom, @NotNull final EternityBasicPattern4x4 left) {
        super(backValue, top, right, bottom, left, 16);
    }

    @Override
    public Image getImage() {
        return EternityTiles4x4ImageSingleton.getInstance().getImageForTile(this);
    }

}