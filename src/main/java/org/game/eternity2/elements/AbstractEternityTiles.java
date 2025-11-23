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
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

package org.game.eternity2.elements;

import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class AbstractEternityTiles {

    public final static AbstractEternityTile EternityTile0 = new AbstractEternityTile(0,
            AbstractEternityBasicPatterns.EternityBasicPatternGray,
            AbstractEternityBasicPatterns.EternityBasicPatternGray,
            AbstractEternityBasicPatterns.EternityBasicPatternGray,
            AbstractEternityBasicPatterns.EternityBasicPatternGray, 0);

    public final static AbstractEternityBoard TILESSTORE = new AbstractEternityBoard(1, 1) {
        @Override
        public boolean setTileAt(int x, int y, @NotNull EternityTileInterface tile) {
            tiles[x + getYBoardSize() * y] = (AbstractEternityTile) tile;
            return true;
        }

        @Override
        public void setTileAtNoCheck(int x, int y, @NotNull EternityTileInterface tile) {
            tiles[x + getYBoardSize() * y] = (AbstractEternityTile) tile;
        }

        @Override
        public EternityTileInterface getTileAtNoCheck(int x, int y) {
            return tiles[x + getYBoardSize() * y];
        }

        @Override
        public EternityTileInterface getTileAt(int x, int y) {
            return tiles[x + getYBoardSize() * y];
        }

        @Override
        public boolean areBordersComplete() {
            return false;
        }

        @Override
        public boolean areBordersCorrect() {
            return false;
        }

        @Override
        public int numTiles() {
            return 0;
        }

        @Override
        public boolean doAllTilesMatch() {
            return false;
        }

        @Override
        public boolean isWinningSolution() {
            return false;
        }

        @Override
        public boolean isValid(int x, int y) {
            return false;
        }

        @Override
        public boolean isCorner(int x, int y) {
            return false;
        }

        @Override
        public boolean isBorder(int x, int y) {
            return false;
        }

        @Override
        public boolean isTileFree(int x, int y) {
            return false;
        }

        @Override
        public boolean areNeighborsMatching(int x, int y, @NotNull EternityTileInterface eternityTile) {
            return false;
        }

        @Override
        public boolean areBordersMatchingForBorderTile(int x, int y, @NotNull EternityTileInterface eternityTile) {
            return false;
        }

        @Override
        public boolean areBordersMatchingForInBoardTile(@NotNull EternityTileInterface eternityTile) {
            return false;
        }

        @Override
        public java.awt.Image getImage() {
            return null;
        }
    };

    // TILESTORE is the full set of available tiles. It has no null tiles or gray
    // tile (EternityTile0). Its numTiles() always equals getXBoardSize() x
    // getYBoardSize()
    static {
        TILESSTORE.setTileAt(0, 0, EternityTile0);
    }

    // returns the duplicate tiles of the TILESSTORE if any
    // this may be a normal situation as some puzzles may have two tiles with
    // different backValue and still the same front face (maybe once rotated)
    public static Set<AbstractEternityTile> duplicates() {
        Set<AbstractEternityTile> result;
        result = new HashSet<>();
        for (int i = 0; i < TILESSTORE.tiles.length; i++) {
            for (int j = i + 1; j < TILESSTORE.tiles.length; j++) {
                if (TILESSTORE.tiles[i].equals(TILESSTORE.tiles[j])) {
                    result.add((TILESSTORE.tiles[i]));
                    result.add((TILESSTORE.tiles[j]));
                }
            }
        }
        return result;
    }
}