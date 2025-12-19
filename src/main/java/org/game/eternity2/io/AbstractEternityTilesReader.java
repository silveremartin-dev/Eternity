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

package org.game.eternity2.io;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;
import org.game.eternity2.elements.AbstractEternityTile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A way to read the tiles of the Eternity II box set from a file.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public abstract class AbstractEternityTilesReader {
    private static final Logger logger = LogManager.getLogger(AbstractEternityTilesReader.class);

    private EternityXMLGameTiles eternityXMLGameTiles;

    public AbstractEternityTilesReader(@NotNull String path) {
        try {
            File file = new File(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(EternityXMLGameTiles.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            eternityXMLGameTiles = (EternityXMLGameTiles) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            logger.error("Failed to parse XML game tiles", e);
        }
    }

    // should throw an exception:
    // if the number (:backValue) on the tile is less than 0 or greater than the
    // getXBoardSize()*getYBoardSize()
    // if there is any duplicate number
    // if the top, right, bottom or left don't correspond to any pattern value
    public abstract @NotNull Set<? extends AbstractEternityTile> getTiles();

    public final EternityXMLGameTiles getEternityXMLGameTiles() {
        return eternityXMLGameTiles;
    }

    @XmlRootElement(name = "ETERNITY2GAMETILES")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    protected class EternityXMLGameTiles implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @XmlList
        private List<EternityXMLTile> eternityXMLTiles;

        public EternityXMLGameTiles() {
            super();
        }

        public EternityXMLGameTiles(List<EternityXMLTile> eternityXMLTiles) {
            super();
            this.eternityXMLTiles = eternityXMLTiles;
        }

        // Setters and Getters
        public List<EternityXMLTile> getEternityXMLTiles() {
            return eternityXMLTiles;
        }

        @Override
        public String toString() {
            return "EternityXMLGameTiles [eternityXMLTile=" + eternityXMLTiles.toString() + "]";
        }
    }

    @XmlRootElement(name = "TILE")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    protected class EternityXMLTile implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @XmlValue
        private Integer number;
        @XmlValue
        private Integer top;
        @XmlValue
        private Integer right;
        @XmlValue
        private Integer bottom;
        @XmlValue
        private Integer left;

        public EternityXMLTile() {
            super();
        }

        public EternityXMLTile(int number, int top, int right, int bottom, int left) {
            super();
            this.number = number;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
        }

        // Setters and Getters
        public Integer getNumber() {
            return number;
        }

        public Integer getTop() {
            return top;
        }

        public Integer getRight() {
            return right;
        }

        public Integer getBottom() {
            return bottom;
        }

        public Integer getLeft() {
            return left;
        }

        @Override
        public String toString() {
            return "EternityXMLTile [number=" + number + ", top=" + top + ", right = " + right + ", bottom = " + bottom
                    + ", left = " + left + "]";
        }
    }

}