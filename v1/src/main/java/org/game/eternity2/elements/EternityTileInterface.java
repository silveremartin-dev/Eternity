package org.game.eternity2.elements;

import org.game.eternity2.EternityGameElementInterface;
import org.game.eternity2.elements.size16x16.EternityBasicPattern16x16;

import java.awt.*;

public interface EternityTileInterface extends EternityGameElementInterface, Cloneable {

    int getBackValue();

    EternityBasicPatternInterface getTop();

    EternityBasicPatternInterface getRight();

    EternityBasicPatternInterface getBottom();

    EternityBasicPatternInterface getLeft();

    Image getImage();

    void rotateClockwise();

    void rotateCounterClockwise();

    @Override
    String toString();
}
