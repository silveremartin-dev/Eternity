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


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

/**
 * A class to store a pattern as it is found on top, bottom, left and right of each Eternity II game tile.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class AbstractEternityBasicPattern implements EternityBasicPatternInterface, Cloneable {

    private final int value;

    private BufferedImage image;

    private final int maxValue;

    protected AbstractEternityBasicPattern(int value, String path, int maxValue) {

        if (value >= 0 && value <= maxValue) {
            this.value = value;
            try {
                image = ImageIO.read(new File(path));
            } catch (IOException e) {
                image = null;
            }
            this.maxValue = maxValue;
        } else throw new IllegalArgumentException("Value must be an integer between 0 and " + maxValue + ".");

    }

    @Override
    public int getValue() {
        return value;
    }
    
    @Override
    public Image getImage() {
        return image;
    }

    @Override
    //deep copy
    public Object clone() {
        AbstractEternityBasicPattern result;
        try {
            result = (AbstractEternityBasicPattern) super.clone();
        } catch (CloneNotSupportedException e) {
            result = new AbstractEternityBasicPattern(value, null, maxValue);
        }
        result.image = deepCopy(image);
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

}