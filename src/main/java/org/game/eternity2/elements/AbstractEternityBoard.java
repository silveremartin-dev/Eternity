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


import org.game.eternity2.elements.size16x16.EternityTile16x16;
import org.game.eternity2.elements.size16x16.EternityTiles16x16;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

public class AbstractEternityBoard implements EternityBoardInterface {

    @Serial
    private static final long serialVersionUID =  1L;

    public static final AbstractEternityBoard emptyEternityBoard = new AbstractEternityBoard(1, 1) {

        @Override
        public AbstractEternityTile getTileAtNoCheck(int x, int y) {
            return AbstractEternityTiles.EternityTile0;
        }

        @Override
        public void setTileAtNoCheck(int x, int y, @NotNull AbstractEternityTile tile) {
            throw new UnsupportedOperationException("This is an empty Board only");
        }

        @Override
        public AbstractEternityTile getTileAt(int x, int y) {
            if (x == 0) {
                if (y == 0) {
                    return AbstractEternityTiles.EternityTile0;
                } else throw new IllegalArgumentException("Coordinate Y must be between 0 and getYBoardSize().");
            } else throw new IllegalArgumentException("Coordinate X must be between 0 and getXBoardSize().");
        }

        @Override
        public boolean setTileAt(int x, int y, @NotNull AbstractEternityTile tile) {
            throw new UnsupportedOperationException("This is an empty Board only");
        }

    };

    protected AbstractEternityTile[] tiles;

    private int maxXValue;

    private int maxYValue;

    protected AbstractEternityBoard(int maxXValue, int maxYValue) {
        if (maxXValue > 0) {
            if (maxYValue > 0) {
                tiles = new AbstractEternityTile[maxXValue * maxYValue];
                this.maxXValue = maxXValue;
                this.maxYValue = maxYValue;
            } else throw new IllegalArgumentException("Coordinate Y must be greater than 0.");
        } else throw new IllegalArgumentException("Coordinate X must be greater than 0.");
    }

    @Override
    public final int getXBoardSize() {
        return maxXValue;
    }

    @Override
    public final int getYBoardSize() {
        return maxYValue;
    }

    //use at your own risks, same as getTileAt without the checks for speedup.
    //can return null
    @Override
    public AbstractEternityTile getTileAtNoCheck(int x, int y) {
        return tiles[x + maxYValue*y];
    }

    public void setTileAtNoCheck(int x, int y, @NotNull AbstractEternityTile tile) {
        tiles[x + maxYValue*y] = tile;
    }

    @Override
    public void setTileAtNoCheck(int x, int y, @NotNull EternityTileInterface tile) {
        tiles[x + maxYValue*y] = (AbstractEternityTile) tile;
    }

    @Override
    public AbstractEternityTile getTileAt(int x, int y) {
        if ((x > -1) && (x < maxXValue)) {
            if ((y > -1) && (y < maxYValue)) {
                return tiles[x + maxYValue*y];
            } else throw new IllegalArgumentException("Coordinate Y must be between 0 and getYBoardSize().");
        } else throw new IllegalArgumentException("Coordinate X must be between 0 and getXBoardSize().");
    }

    public boolean setTileAt(int x, int y, @NotNull AbstractEternityTile tile) {
        boolean result;
        result = false;
        if (isValid(x, y)) {
            if (isTileFree(x, y)) {
                if (areNeighborsMatching(x, y, tile)) {
                    if (isCorner(x, y)) {
                        if (areBordersMatchingForBorderTile(x, y, tile)) {
                            tiles[x + maxYValue*y] = tile;
                            result = true;
                        }
                    } else {
                        if (areBordersMatchingForInBoardTile(tile)) {
                            tiles[x + maxYValue*y] = tile;
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean setTileAt(int x, int y, @NotNull EternityTileInterface tile) {
        boolean result;
        result = false;
        if (isValid(x, y)) {
            if (isTileFree(x, y)) {
                if (areNeighborsMatching(x, y, tile)) {
                    if (isCorner(x, y)) {
                        if (areBordersMatchingForBorderTile(x, y, tile)) {
                            tiles[x + maxYValue*y] = (AbstractEternityTile) tile;
                            result = true;
                        }
                    } else {
                        if (areBordersMatchingForInBoardTile(tile)) {
                            tiles[x + maxYValue*y] = (AbstractEternityTile) tile;
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
        while (i<maxXValue && !found) {
            found = tiles[i + j*maxYValue]==null;
            i++;
        }
        while (j<(maxYValue - 1) && !found) {
            found = (tiles[0 + j*maxYValue]==null || tiles[(maxXValue-1) + j*maxYValue]==null);
            j++;
        }
        i=0;
        j=15;
        while (i<maxXValue && !found) {
            found = tiles[i + j*maxYValue]==null;
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
        while (i<maxXValue && result) {
            result = tiles[i + j*maxYValue].getTop().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray);
            i++;
        }
        while (j<(maxXValue-1) && result) {
            result = (tiles[0 + j*maxYValue].getLeft().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray)|| tiles[(maxXValue-1) + j*maxYValue].getRight().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray));
            j++;
        }
        i=0;
        j=maxYValue-1;
        while (i<maxXValue && result) {
            result = tiles[i + j*maxYValue].getBottom().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray);
            i++;
        }
        return result;
    }

    public boolean areHintTilesInPlace(@NotNull AbstractEternityBoard hintsBoard) {
        int i,j;
        boolean result;
        result = true;
        i=0;
        while (i<maxXValue && result) {
            j=0;
            while (j<maxYValue && result) {
                if (hintsBoard.getTileAtNoCheck(i, j)!=null) {
                    result = tiles[i +j*maxYValue].equals(hintsBoard.getTileAtNoCheck(i, j));
                }
                j++;
            }
            i++;
        }
        return result;
    }

    @Override
    public boolean areHintTilesInPlace(@NotNull EternityBoardInterface hintsBoard) {
        int i,j;
        boolean result;
        result = true;
        i=0;
        while (i<maxXValue && result) {
            j=0;
            while (j<maxYValue && result) {
                if (hintsBoard.getTileAtNoCheck(i, j)!=null) {
                    result = tiles[i +j*maxYValue].equals(hintsBoard.getTileAtNoCheck(i, j));
                }
                j++;
            }
            i++;
        }
        return result;
    }

    public boolean areAllHintTilesFree(@NotNull AbstractEternityBoard hintsBoard) {
        int i,j;
        boolean result;
        result = true;
        i=0;
        while (i<maxXValue && result) {
            j=0;
            while (j<maxYValue && result) {
                if (hintsBoard.getTileAtNoCheck(i, j)!=null) {
                    result = tiles[i +j*maxYValue]==null;
                }
                j++;
            }
            i++;
        }
        return result;
    }

    @Override
    public boolean areAllHintTilesFree(@NotNull EternityBoardInterface hintsBoard) {
        int i,j;
        boolean result;
        result = true;
        i=0;
        while (i<maxXValue && result) {
            j=0;
            while (j<maxYValue && result) {
                if (hintsBoard.getTileAtNoCheck(i, j)!=null) {
                    result = tiles[i +j*maxYValue]==null;
                }
                j++;
            }
            i++;
        }
        return result;
    }

    public void putHintTilesOnBoardNoCheck(@NotNull AbstractEternityBoard hintsBoard) {
        for (int i=0; i<maxXValue; i++) {
            for (int j=0; j<maxYValue; j++) {
                if (hintsBoard.getTileAtNoCheck(i, j)!=null) {
                    tiles[i +j*maxYValue] = hintsBoard.getTileAtNoCheck(i, j);
                }
            }
        }
    }

    @Override
    public void putHintTilesOnBoardNoCheck(@NotNull EternityBoardInterface hintsBoard) {
        for (int i=0; i<maxXValue; i++) {
            for (int j=0; j<maxYValue; j++) {
                if (hintsBoard.getTileAtNoCheck(i, j)!=null) {
                    tiles[i +j*maxYValue] = (AbstractEternityTile) hintsBoard.getTileAtNoCheck(i, j);
                }
            }
        }
    }

    public boolean putHintTilesOnBoard(@NotNull AbstractEternityBoard hintsBoard) {
        int i,j;
        boolean result;
        AbstractEternityTile[] resultTiles;
        resultTiles = Arrays.copyOf(tiles, tiles.length);
        result = true;
        i=0;
        while (i<maxXValue && result) {
            j=0;
            while (j<maxYValue && result) {
                if (hintsBoard.getTileAt(i, j)!=null) {
                    result = tiles[i +j*maxYValue]==null;
                    resultTiles[i +j*maxYValue] = hintsBoard.getTileAt(i, j);
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
    public boolean putHintTilesOnBoard(@NotNull EternityBoardInterface hintsBoard) {
        int i,j;
        boolean result;
        AbstractEternityTile[] resultTiles;
        resultTiles = Arrays.copyOf(tiles, tiles.length);
        result = true;
        i=0;
        while (i<maxXValue && result) {
            j=0;
            while (j<maxYValue && result) {
                if (hintsBoard.getTileAt(i, j)!=null) {
                    result = tiles[i +j*maxYValue]==null;
                    resultTiles[i +j*maxYValue] = (AbstractEternityTile) hintsBoard.getTileAt(i, j);
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
        for (int i=0; i<maxXValue; i++) {
            for (int j=0; j<maxYValue; j++) {
                if (tiles[i +j*maxYValue]!=null) {
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
        while (i<maxXValue && result) {
            j=i%2; // start alternatively odd or even
            while (j<maxYValue && result) {
                result = areNeighborsMatching(i, j, tiles[i + j*maxYValue]);
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
        while (i<(maxXValue*maxYValue) && !found) {
            found = (tiles[i]==null || tiles[i].equals(AbstractEternityTiles.EternityTile0));
            i++;
        }
        return !found;
    }

    @Override
    public Set<EternityTileInterface> hasDuplicateTilesOnBoard() {
        Set<EternityTileInterface> resultTiles;
        int[] foundTiles;
        resultTiles = new HashSet<>();
        foundTiles = new int[maxXValue*maxYValue];
        for (int i = 0; i < maxXValue*maxYValue; i++) {
            foundTiles[i] = 0;
        }
        for (int i = 0; i < maxXValue*maxYValue; i++) {
            if (tiles[i] != null && !tiles[i].equals(AbstractEternityTiles.EternityTile0)) {
                foundTiles[tiles[i].getBackValue()]++;
            }
        }
        for (int i = 0; i < maxXValue*maxYValue; i++) {
            if (foundTiles[i] > 1) {
                resultTiles.add(AbstractEternityTiles.TILESSTORE.getTileAtNoCheck(i % maxXValue, i / maxYValue));
            }
        }
        return resultTiles;
    }

    @Override
    //actually returns a Set<AbstractEternityTile>
    public Set<EternityTileInterface> getTiles() {
        Set<EternityTileInterface> resultTiles;
        resultTiles = new HashSet<>();
        for (int i = 0; i < maxXValue*maxYValue; i++) {
            if (tiles[i] != null && !tiles[i].equals(AbstractEternityTiles.EternityTile0)) {
                resultTiles.add(tiles[i]);
            }
        }
        return resultTiles;
    }

    @Override
    public AbstractEternityTile[][] getTilesAsArray() {
        AbstractEternityTile[][] result;
        result = new AbstractEternityTile[maxXValue][maxYValue];
        for (int i=0; i<maxXValue*maxYValue; i++) {
            result[i%maxXValue][i/maxYValue] = tiles[i];
        }
        return result;
    }

    @Override
    //actually returns a Set<AbstractEternityTile>
    public Set<EternityTileInterface> getMissingTiles() {
        Set<EternityTileInterface> resultTiles;
        boolean[] foundTiles;
        resultTiles = new HashSet<>();
        foundTiles = new boolean[maxXValue*maxYValue];
        for (int i = 0; i<maxXValue*maxYValue; i++) {
            foundTiles[i] = false;
        }
        for (int i = 0; i<maxXValue*maxYValue; i++) {
            if (tiles[i]!=null && !tiles[i].equals(AbstractEternityTiles.EternityTile0)) {
                foundTiles[tiles[i].getBackValue()] = true;
            }
        }
        for (int i = 0; i<maxXValue*maxYValue; i++) {
            if (!foundTiles[i]) {
                resultTiles.add(AbstractEternityTiles.TILESSTORE.getTileAtNoCheck(i % maxXValue, i / maxYValue));
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
        while (i<maxXValue) {
            j=i%2; // start alternatively odd or even
            while (j<maxYValue) {
                result+=getNeighborsMatchingCount(i, j, tiles[i + j*maxYValue]);
                j=j+2;
            }
            i++;
        }
        return result;
    }

    @Override
    //needs maxXValue >= 2 and maxYValue >=2
    public boolean isWinningSolution() {
        return computeScore() == (4*2 + ((maxXValue-2)*2 + (maxXValue-2)*2)*3 + (maxXValue-2)*(maxYValue-2)*4)/2;
    }
    
    @Override
    public boolean isValid(int x, int y) {
        return (x > -1) && (x < maxXValue) && (y > -1) && (y < maxYValue);
    }

    @Override
    public boolean isCorner(int x, int y) {
        return ((x == 0) || (x == (maxXValue-1))) && ((y == 0) || (y == (maxYValue-1)));
    }

    @Override
    public boolean isBorder(int x, int y) {
        return (x == 0) || (x == (maxXValue-1)) || (y == 0) || (y == (maxYValue-1));
    }

    @Override
    public boolean isTileFree(int x, int y) {
        return tiles[x + maxYValue*y]==null || tiles[x + maxYValue * y].equals(AbstractEternityBasicPatterns.EternityBasicPatternGray);
    }

    public boolean areNeighborsMatching(int x, int y, @NotNull AbstractEternityTile eternityTile) {
        //if neighbors don't exist there is nothing to check
        boolean result;
        //left tile if any
        result = (x>0 && (tiles[(x-1) + maxYValue*y]==null || (tiles[(x-1) + maxYValue*y]!=null && eternityTile.getLeft().equals(tiles[(x-1) + maxYValue*y].getRight()))));
        //right tile if any
        result = result && (x<(maxXValue-1)) && (tiles[(x+1) + maxYValue*y]==null || (tiles[(x+1) + maxYValue*y]!=null && eternityTile.getRight().equals(tiles[(x+1) + maxYValue*y].getLeft())));
        //top tile if any
        result = result && (y>0 && (tiles[x + maxYValue*(y-1)]==null || (tiles[x + maxYValue*(y-1)]!=null && eternityTile.getTop().equals(tiles[x + maxYValue*(y-1)].getBottom()))));
        //bottom tile if any
        result = result && (y<(maxYValue-1)) && (tiles[x + maxYValue*(y+1)]==null || (tiles[x + maxYValue*(y+1)]!=null && eternityTile.getBottom().equals(tiles[x + maxYValue*(y+1)].getTop())));
        return result;
    }

    @Override
    public boolean areNeighborsMatching(int x, int y, @NotNull EternityTileInterface eternityTile) {
        //if neighbors don't exist there is nothing to check
        boolean result;
        //left tile if any
        result = (x>0 && (tiles[(x-1) + maxYValue*y]==null || (tiles[(x-1) + maxYValue*y]!=null && eternityTile.getLeft().equals(tiles[(x-1) + maxYValue*y].getRight()))));
        //right tile if any
        result = result && (x<(maxXValue-1)) && (tiles[(x+1) + maxYValue*y]==null || (tiles[(x+1) + maxYValue*y]!=null && eternityTile.getRight().equals(tiles[(x+1) + maxYValue*y].getLeft())));
        //top tile if any
        result = result && (y>0 && (tiles[x + maxYValue*(y-1)]==null || (tiles[x + maxYValue*(y-1)]!=null && eternityTile.getTop().equals(tiles[x + maxYValue*(y-1)].getBottom()))));
        //bottom tile if any
        result = result && (y<(maxYValue-1)) && (tiles[x + maxYValue*(y+1)]==null || (tiles[x + maxYValue*(y+1)]!=null && eternityTile.getBottom().equals(tiles[x + maxYValue*(y+1)].getTop())));
        return result;
    }

    public int getNeighborsMatchingCount(int x, int y, @NotNull AbstractEternityTile eternityTile) {
        //if neighbors don't exist there is nothing to check
        //if tile is on border we need to check only 3 or 2 neighbors
        int result;
        result = 0;
        //left tile if any
        if (x > 0 && tiles[(x - 1) + maxYValue * y] != null && eternityTile.getLeft().equals(tiles[(x - 1) + maxYValue * y].getRight())) {
            result++;
        }
        //right tile if any
        if (x < (maxXValue-1) && tiles[(x + 1) + maxYValue * y] != null && eternityTile.getRight().equals(tiles[(x + 1) + maxYValue * y].getLeft())) {
            result++;
        }
        //top tile if any
        if (y > 0 && tiles[x + maxYValue * (y - 1)] != null && eternityTile.getTop().equals(tiles[x + maxYValue * (y - 1)].getBottom())) {
            result++;
        }
        //bottom tile if any
        if (y < (maxYValue-1) && tiles[x + maxYValue * (y + 1)] != null && eternityTile.getBottom().equals(tiles[x + maxYValue * (y + 1)].getTop())) {
            result++;
        }
        return result;
    }

    @Override
    public int getNeighborsMatchingCount(int x, int y, @NotNull EternityTileInterface eternityTile) {
        //if neighbors don't exist there is nothing to check
        //if tile is on border we need to check only 3 or 2 neighbors
        int result;
        result = 0;
        //left tile if any
        if (x > 0 && tiles[(x - 1) + maxYValue * y] != null && eternityTile.getLeft().equals(tiles[(x - 1) + maxYValue * y].getRight())) {
            result++;
        }
        //right tile if any
        if (x < (maxXValue-1) && tiles[(x + 1) + maxYValue * y] != null && eternityTile.getRight().equals(tiles[(x + 1) + maxYValue * y].getLeft())) {
            result++;
        }
        //top tile if any
        if (y > 0 && tiles[x + maxYValue * (y - 1)] != null && eternityTile.getTop().equals(tiles[x + maxYValue * (y - 1)].getBottom())) {
            result++;
        }
        //bottom tile if any
        if (y < (maxYValue-1) && tiles[x + maxYValue * (y + 1)] != null && eternityTile.getBottom().equals(tiles[x + maxYValue * (y + 1)].getTop())) {
            result++;
        }
        return result;
    }

    public boolean areBordersMatchingForBorderTile(int x, int y, @NotNull AbstractEternityTile eternityTile) {
        boolean result;
        result = ((x==0) && eternityTile.getLeft().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray)) || ((x==(maxXValue-1)) && eternityTile.getRight().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray));
        result = result && (((y==0) && eternityTile.getTop().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray)) || ((y==(maxYValue-1)) && eternityTile.getBottom().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray)));
        return result;
    }

    @Override
    public boolean areBordersMatchingForBorderTile(int x, int y, @NotNull EternityTileInterface eternityTile) {
        boolean result;
        result = ((x==0) && eternityTile.getLeft().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray)) || ((x==(maxXValue-1)) && eternityTile.getRight().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray));
        result = result && (((y==0) && eternityTile.getTop().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray)) || ((y==(maxYValue-1)) && eternityTile.getBottom().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray)));
        return result;
    }

    public boolean areBordersMatchingForInBoardTile(@NotNull AbstractEternityTile eternityTile) {
        return !eternityTile.getLeft().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray) && !eternityTile.getRight().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray)
                && !eternityTile.getTop().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray) && !eternityTile.getBottom().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray);
    }

    @Override
    public boolean areBordersMatchingForInBoardTile(@NotNull EternityTileInterface eternityTile) {
        return !eternityTile.getLeft().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray) && !eternityTile.getRight().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray)
                && !eternityTile.getTop().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray) && !eternityTile.getBottom().equals(AbstractEternityBasicPatterns.EternityBasicPatternGray);
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
        while (i<(maxXValue*maxYValue) && !found) {
            if (tiles[i]!=null) {
                 width = tiles[i].getImage().getWidth(null);
                 height = tiles[i].getImage().getHeight(null);
                 found = true;
            } else i++;
        }
        if (found) {
            result = new BufferedImage(width * maxXValue, height * maxYValue, BufferedImage.TYPE_INT_ARGB);
            paint = result.createGraphics();
            paint.setPaint(Color.WHITE);
            paint.fillRect(0, 0, width * maxXValue, height * maxYValue);
            paint.setBackground(Color.WHITE);
            for (i = 0; i < maxXValue; i++) {
                for (int j = 0; j < maxYValue; j++) {
                    if (tiles[i + j * maxYValue] != null) {
                        paint.drawImage(tiles[i + j * maxYValue].getImage(), i * width, j * height, null);
                    }
                }
            }
            paint.dispose();
        } else result = null;
        return result;
    }

    @Override
    //deep copy
    public Object clone() {
        AbstractEternityBoard result;
        try {
            result = (AbstractEternityBoard) super.clone();
        } catch (CloneNotSupportedException e) {
            result = new AbstractEternityBoard(maxXValue, maxYValue);
         }
        result.tiles = Arrays.copyOf(tiles, tiles.length);
        return result;
    }

    @Override
    public String toString() {
        StringBuffer result;
        result = new StringBuffer();
        for (int i = 0; i < ((maxXValue*maxYValue)-1); i++) {
            result.append(tiles[i].toString());
            result.append(", ");
        }
        result.append(tiles[(maxXValue*maxYValue)-1]);
        return result.toString();
    }

    public static BufferedImage resizeImage(@NotNull BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

}

