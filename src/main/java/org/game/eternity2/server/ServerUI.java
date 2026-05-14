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
package org.game.eternity2.server;

/**
 * Minimal interface that the server uses to communicate with a UI.
 * Implementations can be Swing based (ServerGUI) or JavaFX based (ServerApp).
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public interface ServerUI {
    /** Append a message to the UI log. */
    void log(String msg);

    /** Update the server running status indicator. */
    void setServerStatus(boolean running);

    /** Update the displayed client count. */
    void updateClientCount(int count);

    /** Update the display of the best board found so far. */
    void updateBestBoard(org.game.eternity2.model.BoardPrimitive board);

    /** Update the real-time throughput statistics. */
    void updateThroughput(double totalPps);
}
