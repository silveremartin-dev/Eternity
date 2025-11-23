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

import org.game.eternity2.elements.AbstractEternityBoard;
import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.EternityTileInterface;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * An Eternity II 6x6 puzzle board that can be incompletely filled.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */
public class EternityBoard6x6 extends AbstractEternityBoard {

    @Serial
    private static final long serialVersionUID = 1L;

    public final static EternityBoard6x6 emptyEternityBoard = new EternityBoard6x6() {
        @Override
        public EternityTile6x6 getTileAtNoCheck(int x, int y) {
            return EternityTiles6x6.EternityTile0;
        }

        @Override
        public void setTileAtNoCheck(int x, int y, @NotNull EternityTileInterface tile) {
            throw new UnsupportedOperationException("This is an empty Board only");
        }

        @Override
        public EternityTile6x6 getTileAt(int x, int y) {
            if ((x >= 0) && (x < 6) && (y >= 0) && (y < 6)) {
                return EternityTiles6x6.EternityTile0;
            }
            throw new IllegalArgumentException("Coordinate out of bounds");
        }

        @Override
        public boolean setTileAt(int x, int y, @NotNull EternityTileInterface tile) {
            throw new UnsupportedOperationException("This is an empty Board only");
        }
    };

    public EternityBoard6x6() {
        super(6, 6);
    }

    @Override
    public EternityTile6x6 getTileAtNoCheck(int x, int y) {
        return (EternityTile6x6) tiles[x + 6 * y];
    }

    @Override
    public EternityTile6x6 getTileAt(int x, int y) {
        if ((x >= 0) && (x < 6) && (y >= 0) && (y < 6)) {
            return (EternityTile6x6) tiles[x + 6 * y];
        }
        throw new IllegalArgumentException("Coordinate out of bounds");
    }

    @Override
    public boolean setTileAt(int x, int y, @NotNull EternityTileInterface tile) {
        boolean result = false;
        if (isValid(x, y) && isTileFree(x, y) && areNeighborsMatching(x, y, tile)) {
            if (isCorner(x, y)) {
                if (areBordersMatchingForBorderTile(x, y, tile)) {
                    tiles[x + 6 * y] = (EternityTile6x6) tile;
                    result = true;
                }
            } else {
                if (areBordersMatchingForInBoardTile(tile)) {
                    tiles[x + 6 * y] = (EternityTile6x6) tile;
                    result = true;
                }
            }
        }
        return result;
    }

    @Override
    public void putHintTilesOnBoardNoCheck(@NotNull EternityBoardInterface hintsBoard) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                EternityTile6x6 hint = (EternityTile6x6) hintsBoard.getTileAtNoCheck(i, j);
                if (hint != null) {
                    tiles[i + j * 6] = hint;
                }
            }
        }
    }

    @Override
    public int numTiles() {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            if (tiles[i] != null)
                count++;
        }
        return count;
    }

    @Override
    public Image getImage() {
        boolean found = false;
        int width = -1, height = -1;
        BufferedImage result = null;
        for (int i = 0; i < 36 && !found; i++) {
            if (tiles[i] != null) {
                width = tiles[i].getImage().getWidth(null);
                height = tiles[i].getImage().getHeight(null);
                found = true;
            }
        }
        if (found) {
            result = new BufferedImage(width * 6, height * 6, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = result.createGraphics();
            g.setPaint(Color.WHITE);
            g.fillRect(0, 0, width * 6, height * 6);
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    EternityTile6x6 t = (EternityTile6x6) tiles[i + j * 6];
                    if (t != null) {
                        g.drawImage(t.getImage(), i * width, j * height, null);
                    }
                }
            }
            g.dispose();
        }
        return result;
    }

    @Override
    public Object clone() {
        return (EternityBoard6x6) super.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 35; i++) {
            sb.append(tiles[i].toString()).append(", ");
        }
        sb.append(tiles[35]);
        return sb.toString();
    }
}
