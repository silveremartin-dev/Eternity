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

import org.jetbrains.annotations.NotNull;
import java.io.Serializable;

/**
 * A data packet sent through the network between the client (your PC) and the
 * server.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class EternityPacket implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Command {
        LOGIN,
        LOGOUT,
        JOB_REQUEST,
        JOB_DISPATCH,
        RESULT_SUBMISSION,
        MESSAGE,
        REGISTER,
        CHANGE_PASSWORD,
        DELETE_ACCOUNT,
        JOB_REQUEST_NEW,
        JOB_DISPATCH_NEW,
        SERVER_STATUS_REQUEST,
        SERVER_STATUS_RESPONSE,
        PUZZLE_DEFINITION,
        STATISTICS_UPDATE
    }

    private String packetId;
    private Command command;
    private EternityUser user;
    private Object payload; // Can be EternityBoard16x16, Job, String, ServerStatus, etc.
    private String status; // "OK", "ERROR", etc.

    public EternityPacket(@NotNull EternityUser user, Command command, Object payload) {
        this.packetId = java.util.UUID.randomUUID().toString();
        setUser(user);
        this.command = command;
        this.payload = payload;
    }

    public String getPacketId() {
        return packetId;
    }

    public EternityUser getUser() {
        return user;
    }

    public void setUser(@NotNull EternityUser user) {
        if (user != null) {
            this.user = user;
        } else {
            throw new IllegalArgumentException("You cannot set a null EternityUser.");
        }
    }

    public Command getCommand() {
        return command;
    }

    public Object getPayload() {
        return payload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}