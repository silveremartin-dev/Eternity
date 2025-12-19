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


import org.game.eternity2.elements.AbstractEternityBasicPatterns;
import org.game.eternity2.elements.size16x16.EternityBasicPattern16x16;

/**
 * The 22 patterns that are to be found on each corner of each tile from the eternity II online applet.
 *
 * WARNING: If you are reading this there is something wrong since we are not allowed to redistribute the actual tiles patterns.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public final class EternityBasicPatterns4x4 extends AbstractEternityBasicPatterns {

     public final static EternityBasicPattern4x4 EternityBasicPatternGray = new EternityBasicPattern4x4(0, "/images/patterns/gray.gif");

     public final static EternityBasicPattern4x4 EternityBasicPatternOrangeLightBlue = new EternityBasicPattern4x4(1, "/images/patterns/orangelightblue.gif");

     public final static EternityBasicPattern4x4 EternityBasicPatternRoseYellow2 = new EternityBasicPattern4x4(2, "/images/patterns/roseyellow2.gif");

     public final static EternityBasicPattern4x4 EternityBasicPatternDarkBlueYellow = new EternityBasicPattern4x4(3, "/images/patterns/darkblueyellow.gif");

     public final static EternityBasicPattern4x4 EternityBasicPatternPurpleLightBlue = new EternityBasicPattern4x4(4, "/images/patterns/purplelightblue.gif");

    public static final EternityBasicPattern4x4 getPatternFromInteger(int value) {
        switch (value) {
            case 0 : return EternityBasicPatternGray;
            case 1 : return EternityBasicPatternOrangeLightBlue;
            case 2 : return EternityBasicPatternRoseYellow2;
            case 3 : return EternityBasicPatternDarkBlueYellow;
            case 4 : return EternityBasicPatternPurpleLightBlue;
            default : throw new IllegalArgumentException("Value must be between 0 and 4.");
        }
    }

}