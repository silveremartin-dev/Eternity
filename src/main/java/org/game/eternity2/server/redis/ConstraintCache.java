/*
 * MIT License
 *
 * Copyright (c) 2026 Silvere Martin-Michiellot, Antigravity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.game.eternity2.server.redis;

import io.lettuce.core.api.async.RedisAsyncCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Caches tile constraints in Redis to allow fast lookup of compatible tiles.
 * Indexes all tiles by their edge patterns for all 4 rotations.
 * Optimized version using primitive models.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class ConstraintCache {
    private static final Logger logger = LogManager.getLogger(ConstraintCache.class);
    private static final String KEY_PREFIX = "eternity:index:pattern:";

    private final RedisAsyncCommands<String, String> async;

    public ConstraintCache(RedisConnectionManager redisManager) {
        this.async = redisManager.async();
    }

    /**
     * Index all tiles into Redis.
     * 
     * @param pieces Array of piece primitives to index
     */
    public void indexPieces(long[] pieces) {
        try {
            int count = 0;
            for (long piece : pieces) {
                indexTile(piece);
                count++;
            }
            logger.info("Indexed {} pieces into Constraint Cache", count);
        } catch (Exception e) {
            logger.error("Failed to index pieces", e);
        }
    }

    /**
     * Legacy support for and/or wrapper for indexPieces.
     */
    public void indexBoard(BoardPrimitive board) {
        // Since we don't track all pieces in the board primitive directly easily,
        // this method assumes we index the pieces that were used to create the board.
        // In a real scenario, we'd pass the full piece set.
        // For now, let's just log a warning or do nothing if pieces aren't available.
        logger.warn("indexBoard called on ConstraintCache, better use indexPieces(long[] pieces)");
    }

    private void indexTile(long piece) {
        int id = PiecePrimitive.getId(piece);

        // We need to index all 4 rotations
        long current = piece;
        for (int r = 0; r < 4; r++) {
            indexRotation(id, PiecePrimitive.getRotation(current),
                    PiecePrimitive.getTop(current),
                    PiecePrimitive.getRight(current),
                    PiecePrimitive.getBottom(current),
                    PiecePrimitive.getLeft(current));
            current = PiecePrimitive.rotateCW(current);
        }
    }

    private void indexRotation(int tileId, int rotation, int top, int right, int bottom, int left) {
        String value = tileId + ":" + rotation;
        async.sadd(KEY_PREFIX + top + ":TOP", value);
        async.sadd(KEY_PREFIX + right + ":RIGHT", value);
        async.sadd(KEY_PREFIX + bottom + ":BOTTOM", value);
        async.sadd(KEY_PREFIX + left + ":LEFT", value);
    }

    /**
     * Find tiles that match the given edge constraints.
     */
    public List<String> findCandidates(int topPattern, int rightPattern, int bottomPattern, int leftPattern) {
        List<String> keys = new ArrayList<>();

        if (topPattern != -1)
            keys.add(KEY_PREFIX + topPattern + ":TOP");
        if (rightPattern != -1)
            keys.add(KEY_PREFIX + rightPattern + ":RIGHT");
        if (bottomPattern != -1)
            keys.add(KEY_PREFIX + bottomPattern + ":BOTTOM");
        if (leftPattern != -1)
            keys.add(KEY_PREFIX + leftPattern + ":LEFT");

        if (keys.isEmpty())
            return Collections.emptyList();

        try {
            String[] keysArray = keys.toArray(new String[0]);
            Set<String> matches = async.sinter(keysArray).get(1, TimeUnit.SECONDS);
            return new ArrayList<>(matches);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error finding candidates", e);
            return Collections.emptyList();
        }
    }
}
