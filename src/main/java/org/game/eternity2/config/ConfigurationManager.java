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
package org.game.eternity2.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * Manages application configuration with persistent storage.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class ConfigurationManager {
    private static final Logger logger = LogManager.getLogger(ConfigurationManager.class);

    private final String configFilePath;
    private final Properties properties;

    public ConfigurationManager(String configFilePath) {
        this.configFilePath = configFilePath;
        this.properties = new Properties();
        load();
    }

    private void load() {
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                properties.load(input);
                logger.info("Configuration loaded from: {}", configFilePath);
            } catch (IOException e) {
                logger.error("Failed to load configuration: {}", e.getMessage());
            }
        } else {
            logger.info("Configuration file not found, using defaults");
        }
    }

    public boolean save() {
        File configFile = new File(configFilePath);
        File parent = configFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, "Eternity II Configuration");
            logger.info("Configuration saved to: {}", configFilePath);
            return true;
        } catch (IOException e) {
            logger.error("Failed to save configuration: {}", e.getMessage());
            return false;
        }
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer for key {}: {}", key, value);
            }
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public void setProperty(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public Properties getProperties() {
        return properties;
    }
}
