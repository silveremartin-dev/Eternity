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

package org.game.eternity2.client;

import org.game.eternity2.elements.AbstractEternityBoard;
import org.game.eternity2.elements.EternityBoardInterface;

/**
 * An algorithm to solve the puzzle.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

// brute force solver
public class AdvancedEternitySolver implements EternitySolverInterface {

    public AbstractEternityBoard computeTessellation(EternityBoardInterface startingBoard) {
        // http://www.shortestpath.se/eii/eii_details.html
        // https://sourceforge.net/projects/eternityii/
        throw new RuntimeException("Not yet implemented.");
        // can we rotate tiles ?
        // maintain a list of free tiles and check if there are remaining patterns
        // combinations
        // start with the border
        // try to make connected subsets and then connect them
        // use symetries
        // are there duplicate tiles ?

        // better work by putting all tiles on board and swapping them ? faster ?
    }
}
