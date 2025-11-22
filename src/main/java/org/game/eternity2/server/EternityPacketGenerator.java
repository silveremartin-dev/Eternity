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



/**
 * This is a class used by the server to produce data packets to be computed by the client.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityPacketGenerator {

    //generate a packet not computed yet for a client given the ones that have already been computed
    //does so by providing a starting configuration with a couple tiles
    public EternityPacketGenerator() {
        super();
    }

    public EternityPacket getPacket() {
        throw new RuntimeException("Not yet implemented.");
    }

}