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

import java.io.Serializable;

/**
 * Represents a hint for an Eternity puzzle - a tile that is pre-placed
 * at a specific position with a specific rotation.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class Hint implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int row;
    private final int col;
    private final int tileId;
    private final int rotation;

    /**
     * Create a new hint.
     *
     * @param row      Row position (0-indexed)
     * @param col      Column position (0-indexed)
     * @param tileId   ID of the tile to place
     * @param rotation Rotation of the tile (0-3)
     */
    public Hint(int row, int col, int tileId, int rotation) {
        this.row = row;
        this.col = col;
        this.tileId = tileId;
        this.rotation = rotation;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getTileId() {
        return tileId;
    }

    public int getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return String.format("Hint[(%d,%d) tile=%d rot=%d]", row, col, tileId, rotation);
    }
}
