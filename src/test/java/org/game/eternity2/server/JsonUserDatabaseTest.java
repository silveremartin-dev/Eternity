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
package org.game.eternity2.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonUserDatabase.
 */
class JsonUserDatabaseTest {

    private JsonUserDatabase database;

    @BeforeEach
    void setUp() {
        database = new JsonUserDatabase();
    }

    @Test
    void testRegisterNewUser() {
        String uniqueUser = "testuser_" + System.currentTimeMillis();
        boolean result = database.registerUser(uniqueUser, "password123");
        assertTrue(result, "Should successfully register new user");
    }

    @Test
    void testRegisterDuplicateUser() {
        database.registerUser("duplicate", "pass1");
        boolean result = database.registerUser("duplicate", "pass2");
        assertFalse(result, "Should not allow duplicate registration");
    }

    @Test
    void testAuthenticateValidCredentials() {
        database.registerUser("authtest", "secret");
        boolean result = database.authenticateUser("authtest", "secret");
        assertTrue(result, "Should authenticate with correct password");
    }

    @Test
    void testAuthenticateInvalidPassword() {
        database.registerUser("authtest2", "correct");
        boolean result = database.authenticateUser("authtest2", "wrong");
        assertFalse(result, "Should reject incorrect password");
    }

    @Test
    void testAuthenticateNonexistentUser() {
        boolean result = database.authenticateUser("nonexistent", "password");
        assertFalse(result, "Should reject nonexistent user");
    }

    @Test
    void testUserExists() {
        database.registerUser("existcheck", "pass");
        assertTrue(database.userExists("existcheck"));
        assertFalse(database.userExists("notexist"));
    }

    @Test
    void testDeleteUser() {
        database.registerUser("todelete", "pass");
        boolean deleted = database.deleteUser("todelete");
        assertTrue(deleted, "Should delete existing user");
        assertFalse(database.userExists("todelete"));
    }

    @Test
    void testDeleteUserWithPassword() {
        database.registerUser("todelete2", "mypass");
        boolean deleted = database.deleteUser("todelete2", "mypass");
        assertTrue(deleted, "Should delete with correct password");
        assertFalse(database.userExists("todelete2"));
    }

    @Test
    void testDeleteUserWithWrongPassword() {
        database.registerUser("todelete3", "correct");
        boolean deleted = database.deleteUser("todelete3", "wrong");
        assertFalse(deleted, "Should not delete with wrong password");
        assertTrue(database.userExists("todelete3"));
    }

    @Test
    void testChangePassword() {
        database.registerUser("changepass", "oldpass");
        boolean changed = database.changePassword("changepass", "oldpass", "newpass");
        assertTrue(changed, "Should change password");
        assertTrue(database.authenticateUser("changepass", "newpass"));
        assertFalse(database.authenticateUser("changepass", "oldpass"));
    }

    @Test
    void testChangePasswordWithWrongOld() {
        database.registerUser("changepass2", "correct");
        boolean changed = database.changePassword("changepass2", "wrong", "new");
        assertFalse(changed, "Should not change with wrong old password");
    }
}
