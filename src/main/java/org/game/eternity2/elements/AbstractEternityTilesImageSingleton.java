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

package org.game.eternity2.elements;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * A cache to hold pre generated images of the tiles.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

public class AbstractEternityTilesImageSingleton {

    private static class EternityTilesImageSingletonHolder {
        private final static AbstractEternityTilesImageSingleton instance = new AbstractEternityTilesImageSingleton();

    }

    private static Image[] tilesImage;

    protected AbstractEternityTilesImageSingleton() {
        tilesImage = new Image[AbstractEternityTiles.TILESSTORE.getXBoardSize()*AbstractEternityTiles.TILESSTORE.getYBoardSize()];
        for (int i=0; i<AbstractEternityTiles.TILESSTORE.getXBoardSize();i++) {
            for (int j=0; j<AbstractEternityTiles.TILESSTORE.getYBoardSize();j++) {
                tilesImage[i+j*AbstractEternityTiles.TILESSTORE.getYBoardSize()] = computeImageForTile(AbstractEternityTiles.TILESSTORE.getTileAt(i, j));
            }
        }
    }

    public static AbstractEternityTilesImageSingleton getInstance() {
        return EternityTilesImageSingletonHolder.instance;
    }

    public Image getImageForTile(@NotNull AbstractEternityTile eternityTile) {
        return tilesImage[eternityTile.getBackValue()];
    }

    protected Image computeImageForTile(AbstractEternityTile eternityTile) {
        BufferedImage b1, b2, b3, b4;

        b1 = rotateBufferedImageByDegrees((BufferedImage) eternityTile.getTop().getImage(),135);
        b2 = rotateBufferedImageByDegrees((BufferedImage) eternityTile.getLeft().getImage(),225);
        b3 = rotateBufferedImageByDegrees((BufferedImage) eternityTile.getBottom().getImage(),-45);
        b4 = rotateBufferedImageByDegrees((BufferedImage) eternityTile.getRight().getImage(),45);

        return combineImages(b1, b2, b3, b4);
    }

    //we assume there is 4 images, each, square and of the same size, in top, left, bottom and right order
    private BufferedImage combineImages(BufferedImage... bufferedImages) {
        int width = bufferedImages[0].getWidth();
        int height = bufferedImages[0].getHeight();
        int x,y;

        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        FastRGB pixelsImage0 = new FastRGB(bufferedImages[0]);
        for (y=0; y<height/2; y++) {
            x=y;
            while (x<(height-y)) {
                resultImage.setRGB(x, y, pixelsImage0.getRGBAsInt(x,y));
                x++;
            }
        }

        FastRGB pixelsImage1 = new FastRGB(bufferedImages[1]);
        for (x=width/2; x<width; x++) {
            y=x;
            while (y<(height-x)) {
                resultImage.setRGB(x, y, pixelsImage0.getRGBAsInt(x,y));
                y++;
            }
        }

        FastRGB pixelsImage2 = new FastRGB(bufferedImages[2]);
        for (y=height/2; y<height; y++) {
            x=height-y;
            while (x<y) {
                resultImage.setRGB(x, y, pixelsImage0.getRGBAsInt(x,y));
                x++;
            }
        }

        FastRGB pixelsImage3 = new FastRGB(bufferedImages[3]);
        for (x=0; x<width/2; x++) {
            y=x;
            while (y<(height-x)) {
                resultImage.setRGB(x, y, pixelsImage0.getRGBAsInt(x,y));
                y++;
            }
        }

        return resultImage;
    }

    //after https://stackoverflow.com/questions/37758061/rotate-a-buffered-image-in-java
    private static BufferedImage rotateBufferedImageByDegrees(BufferedImage img, double angle) {
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    /**
     * Converts an image to a buffered image.
     * @param image the image to be converted
     * @return buffered image
     */
    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        BufferedImage buff = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buff.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return buff;
    }

    //https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
    private static class FastRGB {

        private int width;
        private int height;

        private boolean hasAlphaChannel;
        private int pixelLength;
        private byte[] pixels;

        public FastRGB(BufferedImage image) {
            pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            width = image.getWidth();
            height = image.getHeight();
            hasAlphaChannel = image.getAlphaRaster() != null;
            if (hasAlphaChannel) {
                pixelLength = 4;
            } else {
                pixelLength = 3;
            }
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isHasAlphaChannel() {
            return hasAlphaChannel;
        }

        public int getPixelLength() {
            return pixelLength;
        }

        public short[] getRGBAsShorts(int x, int y) {
            int pos = (y * pixelLength * width) + (x * pixelLength);
            short rgb[] = new short[4];
            if (hasAlphaChannel)
                rgb[3] = (short) (pixels[pos++] & 0xFF); // Alpha
            rgb[2] = (short) (pixels[pos++] & 0xFF); // Blue
            rgb[1] = (short) (pixels[pos++] & 0xFF); // Green
            rgb[0] = (short) (pixels[pos++] & 0xFF); // Red
            return rgb;
        }

        public int getRGBAsInt(int x, int y) {
            int pos = (y * pixelLength * width) + (x * pixelLength);
            int argb = -16777216; // 255 alpha
            if (hasAlphaChannel)
                argb = (((int) pixels[pos++] & 0xff) << 24); // alpha
            argb += ((int) pixels[pos++] & 0xff); // blue
            argb += (((int) pixels[pos++] & 0xff) << 8); // green
            argb += (((int) pixels[pos++] & 0xff) << 16); // red
            return argb;
        }

    }

}
