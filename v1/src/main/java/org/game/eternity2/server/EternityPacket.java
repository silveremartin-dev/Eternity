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


import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.jetbrains.annotations.NotNull;

/**
 * A data packet sent through the network between the client (your PC) and the server.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityPacket {

    private EternityUser eternityUser;

    public EternityPacket(@NotNull EternityUser eternityUser) {
        setEternityUser(eternityUser);
    }

    public EternityUser getEternityUser() {
        return eternityUser;
    }

    public void setEternityUser(@NotNull EternityUser eternityUser) {
        if (eternityUser != null) {
            this.eternityUser = eternityUser;
        } else throw new IllegalArgumentException("You cannot set a null EternityUser.");
    }

    //send to client: seed number to start computing solutions
    public void seed() {
        throw new RuntimeException("Not yet implemented.");
    }

    //return to server: best result
    public void setEternityBoard(@NotNull EternityBoard16x16 eternityBoard) {
        throw new RuntimeException("Not yet implemented.");
    }

}