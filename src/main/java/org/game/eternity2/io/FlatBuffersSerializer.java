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
package org.game.eternity2.io;

import com.google.flatbuffers.FlatBufferBuilder;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import org.game.eternity2.proto.Board;
import org.game.eternity2.proto.Tile;

import java.nio.ByteBuffer;

/**
 * Zero-copy serializer for BoardPrimitive using FlatBuffers.
 * Produces compact binary representations suitable for network transport
 * or high-performance checkpointing.
 *
 * @author Silvere Martin-Michiellot
 * @author Antigravity
 * @since 4.0
 */
public class FlatBuffersSerializer {

    private FlatBuffersSerializer() {}

    /**
     * Serializes a BoardPrimitive into a FlatBuffers binary byte array.
     * The result is a zero-copy ready buffer.
     */
    public static byte[] serialize(BoardPrimitive board) {
        int w = board.getWidth();
        int h = board.getHeight();
        long[] cells = board.getCells();

        FlatBufferBuilder builder = new FlatBufferBuilder(256 + cells.length * 8);

        // Build tile vector
        int[] tileOffsets = new int[cells.length];
        for (int i = 0; i < cells.length; i++) {
            long piece = cells[i];
            tileOffsets[i] = Tile.createTile(
                builder,
                (short) PiecePrimitive.getId(piece),
                (byte)  PiecePrimitive.getTop(piece),
                (byte)  PiecePrimitive.getRight(piece),
                (byte)  PiecePrimitive.getBottom(piece),
                (byte)  PiecePrimitive.getLeft(piece),
                (byte)  0  // rotation stored as 0 (canonical form)
            );
        }

        int tilesVector = Board.createTilesVector(builder, tileOffsets);
        int boardOffset = Board.createBoard(builder, (short) w, (short) h, tilesVector);
        builder.finish(boardOffset);

        ByteBuffer buf = builder.dataBuffer();
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        return bytes;
    }

    /**
     * Deserializes a FlatBuffers byte array back into a BoardPrimitive.
     */
    public static BoardPrimitive deserialize(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        Board board = Board.getRootAsBoard(buf);

        int w = board.sizeX();
        int h = board.sizeY();
        BoardPrimitive result = new BoardPrimitive(w, h);

        for (int i = 0; i < board.tilesLength(); i++) {
            Tile tile = board.tiles(i);
            if (tile == null || tile.tileId() == 0) continue;

            int x = i % w;
            int y = i / w;
            long piece = PiecePrimitive.create(
                tile.tileId(),
                tile.top(),
                tile.right(),
                tile.bottom(),
                tile.left()
            );
            result.placePiece(x, y, piece);
        }

        return result;
    }

    /**
     * Returns the serialized size in bytes for monitoring/logging.
     */
    public static int serializedSize(BoardPrimitive board) {
        return serialize(board).length;
    }
}
