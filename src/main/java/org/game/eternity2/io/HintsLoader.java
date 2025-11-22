package org.game.eternity2.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.elements.Hint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads puzzle hints from XML resource files.
 * Supports multiple board sizes: 4x4, 6x6, 12x6, 16x16.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class HintsLoader {
    private static final Logger logger = LogManager.getLogger(HintsLoader.class);

    /**
     * Load hints for a specific board size.
     *
     * @param boardSize Board size (4, 6, 12, or 16)
     * @return List of hints
     */
    public static List<Hint> loadHints(int boardSize) {
        String resourcePath = getResourcePath(boardSize);
        return loadHintsFromResource(resourcePath);
    }

    private static String getResourcePath(int boardSize) {
        return switch (boardSize) {
            case 4 -> "/xml/data/e2hints4x4.xml";
            case 6 -> "/xml/data/e2hints6x6.xml";
            case 12 -> "/xml/data/e2hints12x6.xml";
            case 16 -> "/xml/data/e2hints16x16.xml";
            default -> throw new IllegalArgumentException("Unsupported board size: " + boardSize);
        };
    }

    private static List<Hint> loadHintsFromResource(String resourcePath) {
        List<Hint> hints = new ArrayList<>();

        try (InputStream is = HintsLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.warn("Hints resource not found: {}", resourcePath);
                return hints;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList tileNodes = doc.getElementsByTagName("TILE");
            for (int i = 0; i < tileNodes.getLength(); i++) {
                Element tileElement = (Element) tileNodes.item(i);

                int number = Integer.parseInt(getElementText(tileElement, "NUMBER"));
                int x = Integer.parseInt(getElementText(tileElement, "XPOSITION"));
                int y = Integer.parseInt(getElementText(tileElement, "YPOSITION"));
                int rotation = Integer.parseInt(getElementText(tileElement, "ROTATION"));

                hints.add(new Hint(x, y, number, rotation));
            }

            logger.info("Loaded {} hints from {}", hints.size(), resourcePath);

        } catch (Exception e) {
            logger.error("Failed to load hints from {}", resourcePath, e);
        }

        return hints;
    }

    private static String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    /**
     * Get available board sizes.
     *
     * @return Array of supported board sizes
     */
    public static int[] getAvailableBoardSizes() {
        return new int[] { 4, 6, 12, 16 };
    }
}
