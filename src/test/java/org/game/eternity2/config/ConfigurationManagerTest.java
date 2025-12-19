/*
 * Copyright 2022-2024 Silvere Martin-Michiellot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.game.eternity2.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationManager.
 */
class ConfigurationManagerTest {

    @TempDir
    Path tempDir;

    private ConfigurationManager config;
    private File configFile;

    @BeforeEach
    void setUp() {
        configFile = tempDir.resolve("test-config.properties").toFile();
        config = new ConfigurationManager(configFile.getAbsolutePath());
    }

    @Test
    void testGetStringWithDefault() {
        String value = config.getString("nonexistent.key", "defaultValue");
        assertEquals("defaultValue", value);
    }

    @Test
    void testGetIntWithDefault() {
        int value = config.getInt("nonexistent.int", 42);
        assertEquals(42, value);
    }

    @Test
    void testGetBooleanWithDefault() {
        boolean value = config.getBoolean("nonexistent.bool", true);
        assertTrue(value);
    }

    @Test
    void testSetAndGetProperty() {
        config.setProperty("test.key", "testValue");
        assertEquals("testValue", config.getString("test.key", ""));
    }

    @Test
    void testSetIntProperty() {
        config.setProperty("test.int", 123);
        assertEquals(123, config.getInt("test.int", 0));
    }

    @Test
    void testSaveAndLoad() {
        config.setProperty("persist.key", "persistValue");
        assertTrue(config.save());

        // Create new instance to load from file
        ConfigurationManager loaded = new ConfigurationManager(configFile.getAbsolutePath());
        assertEquals("persistValue", loaded.getString("persist.key", ""));
    }

    @Test
    void testGetProperties() {
        config.setProperty("prop1", "value1");
        assertNotNull(config.getProperties());
        assertEquals("value1", config.getProperties().getProperty("prop1"));
    }
}
