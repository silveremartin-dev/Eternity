/**
 * Copyright (C) 2007 Silvere Martin-Michiellot
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.game.eternity2.server;

import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.EternityTileInterface;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Some abstract information to communicate in a reliable manner between the
 * client and the server and some statistics too.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class EternityUser implements java.io.Serializable {

    private String login;
    private String password;
    private Duration computedTime;
    private List<EternityPacket> computedPackets;
    private List<EternityTileInterface> computedTiles;
    private List<Integer> bestScores;
    private List<EternityBoardInterface> bestSolutions;
    private String hardwareArchitecture;
    private String operatingSystem;
    private String operatingSystemVersion;

    public EternityUser(@NotNull String login, @NotNull String password) {
        if ((login != null) || (password != null)) {
            this.login = login;
            this.password = password;
            computedTime = Duration.ZERO;
            computedPackets = new ArrayList<>();
            computedTiles = new ArrayList<>();
            bestScores = new ArrayList<>();
            bestSolutions = new ArrayList<>();
            hardwareArchitecture = System.getProperty("os.arch");
            operatingSystem = System.getProperty("os.name");
            operatingSystemVersion = System.getProperty("os.version");
        } else
            throw new IllegalArgumentException("You cannot set a null login or null password.");
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(@NotNull String login) {
        if (login != null) {
            this.login = login;
        } else
            throw new IllegalArgumentException("You cannot set a null login.");
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        if (password != null) {
            this.password = password;
        } else
            throw new IllegalArgumentException("You cannot set a null password.");
    }

    public Duration getComputedTime() {
        return computedTime;
    }

    public void setComputedTime(@NotNull Duration computedTime) {
        if (computedTime != null) {
            this.computedTime = computedTime;
        } else
            throw new IllegalArgumentException("You cannot set a null computed time.");
    }

    public List<EternityPacket> getComputedPackets() {
        return computedPackets;
    }

    public void setComputedPackets(@NotNull List<EternityPacket> computedPackets) {
        if (computedPackets != null) {
            this.computedPackets = computedPackets;
        } else
            throw new IllegalArgumentException("You cannot set a null computed packets array.");
    }

    public List<EternityTileInterface> getComputedTiles() {
        return computedTiles;
    }

    public void setComputedTiles(@NotNull List<EternityTileInterface> computedTiles) {
        if (computedTiles != null) {
            this.computedTiles = computedTiles;
        } else
            throw new IllegalArgumentException("You cannot set a null computed tiles array.");
    }

    public List<Integer> getBestScores() {
        return bestScores;
    }

    public void setBestScores(@NotNull List<Integer> bestScores) {
        if (bestScores != null) {
            this.bestScores = bestScores;
        } else
            throw new IllegalArgumentException("You cannot set a null best scores array.");
    }

    public List<EternityBoardInterface> getBestSolutions() {
        return bestSolutions;
    }

    public void setBestSolutions(@NotNull List<EternityBoardInterface> bestSolutions) {
        if (bestSolutions != null) {
            this.bestSolutions = bestSolutions;
        } else
            throw new IllegalArgumentException("You cannot set a null best solutions array.");
    }

    public String getHardwareArchitecture() {
        return hardwareArchitecture;
    }

    public void setHardwareArchitecture(@NotNull String hardwareArchitecture) {
        if (hardwareArchitecture != null) {
            this.hardwareArchitecture = hardwareArchitecture;
        } else
            throw new IllegalArgumentException("You cannot set a null hardware architecture.");
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(@NotNull String operatingSystem) {
        if (operatingSystem != null) {
            this.operatingSystem = operatingSystem;
        } else
            throw new IllegalArgumentException("You cannot set a null operating system.");
    }

    public String getOperatingSystemVersion() {
        return operatingSystemVersion;
    }

    public void setOperatingSystemVersion(@NotNull String operatingSystemVersion) {
        if (operatingSystemVersion != null) {
            this.operatingSystemVersion = operatingSystemVersion;
        } else
            throw new IllegalArgumentException("You cannot set a null operating system version.");
    }

}