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

import org.game.eternity2.elements.AbstractEternityBoard;
import org.game.eternity2.elements.EternityBoardInterface;

import org.game.eternity2.elements.EternityTileInterface;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * An Eternity II 4x4 puzzle Board that can be incompletely filled.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityBoard4x4 extends AbstractEternityBoard {

    @Serial
    private static final long serialVersionUID = 1L;

    public final static EternityBoard4x4 emptyEternityBoard = new EternityBoard4x4() {

        @Override
        public EternityTile4x4 getTileAtNoCheck(int x, int y) {
            return EternityTiles4x4.EternityTile0;
        }

        @Override
        public void setTileAtNoCheck(int x, int y, @NotNull EternityTileInterface tile) {
            throw new UnsupportedOperationException("This is an empty Board only");
        }

        @Override
        public EternityTile4x4 getTileAt(int x, int y) {
            if ((x >= 0) && (x < 4)) {
                if ((y >= 0) && (y < 4)) {
                    return EternityTiles4x4.EternityTile0;
                } else
                    throw new IllegalArgumentException("Coordinate Y must be between 0 and getYBoardSize().");
            } else
                throw new IllegalArgumentException("Coordinate X must be between 0 and getXBoardSize().");
        }

        @Override
        public boolean setTileAt(int x, int y, @NotNull EternityTileInterface tile) {
            throw new UnsupportedOperationException("This is an empty Board only");
        }

    };

    public EternityBoard4x4() {
        super(4, 4);
    }

    @Override
    public EternityTile4x4 getTileAtNoCheck(int x, int y) {
        return (EternityTile4x4) tiles[x + 4 * y];
    }

    public void setTileAtNoCheck(int x, int y, @NotNull EternityTileInterface tile) {
        tiles[x + 4 * y] = (EternityTile4x4) tile;
    }

    @Override
    public EternityTile4x4 getTileAt(int x, int y) {
        if ((x > -1) && (x < 4)) {
            if ((y > -1) && (y < 4)) {
                return (EternityTile4x4) tiles[x + 4 * y];
            } else
                throw new IllegalArgumentException("Coordinate Y must be between 0 and 3.");
        } else
            throw new IllegalArgumentException("Coordinate X must be between 0 and 3.");
    }

    public boolean setTileAt(int x, int y, @NotNull EternityTileInterface tile) {
        boolean result;
        result = false;
        if (isValid(x, y)) {
            if (isTileFree(x, y)) {
                if (areNeighborsMatching(x, y, tile)) {
                    if (isCorner(x, y)) {
                        if (areBordersMatchingForBorderTile(x, y, tile)) {
                            tiles[x + 4 * y] = (EternityTile4x4) tile;
                            result = true;
                        }
                    } else {
                        if (areBordersMatchingForInBoardTile(tile)) {
                            tiles[x + 4 * y] = (EternityTile4x4) tile;
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean areBordersComplete() {
        int i, j;
        boolean found;
        i = 0;
        j = 0;
        found = false;
        while (i < 4 && !found) {
            found = tiles[i + j * 4] == null;
            i++;
        }
        while (j < 3 && !found) {
            found = (tiles[0 + j * 4] == null || tiles[3 + j * 4] == null);
            j++;
        }
        i = 0;
        j = 3;
        while (i < 4 && !found) {
            found = tiles[i + j * 4] == null;
            i++;
        }
        return !found;
    }

    @Override
    public boolean areBordersCorrect() {
        int i, j;
        boolean result;
        i = 0;
        j = 0;
        result = areBordersComplete();
        while (i < 4 && result) {
            result = tiles[i + j * 4].getTop().equals(EternityBasicPatterns4x4.EternityBasicPatternGray);
            i++;
        }
        while (j < 3 && result) {
            result = (tiles[0 + j * 4].getLeft().equals(EternityBasicPatterns4x4.EternityBasicPatternGray)
                    || tiles[3 + j * 4].getRight().equals(EternityBasicPatterns4x4.EternityBasicPatternGray));
            j++;
        }
        i = 0;
        j = 3;
        while (i < 4 && result) {
            result = tiles[i + j * 4].getBottom().equals(EternityBasicPatterns4x4.EternityBasicPatternGray);
            i++;
        }
        return result;
    }

    public boolean areHintTilesInPlace(@NotNull EternityBoardInterface hintsBoard) {
        int i, j;
        boolean result;
        result = true;
        i = 0;
        while (i < 4 && result) {
            j = 0;
            while (j < 4 && result) {
                if (hintsBoard.getTileAtNoCheck(i, j) != null) {
                    result = tiles[i + j * 4].equals(hintsBoard.getTileAtNoCheck(i, j));
                }
                j++;
            }
            i++;
        }
        return result;
    }

    public boolean areAllHintTilesFree(@NotNull EternityBoardInterface hintsBoard) {
        int i, j;
        boolean result;
        result = true;
        i = 0;
        while (i < 4 && result) {
            j = 0;
            while (j < 4 && result) {
                if (hintsBoard.getTileAtNoCheck(i, j) != null) {
                    result = tiles[i + j * 4] == null;
                }
                j++;
            }
            i++;
        }
        return result;
    }

    public void putHintTilesOnBoardNoCheck(@NotNull EternityBoardInterface hintsBoard) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (hintsBoard.getTileAtNoCheck(i, j) != null) {
                    tiles[i + j * 4] = (EternityTile4x4) hintsBoard.getTileAtNoCheck(i, j);
                }
            }
        }
    }

    public boolean putHintTilesOnBoard(@NotNull EternityBoardInterface hintsBoard) {
        int i, j;
        boolean result;
        org.game.eternity2.elements.AbstractEternityTile[] resultTiles;
        resultTiles = Arrays.copyOf(tiles, tiles.length);
        result = true;
        i = 0;
        while (i < 4 && result) {
            j = 0;
            while (j < 4 && result) {
                if (hintsBoard.getTileAt(i, j) != null) {
                    result = tiles[i + j * 4] == null;
                    resultTiles[i + j * 4] = (EternityTile4x4) hintsBoard.getTileAt(i, j);
                }
                j++;
            }
            i++;
        }
        if (result) {
            tiles = resultTiles;
        }
        return result;
    }

    @Override
    public int numTiles() {
        int result;
        result = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (tiles[i + j * 4] != null) {
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public boolean doAllTilesMatch() {
        int i, j;
        boolean result;
        result = true;
        // we only need to check one tile every two because neighboring is a symmetrical
        // relation
        i = 0;
        while (i < 4 && result) {
            j = i % 2; // start alternatively odd or even
            while (j < 4 && result) {
                result = areNeighborsMatching(i, j, tiles[i + j * 4]);
                j = j + 2;
            }
            i++;
        }
        return result;
    }

    @Override
    public boolean isBoardFilled() {
        boolean found;
        int i;
        found = false;
        i = 0;
        while (i < 16 && !found) {
            found = (tiles[i] == null || tiles[i].equals(EternityTiles4x4.EternityTile0));
            i++;
        }
        return !found;
    }

    public Set<EternityTileInterface> hasDuplicateTilesOnBoard() {
        Set<EternityTileInterface> resultTiles;
        int[] foundTiles;
        resultTiles = new HashSet<>();
        foundTiles = new int[16];
        for (int i = 0; i < 16; i++) {
            foundTiles[i] = 0;
        }
        for (int i = 0; i < 16; i++) {
            if (tiles[i] != null && !tiles[i].equals(EternityTiles4x4.EternityTile0)) {
                foundTiles[tiles[i].getBackValue()]++;
            }
        }
        for (int i = 0; i < 16; i++) {
            if (foundTiles[i] > 1) {
                resultTiles.add(EternityTiles4x4.TILESSTORE.getTileAtNoCheck(i % 4, i / 4));
            }
        }
        return resultTiles;
    }

    public Set<EternityTileInterface> getTiles() {
        Set<EternityTileInterface> resultTiles;
        resultTiles = new HashSet<>();
        for (int i = 0; i < 16; i++) {
            if (tiles[i] != null && !tiles[i].equals(EternityTiles4x4.EternityTile0)) {
                resultTiles.add(tiles[i]);
            }
        }
        return resultTiles;
    }

    @Override
    public EternityTile4x4[][] getTilesAsArray() {
        EternityTile4x4[][] result;
        result = new EternityTile4x4[4][4];
        for (int i = 0; i < 16; i++) {
            result[i % 4][i / 4] = (EternityTile4x4) tiles[i];
        }
        return result;
    }

    public Set<EternityTileInterface> getMissingTiles() {
        Set<EternityTileInterface> resultTiles;
        boolean[] foundTiles;
        resultTiles = new HashSet<>();
        foundTiles = new boolean[16];
        for (int i = 0; i < 16; i++) {
            foundTiles[i] = false;
        }
        for (int i = 0; i < 16; i++) {
            if (tiles[i] != null && !tiles[i].equals(EternityTiles4x4.EternityTile0)) {
                foundTiles[tiles[i].getBackValue()] = true;
            }
        }
        for (int i = 0; i < 16; i++) {
            if (!foundTiles[i]) {
                resultTiles.add(EternityTiles4x4.TILESSTORE.getTileAtNoCheck(i % 4, i / 4));
            }
        }
        return resultTiles;
    }

    @Override
    public int computeScore() {
        int i, j;
        int result;
        result = 0;
        // we only need to check one tile every two because neighboring is a symmetrical
        // relation
        i = 0;
        while (i < 4) {
            j = i % 2; // start alternatively odd or even
            while (j < 4) {
                result += getNeighborsMatchingCount(i, j, tiles[i + j * 4]);
                j = j + 2;
            }
            i++;
        }
        return result;
    }

    // outputs true if you have won
    @Override
    public boolean isWinningSolution() {
        return computeScore() == 24;
    }

    @Override
    public boolean isValid(int x, int y) {
        return (x > -1) && (x < 4) && (y > -1) && (y < 4);
    }

    @Override
    public boolean isCorner(int x, int y) {
        return ((x == 0) || (x == 3)) && ((y == 0) || (y == 3));
    }

    @Override
    public boolean isBorder(int x, int y) {
        return (x == 0) || (x == 3) || (y == 0) || (y == 3);
    }

    // assume isValid called first
    @Override
    public boolean isTileFree(int x, int y) {
        return tiles[x + 4 * y] == null || tiles[x + 4 * y].equals(EternityTiles4x4.EternityTile0);
    }

    // assume isValid called first
    @Override
    public boolean areNeighborsMatching(int x, int y, @NotNull EternityTileInterface eternityTile) {
        // if neighbors don't exist there is nothing to check
        boolean result;
        // left tile if any
        result = (x > 0 && (tiles[(x - 1) + 4 * y] == null || (tiles[(x - 1) + 4 * y] != null
                && eternityTile.getLeft().equals(tiles[(x - 1) + 4 * y].getRight()))));
        // right tile if any
        result = result && (x < 3 && (tiles[(x + 1) + 4 * y] == null || (tiles[(x + 1) + 4 * y] != null
                && eternityTile.getRight().equals(tiles[(x + 1) + 4 * y].getLeft()))));
        // top tile if any
        result = result && (y > 0 && (tiles[x + 4 * (y - 1)] == null || (tiles[x + 4 * (y - 1)] != null
                && eternityTile.getTop().equals(tiles[x + 4 * (y - 1)].getBottom()))));
        // bottom tile if any
        result = result && (y < 3 && (tiles[x + 4 * (y + 1)] == null || (tiles[x + 4 * (y + 1)] != null
                && eternityTile.getBottom().equals(tiles[x + 4 * (y + 1)].getTop()))));
        return result;
    }

    public int getNeighborsMatchingCount(int x, int y, @NotNull EternityTileInterface eternityTile) {
        // if neighbors don't exist there is nothing to check
        // if tile is on border we need to check only 3 or 2 neighbors
        int result;
        result = 0;
        // left tile if any
        if (x > 0 && tiles[(x - 1) + 4 * y] != null
                && eternityTile.getLeft().equals(tiles[(x - 1) + 4 * y].getRight())) {
            result++;
        }
        // right tile if any
        if (x < 3 && tiles[(x + 1) + 4 * y] != null
                && eternityTile.getRight().equals(tiles[(x + 1) + 4 * y].getLeft())) {
            result++;
        }
        // top tile if any
        if (y > 0 && tiles[x + 4 * (y - 1)] != null
                && eternityTile.getTop().equals(tiles[x + 4 * (y - 1)].getBottom())) {
            result++;
        }
        // bottom tile if any
        if (y < 3 && tiles[x + 4 * (y + 1)] != null
                && eternityTile.getBottom().equals(tiles[x + 4 * (y + 1)].getTop())) {
            result++;
        }
        return result;
    }

    @Override
    public boolean areBordersMatchingForBorderTile(int x, int y, @NotNull EternityTileInterface eternityTile) {
        boolean result;
        result = ((x == 0) && eternityTile.getLeft().equals(EternityBasicPatterns4x4.EternityBasicPatternGray))
                || ((x == 3) && eternityTile.getRight().equals(EternityBasicPatterns4x4.EternityBasicPatternGray));
        result = result && (((y == 0)
                && eternityTile.getTop().equals(EternityBasicPatterns4x4.EternityBasicPatternGray))
                || ((y == 3) && eternityTile.getBottom().equals(EternityBasicPatterns4x4.EternityBasicPatternGray)));
        return result;
    }

    // assume tile not on border
    @Override
    public boolean areBordersMatchingForInBoardTile(@NotNull EternityTileInterface eternityTile) {
        return !eternityTile.getLeft().equals(EternityBasicPatterns4x4.EternityBasicPatternGray)
                && !eternityTile.getRight().equals(EternityBasicPatterns4x4.EternityBasicPatternGray)
                && !eternityTile.getTop().equals(EternityBasicPatterns4x4.EternityBasicPatternGray)
                && !eternityTile.getBottom().equals(EternityBasicPatterns4x4.EternityBasicPatternGray);
    }

    @Override
    public Image getImage() {
        boolean found;
        int i;
        int width;
        int height;
        Graphics2D paint;
        BufferedImage result;
        found = false;
        i = 0;
        width = -1;
        height = -1;
        while (i < 16 && !found) {
            if (tiles[i] != null) {
                width = tiles[i].getImage().getWidth(null);
                height = tiles[i].getImage().getHeight(null);
                found = true;
            } else
                i++;
        }
        if (found) {
            result = new BufferedImage(width * 4, height * 4, BufferedImage.TYPE_INT_ARGB);
            paint = result.createGraphics();
            paint.setPaint(Color.WHITE);
            paint.fillRect(0, 0, width * 4, height * 4);
            paint.setBackground(Color.WHITE);
            for (i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (tiles[i + j * 4] != null) {
                        paint.drawImage(tiles[i + j * 4].getImage(), i * width, j * height, null);
                    }
                }
            }
            paint.dispose();
        } else
            result = null;
        return result;
    }

    @Override
    public Object clone() {
        return (EternityBoard4x4) super.clone();
    }

    @Override
    public String toString() {
        StringBuffer result;
        result = new StringBuffer();
        IntStream.range(0, 15).forEach(i -> {
            result.append(tiles[i].toString());
            result.append(", ");
        });
        result.append(tiles[15]);
        return result.toString();
    }

}
