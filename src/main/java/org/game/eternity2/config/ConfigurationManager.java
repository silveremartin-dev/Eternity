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
        try (OutputStream output = new FileOutputStream(configFilePath)) {
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
