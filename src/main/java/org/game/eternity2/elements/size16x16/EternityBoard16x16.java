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


import org.game.eternity2.elements.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.game.eternity2.elements.size16x16.EternityBasicPatterns16x16.EternityBasicPatternGray;

/**
 * An Eternity II game Board that can be incompletely filled.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityBoard16x16 extends AbstractEternityBoard {

    @Serial
    private static final long serialVersionUID =  1L;

    public final static EternityBoard16x16 emptyEternityBoard = new EternityBoard16x16() {

        @Override
        public EternityTile16x16 getTileAtNoCheck(int x, int y) {
            return EternityTiles16x16.EternityTile0;
        }

        @Override
        public void setTileAtNoCheck(int x, int y, @NotNull EternityTileInterface tile) {
            throw new UnsupportedOperationException("This is an empty Board only");
        }

        @Override
        public EternityTile16x16 getTileAt(int x, int y) {
            if ((x >= 0) && (x < 16)) {
                if ((y >= 0) && (y < 16)) {
                    return EternityTiles16x16.EternityTile0;
                } else throw new IllegalArgumentException("Coordinate Y must be between 0 and getYBoardSize().");
            } else throw new IllegalArgumentException("Coordinate X must be between 0 and getXBoardSize().");
        }

        @Override
        public boolean setTileAt(int x, int y, @NotNull EternityTile16x16 tile) {
            throw new UnsupportedOperationException("This is an empty Board only");
        }

    };

    protected EternityTile16x16[] tiles;

    public EternityBoard16x16() {
        super(16, 16);
        tiles = new EternityTile16x16[256];
    }

    @Override
    public EternityTile16x16 getTileAtNoCheck(int x, int y) {
        return tiles[x + 16*y];
    }

    public void setTileAtNoCheck(int x, int y, @NotNull EternityTile16x16 tile) {
        tiles[x + 16*y] = tile;
    }

    @Override
    public EternityTile16x16 getTileAt(int x, int y) {
        if ((x > -1) && (x < 16)) {
            if ((y > -1) && (y < 16)) {
                return tiles[x + 16*y];
            } else throw new IllegalArgumentException("Coordinate Y must be between 0 and 15.");
        } else throw new IllegalArgumentException("Coordinate X must be between 0 and 15.");
    }

    public boolean setTileAt(int x, int y, @NotNull EternityTile16x16 tile) {
        boolean result;
        result = false;
        if (isValid(x, y)) {
            if (isTileFree(x, y)) {
                if (areNeighborsMatching(x, y, tile)) {
                    if (isCorner(x, y)) {
                        if (areBordersMatchingForBorderTile(x, y, tile)) {
                            tiles[x + 16*y] = tile;
                            result = true;
                        }
                    } else {
                        if (areBordersMatchingForInBoardTile(tile)) {
                            tiles[x + 16*y] = tile;
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
        int i,j;
        boolean found;
        i=0;
        j=0;
        found = false;
        while (i<16 && !found) {
            found = tiles[i + j*16]==null;
            i++;
        }
        while (j<15 && !found) {
            found = (tiles[0 + j*16]==null || tiles[15 + j*16]==null);
            j++;
        }
        i=0;
        j=15;
        while (i<16 && !found) {
            found = tiles[i + j*16]==null;
            i++;
        }
        return !found;
    }

    @Override
    public boolean areBordersCorrect() {
        int i,j;
        boolean result;
        i=0;
        j=0;
        result = areBordersComplete();
        while (i<16 && result) {
            result = tiles[i + j*16].getTop().equals(EternityBasicPatterns16x16.EternityBasicPatternGray);
            i++;
        }
        while (j<15 && result) {
            result = (tiles[0 + j*16].getLeft().equals(EternityBasicPatterns16x16.EternityBasicPatternGray)|| tiles[15 + j*16].getRight().equals(EternityBasicPatterns16x16.EternityBasicPatternGray));
            j++;
        }
        i=0;
        j=15;
        while (i<16 && result) {
            result = tiles[i + j*16].getBottom().equals(EternityBasicPatterns16x16.EternityBasicPatternGray);
            i++;
        }
        return result;
    }

    public boolean areHintTilesInPlace(@NotNull EternityBoard16x16 hintsBoard) {
        int i,j;
        boolean result;
        result = true;
        i=0;
        while (i<16 && result) {
            j=0;
            while (j<16 && result) {
                if (hintsBoard.getTileAtNoCheck(i, j)!=null) {
                    result = tiles[i +j*16].equals(hintsBoard.getTileAtNoCheck(i, j));
                }
                j++;
            }
            i++;
        }
        return result;
    }

    public boolean areAllHintTilesFree(@NotNull EternityBoard16x16 hintsBoard) {
        int i,j;
        boolean result;
        result = true;
        i=0;
        while (i<16 && result) {
            j=0;
            while (j<16 && result) {
                if (hintsBoard.getTileAtNoCheck(i, j)!=null) {
                    result = tiles[i +j*16]==null;
                }
                j++;
            }
            i++;
        }
        return result;
    }

    public void putHintTilesOnBoardNoCheck(@NotNull EternityBoard16x16 hintsBoard) {
        for (int i=0; i<16; i++) {
            for (int j=0; j<16; j++) {
                if (hintsBoard.getTileAtNoCheck(i, j)!=null) {
                    tiles[i +j*16] = hintsBoard.getTileAtNoCheck(i, j);
                }
            }
        }
    }

    public boolean putHintTilesOnBoard(@NotNull EternityBoard16x16 hintsBoard) {
        int i,j;
        boolean result;
        EternityTile16x16[] resultTiles;
        resultTiles = Arrays.copyOf(tiles, tiles.length);
        result = true;
        i=0;
        while (i<16 && result) {
            j=0;
            while (j<16 && result) {
                if (hintsBoard.getTileAt(i, j)!=null) {
                    result = tiles[i +j*16]==null;
                    resultTiles[i +j*16] = hintsBoard.getTileAt(i, j);
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
        for (int i=0; i<16; i++) {
            for (int j=0; j<16; j++) {
                if (tiles[i +j*16]!=null) {
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public boolean doAllTilesMatch() {
        int i,j;
        boolean result;
        result = true;
        //we only need to check one tile every two because neighboring is a symmetrical relation
        i=0;
        while (i<16 && result) {
            j=i%2; // start alternatively odd or even
            while (j<16 && result) {
                result = areNeighborsMatching(i, j, tiles[i + j*16]);
                j=j+2;
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
        i=0;
        while (i<256 && !found) {
            found = (tiles[i]==null || tiles[i].equals(EternityTiles16x16.EternityTile0));
            i++;
        }
        return !found;
    }

    @Override
    public Set<EternityTileInterface> hasDuplicateTilesOnBoard() {
        Set<EternityTileInterface> resultTiles;
        int[] foundTiles;
        resultTiles = new HashSet<>();
        foundTiles = new int[256];
        for (int i = 0; i < 256; i++) {
            foundTiles[i] = 0;
        }
        for (int i = 0; i < 256; i++) {
            if (tiles[i] != null && !tiles[i].equals(EternityTiles16x16.EternityTile0)) {
                foundTiles[tiles[i].getBackValue()]++;
            }
        }
        for (int i = 0; i < 256; i++) {
            if (foundTiles[i] > 1) {
                resultTiles.add(EternityTiles16x16.TILESSTORE.getTileAtNoCheck(i % 16, i / 16));
            }
        }
        return resultTiles;
    }

    @Override
    public Set<EternityTileInterface> getTiles() {
        Set<EternityTileInterface> resultTiles;
        resultTiles = new HashSet<>();
        for (int i = 0; i < 256; i++) {
            if (tiles[i] != null && !tiles[i].equals(EternityTiles16x16.EternityTile0)) {
                resultTiles.add(tiles[i]);
            }
        }
        return resultTiles;
    }

    @Override
    public EternityTile16x16[][] getTilesAsArray() {
        EternityTile16x16[][] result;
        result = new EternityTile16x16[16][16];
        for (int i=0; i<256; i++) {
            result[i%16][i/16] = tiles[i];
        }
        return result;
    }

    @Override
    public Set<EternityTileInterface> getMissingTiles() {
        Set<EternityTileInterface> resultTiles;
        boolean[] foundTiles;
        resultTiles = new HashSet<>();
        foundTiles = new boolean[256];
        for (int i = 0; i<256; i++) {
            foundTiles[i] = false;
        }
        for (int i = 0; i<256; i++) {
            if (tiles[i]!=null && !tiles[i].equals(EternityTiles16x16.EternityTile0)) {
                foundTiles[tiles[i].getBackValue()] = true;
            }
        }
        for (int i = 0; i<256; i++) {
            if (!foundTiles[i]) {
                resultTiles.add(EternityTiles16x16.TILESSTORE.getTileAtNoCheck(i % 16, i / 16));
            }
        }
        return resultTiles;
    }

    @Override
    public int computeScore() {
        int i,j;
        int result;
        result = 0;
        //we only need to check one tile every two because neighboring is a symmetrical relation
        i=0;
        while (i<16) {
            j=i%2; // start alternatively odd or even
            while (j<16) {
                result+=getNeighborsMatchingCount(i, j, tiles[i + j*16]);
                j=j+2;
            }
            i++;
        }
        return result;
    }

    //outputs true if you have won
    @Override
    public boolean isWinningSolution() {
        return computeScore() == 480;
    }
    
    @Override
    public boolean isValid(int x, int y) {
        return (x > -1) && (x < 16) && (y > -1) && (y < 16);
    }

    @Override
    public boolean isCorner(int x, int y) {
        return ((x == 0) || (x == 15)) && ((y == 0) || (y == 15));
    }

    @Override
    public boolean isBorder(int x, int y) {
        return (x == 0) || (x == 15) || (y == 0) || (y == 15);
    }

    //assume isValid called first
    @Override
    public boolean isTileFree(int x, int y) {
        return tiles[x + 16*y]==null || tiles[x + 16 * y].equals(EternityTiles16x16.EternityTile0);
    }

    //assume isValid called first
    @Override
    public boolean areNeighborsMatching(int x, int y, @NotNull EternityTileInterface eternityTile) {
        //if neighbors don't exist there is nothing to check
        boolean result;
        //left tile if any
        result = (x>0 && (tiles[(x-1) + 16*y]==null || (tiles[(x-1) + 16*y]!=null && eternityTile.getLeft().equals(tiles[(x-1) + 16*y].getRight()))));
        //right tile if any
        result = result && (x<15 && (tiles[(x+1) + 16*y]==null || (tiles[(x+1) + 16*y]!=null && eternityTile.getRight().equals(tiles[(x+1) + 16*y].getLeft()))));
        //top tile if any
        result = result && (y>0 && (tiles[x + 16*(y-1)]==null || (tiles[x + 16*(y-1)]!=null && eternityTile.getTop().equals(tiles[x + 16*(y-1)].getBottom()))));
        //bottom tile if any
        result = result && (y<15 && (tiles[x + 16*(y+1)]==null || (tiles[x + 16*(y+1)]!=null && eternityTile.getBottom().equals(tiles[x + 16*(y+1)].getTop()))));
        return result;
    }

    public int getNeighborsMatchingCount(int x, int y, @NotNull EternityTileInterface eternityTile) {
        //if neighbors don't exist there is nothing to check
        //if tile is on border we need to check only 3 or 2 neighbors
        int result;
        result = 0;
        //left tile if any
        if (x > 0 && tiles[(x - 1) + 16 * y] != null && eternityTile.getLeft().equals(tiles[(x - 1) + 16 * y].getRight())) {
            result++;
        }
        //right tile if any
        if (x < 15 && tiles[(x + 1) + 16 * y] != null && eternityTile.getRight().equals(tiles[(x + 1) + 16 * y].getLeft())) {
            result++;
        }
        //top tile if any
        if (y > 0 && tiles[x + 16 * (y - 1)] != null && eternityTile.getTop().equals(tiles[x + 16 * (y - 1)].getBottom())) {
            result++;
        }
        //bottom tile if any
        if (y < 15 && tiles[x + 16 * (y + 1)] != null && eternityTile.getBottom().equals(tiles[x + 16 * (y + 1)].getTop())) {
            result++;
        }
        return result;
    }

    @Override
    public boolean areBordersMatchingForBorderTile(int x, int y, @NotNull EternityTileInterface eternityTile) {
        boolean result;
        result = ((x==0) && eternityTile.getLeft().equals(EternityBasicPatterns16x16.EternityBasicPatternGray)) || ((x==15) && eternityTile.getRight().equals(EternityBasicPatterns16x16.EternityBasicPatternGray));
        result = result && (((y==0) && eternityTile.getTop().equals(EternityBasicPatterns16x16.EternityBasicPatternGray)) || ((y==15) && eternityTile.getBottom().equals(EternityBasicPatterns16x16.EternityBasicPatternGray)));
        return result;
    }

    //assume tile not on border
    @Override
    public boolean areBordersMatchingForInBoardTile(@NotNull EternityTileInterface eternityTile) {
        return !eternityTile.getLeft().equals(EternityBasicPatterns16x16.EternityBasicPatternGray) && !eternityTile.getRight().equals(EternityBasicPatterns16x16.EternityBasicPatternGray)
        && !eternityTile.getTop().equals(EternityBasicPatterns16x16.EternityBasicPatternGray) && !eternityTile.getBottom().equals(EternityBasicPatterns16x16.EternityBasicPatternGray);
    }

    @Override
    public Image getImage() {
        boolean found;
        int i;
        int width;
        int height;
        Graphics2D paint;
        BufferedImage result;
        i = 0;
        found = false;
        width = -1;
        height = -1;
        while (i<256 && !found) {
            if (tiles[i]!=null) {
                 width = tiles[i].getImage().getWidth(null);
                 height = tiles[i].getImage().getHeight(null);
                 found = true;
            } else i++;
        }
        if (found) {
            result = new BufferedImage(width * 16, height * 16, BufferedImage.TYPE_INT_ARGB);
            paint = result.createGraphics();
            paint.setPaint(Color.WHITE);
            paint.fillRect(0, 0, width * 16, height * 16);
            paint.setBackground(Color.WHITE);
            for (i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    if (tiles[i + j * 16] != null) {
                        paint.drawImage(tiles[i + j * 16].getImage(), i * width, j * height, null);
                    }
                }
            }
            paint.dispose();
        } else result = null;
        return result;
    }

    @Override
    public Object clone() {
        return (EternityBoard16x16) super.clone();
    }

    @Override
    public String toString() {
        StringBuffer result;
        result = new StringBuffer();
        for (int i = 0; i < 255; i++) {
            result.append(tiles[i].toString());
            result.append(", ");
        }
        result.append(tiles[255]);
        return result.toString();
    }

}

