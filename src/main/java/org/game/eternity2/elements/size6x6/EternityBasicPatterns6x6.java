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


import org.game.eternity2.elements.AbstractEternityBasicPatterns;

/**
 * The 7 patterns that are to be found on each corner of each tile from the 6x6 eternity II game.
 *
 * WARNING: If you are reading this there is something wrong since we are not allowed to redistribute the actual tiles patterns.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public final class EternityBasicPatterns6x6 extends AbstractEternityBasicPatterns {

    public final static EternityBasicPattern6x6 EternityBasicPatternGray = new EternityBasicPattern6x6(0, "/images/patterns/gray.gif");

    public final static EternityBasicPattern6x6 EternityBasicPatternOrangeLightBlue = new EternityBasicPattern6x6(1, "/images/patterns/orangelightblue.gif");

    public final static EternityBasicPattern6x6 EternityBasicPatternDarkBlueYellow = new EternityBasicPattern6x6(2, "/images/patterns/darkblueyellow.gif");

    public final static EternityBasicPattern6x6 EternityBasicPatternRoseLightBlue = new EternityBasicPattern6x6(3, "/images/patterns/roselightblue.gif");

    public final static EternityBasicPattern6x6 EternityBasicPatternGreenDarkBlue = new EternityBasicPattern6x6(4, "/images/patterns/greendarkblue.gif");

    public final static EternityBasicPattern6x6 EternityBasicPatternRoseYellow2 = new EternityBasicPattern6x6(5, "/images/patterns/roseyellow2.gif");

    public final static EternityBasicPattern6x6 EternityBasicPatternPurpleLightBlue = new EternityBasicPattern6x6(6, "/images/patterns/purplelightblue.gif");

    public final static EternityBasicPattern6x6 EternityBasicPatternYellowLightBlue = new EternityBasicPattern6x6(7, "/images/patterns/yellowlightblue.gif");

    public static EternityBasicPattern6x6 getPatternFromInteger(int value) {
        switch (value) {
            case 0 -> {
                return EternityBasicPatternGray;
            }
            case 1 -> {
                return EternityBasicPatternOrangeLightBlue;
            }
            case 2 -> {
                return EternityBasicPatternDarkBlueYellow;
            }
            case 3 -> {
                return EternityBasicPatternRoseLightBlue;
            }
            case 4 -> {
                return EternityBasicPatternGreenDarkBlue;
            }
            case 5 -> {
                return EternityBasicPatternRoseYellow2;
            }
            case 6 -> {
                return EternityBasicPatternPurpleLightBlue;
            }
            case 7 -> {
                return EternityBasicPatternYellowLightBlue;
            }
            default -> throw new IllegalArgumentException("Value must be between 0 and 7.");
        }
    }

}