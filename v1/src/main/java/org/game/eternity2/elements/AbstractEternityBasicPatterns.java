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


/**
 * The 22 patterns that are to be found on each corner of each tile from the eternity II game. 
 *
 * WARNING: If you are reading this there is something wrong since we are not allowed to redistribute the actual tiles patterns.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class AbstractEternityBasicPatterns {

    public final static AbstractEternityBasicPattern EternityBasicPatternGray = new AbstractEternityBasicPattern(0, "/images/patterns/gray.gif", 0);

    public static AbstractEternityBasicPattern getPatternFromInteger(int value) {
        switch (value) {
            case 0 : return EternityBasicPatternGray;
            default : throw new IllegalArgumentException("Value must be 0.");
        }
    }

}