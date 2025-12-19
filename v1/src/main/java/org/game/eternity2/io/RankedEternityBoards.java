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


import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.server.EternityUser;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The score board of best solutions so far. Use with a database to output statistics, currently in a flat file.
 *
 * This is done the old way but may be it would be better with FileChannel : https://stackoverflow.com/questions/22929248/how-to-use-filechannel-as-argument-to-write-object or not https://codereview.stackexchange.com/questions/76877/reading-and-writing-serializable-objects-using-nio
 *
 * Eventually, we could switch to something like https://github.com/nitrite/nitrite-java or perhaps https://github.com/objectbox/objectbox-java
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class RankedEternityBoards {

    private ArrayList<RankedEternityBoard> rankedBoards;

    public RankedEternityBoards() {
        rankedBoards = new ArrayList<>();
    }

    public void addRankedEternityBoard(@NotNull RankedEternityBoard rankedBoard) {
        rankedBoards.add(rankedBoard);
    }

    public void removeRankedEternityBoard(@NotNull RankedEternityBoard rankedBoard) {
        rankedBoards.remove(rankedBoard);
    }

    public ArrayList<RankedEternityBoard> getRankedEternityBoards() {
        return rankedBoards;
    }

    public void writeToFile(@NotNull String path) throws IOException {
        writeToFile(path, rankedBoards);
    }

    public void readFromFile(@NotNull String path) throws IOException {
        ArrayList<RankedEternityBoard> rankedBoardsResult = new ArrayList<>();
        readFromFile(path, rankedBoardsResult);
        rankedBoards = rankedBoardsResult;
    }

    public static void writeToFile(@NotNull String path, @NotNull List<RankedEternityBoard> rankedBoards) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        try {
            for (int i = 0; i < rankedBoards.size(); i++) {
                oos.writeObject(rankedBoards.get(i));
            }
        } catch (OptionalDataException e) {
            if (!e.eof)
                throw e;
        } finally {
            oos.close();
        }
    }

    public static void readFromFile(@NotNull String path, @NotNull List<RankedEternityBoard> rankedBoards) throws IOException {

        FileInputStream fis = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fis);

        try {
            while (true) {
                rankedBoards.add((RankedEternityBoard)ois.readObject());
            }
        } catch (OptionalDataException | ClassNotFoundException e) {
            throw new IOException("File does not contain (only) RankedEternityBoards.");
        } finally {
            ois.close();
        }
    }

    private class RankedEternityBoard implements Serializable {

        @Serial
        private static final long serialVersionUID =  1L;

        private EternityBoardInterface eternityBoard;
        private EternityUser user;
        private Date date;
        private int score;

        public RankedEternityBoard() {
        }

        public EternityBoardInterface getEternityBoard() {
            return eternityBoard;
        }

        public void setEternityBoard(EternityBoard16x16 eternityBoard) {
            this.eternityBoard = eternityBoard;
            score = eternityBoard.computeScore();
        }

        public EternityUser getUser() {
            return user;
        }

        public void setUser(EternityUser user) {
            this.user = user;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public int getScore() {
            return score;
        }

    }

}