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


import org.game.eternity2.elements.AbstractEternityBasicPatterns;

/**
 * The 22 patterns that are to be found on each corner of each tile from the eternity II game. 
 *
 * WARNING: If you are reading this there is something wrong since we are not allowed to redistribute the actual tiles patterns.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public final class EternityBasicPatterns16x16 extends AbstractEternityBasicPatterns {

    public final static EternityBasicPattern16x16 EternityBasicPatternGray = new EternityBasicPattern16x16(0, "/images/patterns/gray.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternOrangeLightBlue = new EternityBasicPattern16x16(1, "/images/patterns/orangelightblue.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternRoseYellow2 = new EternityBasicPattern16x16(2, "/images/patterns/roseyellow2.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternBrownGreen = new EternityBasicPattern16x16(3, "/images/patterns/browngreen.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternLightBlueRose2 = new EternityBasicPattern16x16(4, "/images/patterns/lightbluerose2.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternGreenDarkBlue = new EternityBasicPattern16x16(5, "/images/patterns/greendarkblue.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternPurpleYellow = new EternityBasicPattern16x16(6, "/images/patterns/purpleyellow.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternLightBlueRose = new EternityBasicPattern16x16(7, "/images/patterns/lightbluerose.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternDarkBlueOrange = new EternityBasicPattern16x16(8, "/images/patterns/darkblueorange.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternDarkBlueYellow = new EternityBasicPattern16x16(9, "/images/patterns/darkblueyellow.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternPurpleLightBlue = new EternityBasicPattern16x16(10, "/images/patterns/purplelightblue.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternGreenOrange = new EternityBasicPattern16x16(11, "/images/patterns/greenorange.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternYellowDarkBlue = new EternityBasicPattern16x16(12, "/images/patterns/yellowdarkblue.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternBrownOrange = new EternityBasicPattern16x16(13, "/images/patterns/brownorange.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternGreenRose = new EternityBasicPattern16x16(14, "/images/patterns/greenrose.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternYellowGreen = new EternityBasicPattern16x16(15, "/images/patterns/yellowgreen.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternDarkBlueLightBlue = new EternityBasicPattern16x16(16, "/images/patterns/darkbluelightblue.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternRoseLightBlue = new EternityBasicPattern16x16(17, "/images/patterns/roselightblue.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternYellowLightBlue = new EternityBasicPattern16x16(18, "/images/patterns/yellowlightblue.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternBrownYellow = new EternityBasicPattern16x16(19, "/images/patterns/brownyellow.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternOrangePurple = new EternityBasicPattern16x16(20, "/images/patterns/orangepurple.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternRoseYellow = new EternityBasicPattern16x16(21, "/images/patterns/roseyellow.gif");

    public final static EternityBasicPattern16x16 EternityBasicPatternDarkBlueRose = new EternityBasicPattern16x16(22, "/images/patterns/darkbluerose.gif");

    public static final EternityBasicPattern16x16 getPatternFromInteger(int value) {
        switch (value) {
            case 0 : return EternityBasicPatternGray;
            case 1 : return EternityBasicPatternOrangeLightBlue;
            case 2 : return EternityBasicPatternRoseYellow2;
            case 3 : return EternityBasicPatternBrownGreen;
            case 4 : return EternityBasicPatternLightBlueRose2;
            case 5 : return EternityBasicPatternGreenDarkBlue;
            case 6 : return EternityBasicPatternPurpleYellow;
            case 7 : return EternityBasicPatternLightBlueRose;
            case 8 : return EternityBasicPatternDarkBlueOrange;
            case 9 : return EternityBasicPatternDarkBlueYellow;
            case 10 : return EternityBasicPatternPurpleLightBlue;
            case 11 : return EternityBasicPatternGreenOrange;
            case 12 : return EternityBasicPatternYellowDarkBlue;
            case 13 : return EternityBasicPatternBrownOrange;
            case 14 : return EternityBasicPatternGreenRose;
            case 15 : return EternityBasicPatternYellowGreen;
            case 16 : return EternityBasicPatternDarkBlueLightBlue;
            case 17 : return EternityBasicPatternRoseLightBlue;
            case 18 : return EternityBasicPatternYellowLightBlue;
            case 19 : return EternityBasicPatternBrownYellow;
            case 20 : return EternityBasicPatternOrangePurple;
            case 21 : return EternityBasicPatternRoseYellow;
            case 22 : return EternityBasicPatternDarkBlueRose;
            default : throw new IllegalArgumentException("Value must be between 0 and 22.");
        }
    }

}