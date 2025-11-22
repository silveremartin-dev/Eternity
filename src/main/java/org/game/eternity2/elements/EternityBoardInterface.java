package org.game.eternity2.elements;

import org.game.eternity2.EternityGameElementInterface;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

public interface EternityBoardInterface extends EternityGameElementInterface, Serializable, Cloneable {

    int getXBoardSize();

    int getYBoardSize();

    //use at your own risks, same as getTileAt without the checks for speedup.
    //can return null
    EternityTileInterface getTileAtNoCheck(int x, int y);

    //use at your own risks, same as getTileAt without the checks for speedup.
    //works better by calling other methods first to check if there is no tile at this location and borders and neighbors match
    void setTileAtNoCheck(int x, int y, @NotNull EternityTileInterface tile);

    //can return null
    EternityTileInterface getTileAt(int x, int y);

    //check parameters
    //checks if tile is not already set
    //checks if this is a border tile with correct orientation to be put on the border or on the opposite that the tile has no border when it is to be put on the inside Board
    //check if new tile matches with neighbors
    //parameter tile can be null though it is preferred to use EternityBasicPatternGray
    //returns true if tile was set
    boolean setTileAt(int x, int y, @NotNull EternityTileInterface tile);

    //check for null values on border
    // doesn't check for EternityBasicPatternGray. Call areBordersCorrect() for this
    boolean areBordersComplete();

    //check only if borders are on borders
    //it is not mandatory to completely fill the corners to call this method
    //this method calls areBordersComplete()
    boolean areBordersCorrect();

    //check hint tiles (139...) are where they should
    //should be called with areHintTilesInPlace(EternityBoardReader.hintEternityBoard);
    boolean areHintTilesInPlace(@NotNull EternityBoardInterface hintsBoard);

    //returns true if the bord wasn't set for any tile where there should be an hint tile
    //should be called with areAllHintTilesFree(EternityBoardReader.hintEternityBoard);
    boolean areAllHintTilesFree(@NotNull EternityBoardInterface hintsBoard);

    //should be done before adding tiles
    //should be called with putHintTilesOnBoardNoCheck(EternityBoardReader.hintEternityBoard);
    void putHintTilesOnBoardNoCheck(@NotNull EternityBoardInterface hintsBoard);

    //should be done before adding tiles
    //return false if one or more tiles are already set
    //leaves the tiles unchanged if this is the case
    //should be called with putHintTilesOnBoard(EternityBoardReader.hintEternityBoard);
    boolean putHintTilesOnBoard(@NotNull EternityBoardInterface hintsBoard);

    //get the number of tiles on board including EternityTiles.EternityTile0
    int numTiles();

    //check that all the tiles are side to side
    boolean doAllTilesMatch();

    //check that the Board is completely filled with something else than EternityBasicPatternGray, but the tilling may still be invalid
    boolean isBoardFilled();

    //should not happen if you pick the tiles to put on board one after another from a Set of Tiles like the EternityTiles.TILESSTORE
    //returns the duplicate tiles found if any and if not an empty Set
    Set<EternityTileInterface> hasDuplicateTilesOnBoard();

    //return the Set of Tiles without their position
    Set<EternityTileInterface> getTiles();

    //return the Set of Tiles with position on board
    EternityTileInterface[][] getTilesAsArray();

    //the tiles not on the board
    Set<EternityTileInterface> getMissingTiles();

    //returns a value from 0 to 480
    //480 is a complete Board with all the tiles as they should
    //used to be worth 2 million dollars
    int computeScore();

    //outputs true if you have won
    boolean isWinningSolution();

    boolean isValid(int x, int y);

    boolean isCorner(int x, int y);

    boolean isBorder(int x, int y);

    //assume isValid called first
    boolean isTileFree(int x, int y);

    //assume isValid called first
    boolean areNeighborsMatching(int x, int y, @NotNull EternityTileInterface eternityTile);

    //assume isValid called first
    int getNeighborsMatchingCount(int x, int y, @NotNull EternityTileInterface eternityTile);

    //assume tile on border: x or y = 0 or 15
    boolean areBordersMatchingForBorderTile(int x, int y, @NotNull EternityTileInterface eternityTile);

    //assume tile not on border
    boolean areBordersMatchingForInBoardTile(@NotNull EternityTileInterface eternityTile);

    //null images are painted white
    //though we could paint them using EternityTile0
    Image getImage();

}
