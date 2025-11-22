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

package org.game.eternity2.server;

import org.jetbrains.annotations.NotNull;
import java.io.Serializable;

/**
 * A data packet sent through the network between the client (your PC) and the
 * server.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
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
        SERVER_STATUS_RESPONSE
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