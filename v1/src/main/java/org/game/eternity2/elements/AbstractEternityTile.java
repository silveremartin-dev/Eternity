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


import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * One of the tiles from an Eternity II puzzle game.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class AbstractEternityTile implements EternityTileInterface {

    private int backValue;

    private AbstractEternityBasicPattern left;
    private AbstractEternityBasicPattern top;
    private AbstractEternityBasicPattern bottom;
    private AbstractEternityBasicPattern right;

    private final int maxValue;

    protected AbstractEternityTile(int backValue, @NotNull final AbstractEternityBasicPattern top, @NotNull final AbstractEternityBasicPattern right, @NotNull final AbstractEternityBasicPattern bottom, @NotNull final AbstractEternityBasicPattern left, int maxValue) {

        if ((top != null) && (right != null) && (bottom != null) && (left != null)) {
            if ((backValue >= 0) && (backValue <= maxValue)) {
                this.backValue = backValue;
                this.top = top;
                this.right = right;
                this.bottom = bottom;
                this.left = left;
                this.maxValue = maxValue;
            } else throw new IllegalArgumentException("Back value must be an integer between 1 and " + maxValue + ".");
        } else throw new IllegalArgumentException("All four patterns must be non null.");

    }

    @Override
    public int getBackValue() {
        return backValue;
    }

    @Override
    public AbstractEternityBasicPattern getTop() {
        return top;
    }

    @Override
    public AbstractEternityBasicPattern getRight() {
        return right;
    }

    @Override
    public AbstractEternityBasicPattern getBottom() {
        return bottom;
    }

    @Override
    public AbstractEternityBasicPattern getLeft() {
        return left;
    }

    //always return null
    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public void rotateClockwise() {
        AbstractEternityBasicPattern tempPattern;
        tempPattern = top;
        top = right;
        right = bottom;
        bottom = left;
        left = tempPattern;
    }

    @Override
    public void rotateCounterClockwise() {
        AbstractEternityBasicPattern tempPattern;
        tempPattern = top;
        top = left;
        left = bottom;
        bottom = right;
        right = tempPattern;
    }

    @Override
    //two tiles are equal is, regardless of the backValue, and regardless of rotation, they share the same pattern in same order, that is : they can show the same front face
    //because the tiles are still not the same because of the backValue they shouldn"t be treated as one tile for set purposes, that is why we don't override hashcode()
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        AbstractEternityTile that = (AbstractEternityTile) o;
        String thisFaceValue = top.toString()+","+left.toString()+","+bottom.toString()+","+right.toString()+top.toString()+","+left.toString()+","+bottom.toString();
        String thatFaceValue = that.top.toString()+", "+that.left.toString()+", "+that.bottom.toString()+", "+that.right.toString();
        return thisFaceValue.contains(thatFaceValue);
    }

    @Override
    public String toString() {
        return String.valueOf(backValue)+", "+top.toString()+", "+left.toString()+", "+bottom.toString()+", "+right.toString();
    }

    @Override
    public Object clone() {
        AbstractEternityTile result;
        try {
            result = (AbstractEternityTile) super.clone();
            result.backValue = backValue;
            result.top = top;
            result.left = left;
            result.bottom = bottom;
            result.right = right;
        } catch (CloneNotSupportedException e) {
            result = new AbstractEternityTile(backValue, top, left, bottom, right, maxValue);
        }
        return result;
    }

}