package org.game.eternity2.elements;

import org.game.eternity2.EternityGameElementInterface;

import java.awt.*;

public interface EternityTileInterface extends EternityGameElementInterface, Cloneable {

    EternityBasicPatternInterface getTop();

    EternityBasicPatternInterface getRight();

    EternityBasicPatternInterface getBottom();

    EternityBasicPatternInterface getLeft();

    Image getImage();

    void rotateClockwise();

    void rotateCounterClockwise();

    @Override
    String toString();

    int getBackValue();

    int getRotation();
}
