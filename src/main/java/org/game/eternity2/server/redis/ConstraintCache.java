package org.game.eternity2.server.redis;

import io.lettuce.core.api.async.RedisAsyncCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.elements.AbstractEternityBoard;
import org.game.eternity2.elements.AbstractEternityTile;
import org.game.eternity2.elements.EternityTileInterface;

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
 */
public class ConstraintCache {
    private static final Logger logger = LogManager.getLogger(ConstraintCache.class);
    private static final String KEY_PREFIX = "eternity:index:pattern:";

    private final RedisAsyncCommands<String, String> async;

    public ConstraintCache(RedisConnectionManager redisManager) {
        this.async = redisManager.async();
    }

    /**
     * Index all tiles from the board into Redis.
     * This allows finding tiles that match specific edge constraints.
     * 
     * @param board The board containing all game tiles
     */
    public void indexBoard(AbstractEternityBoard board) {
        try {
            // Clear existing index
            // In a real scenario we might want to be more selective, but for now we rebuild

            Set<EternityTileInterface> tiles = board.getTiles();
            int count = 0;

            for (EternityTileInterface tile : tiles) {
                if (tile instanceof AbstractEternityTile) {
                    indexTile((AbstractEternityTile) tile);
                    count++;
                }
            }

            logger.info("Indexed {} tiles into Constraint Cache", count);

        } catch (Exception e) {
            logger.error("Failed to index board", e);
        }
    }

    private void indexTile(AbstractEternityTile tile) {
        int id = tile.getBackValue();

        // We need to index all 4 rotations
        // Rotation 0: Original
        indexRotation(id, 0,
                tile.getTop().getValue(),
                tile.getRight().getValue(),
                tile.getBottom().getValue(),
                tile.getLeft().getValue());

        // Rotation 1: Clockwise 90 (Top becomes Right, Left becomes Top, etc.)
        // Logic from AbstractEternityTile.rotateClockwise:
        // temp = top; top = right; right = bottom; bottom = left; left = temp; (Wait,
        // this is counter-clockwise? No, let's check source)
        // Source says: temp=top; top=right; right=bottom; bottom=left; left=temp;
        // If top becomes right, that means the pattern at 'right' moves to 'top'? No.
        // If I rotate tile clockwise:
        // The pattern that was on Left is now on Top.
        // The pattern that was on Top is now on Right.
        // The pattern that was on Right is now on Bottom.
        // The pattern that was on Bottom is now on Left.

        indexRotation(id, 1,
                tile.getLeft().getValue(), // Top
                tile.getTop().getValue(), // Right
                tile.getRight().getValue(), // Bottom
                tile.getBottom().getValue() // Left
        );

        // Rotation 2: 180
        indexRotation(id, 2,
                tile.getBottom().getValue(), // Top
                tile.getLeft().getValue(), // Right
                tile.getTop().getValue(), // Bottom
                tile.getRight().getValue() // Left
        );

        // Rotation 3: 270
        indexRotation(id, 3,
                tile.getRight().getValue(), // Top
                tile.getBottom().getValue(), // Right
                tile.getLeft().getValue(), // Bottom
                tile.getTop().getValue() // Left
        );
    }

    private void indexRotation(int tileId, int rotation, int top, int right, int bottom, int left) {
        String value = tileId + ":" + rotation;

        // Pipeline these adds? Lettuce auto-pipelines.
        async.sadd(KEY_PREFIX + top + ":TOP", value);
        async.sadd(KEY_PREFIX + right + ":RIGHT", value);
        async.sadd(KEY_PREFIX + bottom + ":BOTTOM", value);
        async.sadd(KEY_PREFIX + left + ":LEFT", value);
    }

    /**
     * Find tiles that match the given edge constraints.
     * Pass -1 for any edge constraint to ignore it (wildcard).
     * 
     * @return List of strings in format "tileId:rotation"
     */
    public List<String> findCandidates(int topPattern, int rightPattern, int bottomPattern, int leftPattern) {
        List<String> keys = new ArrayList<>();

        if (topPattern != -1)
            keys.add(KEY_PREFIX + topPattern + ":BOTTOM"); // We look for a tile whose TOP matches this pattern?
        // Wait, if we want a tile to place at (x,y), and the tile above has pattern P
        // on its bottom,
        // then our tile must have pattern P on its TOP.
        // So if the argument 'topPattern' means "The pattern required on the Top side
        // of the tile", then:
        // keys.add(KEY_PREFIX + topPattern + ":TOP");

        // Let's assume arguments are "Target Pattern ID required on that side of the
        // candidate tile".
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
