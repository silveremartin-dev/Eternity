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

import org.game.eternity2.model.BoardPrimitive;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Information about a user, including progress and statistics.
 * Refactored to use optimized models.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class EternityUser implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String login;
    private String password;
    private Duration computedTime;
    private List<EternityPacket> computedPackets;
    private List<Long> computedPieces;
    private List<Integer> bestScores;
    private List<BoardPrimitive> bestSolutions;
    private String hardwareArchitecture;
    private String operatingSystem;
    private String operatingSystemVersion;

    public EternityUser(@NotNull String login, @NotNull String password) {
        if (login == null || password == null) {
            throw new IllegalArgumentException("You cannot set a null login or null password.");
        }
        this.login = login;
        this.password = password;
        this.computedTime = Duration.ZERO;
        this.computedPackets = new ArrayList<>();
        this.computedPieces = new ArrayList<>();
        this.bestScores = new ArrayList<>();
        this.bestSolutions = new ArrayList<>();
        this.hardwareArchitecture = System.getProperty("os.arch");
        this.operatingSystem = System.getProperty("os.name");
        this.operatingSystemVersion = System.getProperty("os.version");
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(@NotNull String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    public Duration getComputedTime() {
        return computedTime;
    }

    public void setComputedTime(@NotNull Duration computedTime) {
        this.computedTime = computedTime;
    }

    public List<EternityPacket> getComputedPackets() {
        return computedPackets;
    }

    public void setComputedPackets(@NotNull List<EternityPacket> computedPackets) {
        this.computedPackets = computedPackets;
    }

    public List<Long> getComputedPieces() {
        return computedPieces;
    }

    public void setComputedPieces(@NotNull List<Long> computedPieces) {
        this.computedPieces = computedPieces;
    }

    public List<Integer> getBestScores() {
        return bestScores;
    }

    public void setBestScores(@NotNull List<Integer> bestScores) {
        this.bestScores = bestScores;
    }

    public List<BoardPrimitive> getBestSolutions() {
        return bestSolutions;
    }

    public void setBestSolutions(@NotNull List<BoardPrimitive> bestSolutions) {
        this.bestSolutions = bestSolutions;
    }

    public String getHardwareArchitecture() {
        return hardwareArchitecture;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getOperatingSystemVersion() {
        return operatingSystemVersion;
    }
}