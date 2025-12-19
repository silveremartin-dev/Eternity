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

/**
 * This is an implementation of eternity II game for Java.
 *
 * Official game site:
 * http://www.eternityii.com
 *
 * Fan sites:
 * http://www.tetravexii.com/
 * http://www.eternity.net/
 * http://www.eternity2.fr/
 * http://games.groups.yahoo.com/group/eternity_two/
 *
 *
 * All the contents of this site is under Apache License 2.0 (except copyrighted material from Eternity 2 Game of course).
 *
 * Like others, this programs offers to compute valid solutions for the game, assuming you feed it with the correct tiles from the game box.
 *
 * First release, December 2022.
 *
 *
 * v0.5:
 * Solvers.
 * v0.4:
 * Client and server UI.
 * v0.3:
 * Major rewriting but see https://en.wikipedia.org/wiki/Covariance_and_contravariance_(computer_science)
 * v0.2:
 * Done the core tiling and patterning.
 * v0.1:
 * Done the core programming (tile, board).
 *
 *
 * TODO:
 * Support variable board size and tile patterns, see http://www.cristal.org/Eternity-II/shorter.html
 * 12x6 solved game tiles
 * A nice GUI (to display boards or to play manually).
 * A distributed client (GUI and command line) and server (GUI) that would keep track of good subpatterns (Boards), rank the results, give tasks to the clients and gather back the results on a server.
 * A database to store all results.
 * A command line server ?
 *
 * Check if all tiles are different
 * Check how to use symmetries
 *
 * Maybe we should not try to find a pattern for the tile but for the diagonal 22 patterns: as we would get rid of the border we would reduce from 256 (-1 hint) tiles to 480 (-2 hint):
 *
 * Note that the 22 patterns (+ gray border) have different number of occurrence on the board (divide each following number by two as there are 4 half patterns per tile except gray border) and it might prove easier to start (and fail most of the time but earlier on) with patterns with low count:
 * EternityBasicPatternGray: 64
 * EternityBasicPatternLightBlue: 24
 * EternityBasicPatternRoseYellow2: 48
 * EternityBasicPatternBrownGreen: 50
 * EternityBasicPatternLightBlueRose2: 50
 * EternityBasicPatternGreenDarkBlue: 24
 * EternityBasicPatternPurpleYellow: 48
 * EternityBasicPatternLightBlueRose: 50
 * EternityBasicPatternDarkBlueOrange: 50
 * EternityBasicPatternDarkBlueYellow: 24
 * EternityBasicPatternPurpleLightBlue: 48
 * EternityBasicPatternGreenOrange: 50
 * EternityBasicPatternYellowDarkBlue: 50
 * EternityBasicPatternBrownOrange: 24
 * EternityBasicPatternGreenRose: 48
 * EternityBasicPatternYellowGreen: 50
 * EternityBasicPatternDarkBlueLightBlue: 50
 * EternityBasicPatternRoseLightBlue: 24
 * EternityBasicPatternYellowLightBlue: 48
 * EternityBasicPatternBrownYellow: 50
 * EternityBasicPatternOrangePurple: 50
 * EternityBasicPatternRoseYellow: 50
 * EternityBasicPatternDarkBlueRose: 50
 *
 *
 * Tile number (1 to 256), x position (1 to 16), y position (1 to 16), rotation (0 to 3 times 90 degrees clockwise)
 *
 * Hint given with the game
 * 139 9 8 0
 *
 * Extra hint 1: 36-piece Clue Puzzle 1, square (6 × 6) puzzle
 * 181 14 3 1 (unsure)
 *
 * Extra hint 2: 72-piece Clue Puzzle 2, rectangular (12 × 6) puzzle
 * 255 3 14 1 (unsure)
 *
 * Extra hint 3: 36-piece Clue Puzzle 3, square (6 × 6) puzzle
 * ?
 *
 * Extra hint 4: 72-piece Clue Puzzle 4,  rectangular (12 × 6) puzzle
 * ?
 *
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */
package org.game.eternity2;