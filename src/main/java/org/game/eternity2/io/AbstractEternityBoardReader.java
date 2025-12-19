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
import org.game.eternity2.elements.AbstractEternityBoard;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A way to read a (possibly partial) solution to the Eternity II puzzles from a
 * file.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public abstract class AbstractEternityBoardReader {
    private static final Logger logger = LogManager.getLogger(AbstractEternityBoardReader.class);

    private EternityXMLGameBoard eternityXMLGameBoard;

    public AbstractEternityBoardReader(@NotNull String path) {
        try {
            File file = new File(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(EternityXMLGameBoard.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            eternityXMLGameBoard = (EternityXMLGameBoard) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            logger.error("Failed to parse XML game board", e);
        }
    }

    // should throw an exception:
    // if the number (:backValue) on the tile is less than 0 or greater than the
    // getXBoardSize()*getYBoardSize()
    // if there is any duplicate number
    // if rotation is not between 0 and 3
    // if xPosition or xPosition is not a valid position on the board
    public abstract @NotNull AbstractEternityBoard getBoard();

    public final EternityXMLGameBoard getEternityXMLGameBoard() {
        return eternityXMLGameBoard;
    }

    @XmlRootElement(name = "ETERNITY2GAMETESSELATION")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    protected class EternityXMLGameBoard implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @XmlAttribute
        private Integer xBoardSize;
        @XmlAttribute
        private Integer yBoardSize;

        @XmlList
        private List<EternityXMLTile> eternityXMLTiles;

        public EternityXMLGameBoard() {
            super();
        }

        public EternityXMLGameBoard(List<EternityXMLTile> eternityXMLTiles) {
            super();
            this.eternityXMLTiles = eternityXMLTiles;
        }

        // Setters and Getters
        public Integer getXBoardSize() {
            return xBoardSize;
        }

        public void setXBoardSize(Integer xBoardSize) {
            this.xBoardSize = xBoardSize;
        }

        public Integer getYBoardSize() {
            return yBoardSize;
        }

        public void setYBoardSize(Integer yBoardSize) {
            this.yBoardSize = yBoardSize;
        }

        public List<EternityXMLTile> getEternityXMLTiles() {
            return eternityXMLTiles;
        }

        @Override
        public String toString() {
            return "EternityXMLGameBoard [xBoardSize=" + xBoardSize + ", yBoardSize=" + yBoardSize
                    + ", eternityXMLTile=" + eternityXMLTiles.toString() + "]";
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
        private Integer xPosition;
        @XmlValue
        private Integer yPosition;
        @XmlValue
        private Integer rotation;

        public EternityXMLTile() {
            super();
        }

        public EternityXMLTile(int number, int xPosition, int yPosition, int rotation) {
            super();
            this.number = number;
            this.xPosition = xPosition;
            this.yPosition = yPosition;
            this.rotation = rotation;
        }

        // Setters and Getters
        public Integer getNumber() {
            return number;
        }

        public Integer getXPosition() {
            return xPosition;
        }

        public Integer getYPosition() {
            return yPosition;
        }

        public Integer getRotation() {
            return rotation;
        }

        @Override
        public String toString() {
            return "EternityXMLTile [number=" + number + ", xPosition=" + xPosition + ", yPosition = " + yPosition
                    + ", rotation = " + rotation + "]";
        }
    }

}