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
        String uniqueUser = "testuser_" + System.nanoTime();
        boolean result = database.registerUser(uniqueUser, "password123");
        assertTrue(result, "Should successfully register new user");
    }

    @Test
    void testRegisterDuplicateUser() {
        String user = "duplicate_" + System.nanoTime();
        database.registerUser(user, "pass1");
        boolean result = database.registerUser(user, "pass2");
        assertFalse(result, "Should not allow duplicate registration");
    }

    @Test
    void testAuthenticateValidCredentials() {
        String user = "authtest_" + System.nanoTime();
        database.registerUser(user, "secret");
        boolean result = database.authenticateUser(user, "secret");
        assertTrue(result, "Should authenticate with correct password");
    }

    @Test
    void testAuthenticateInvalidPassword() {
        String user = "authtest2_" + System.nanoTime();
        database.registerUser(user, "correct");
        boolean result = database.authenticateUser(user, "wrong");
        assertFalse(result, "Should reject incorrect password");
    }

    @Test
    void testAuthenticateNonexistentUser() {
        boolean result = database.authenticateUser("nonexistent_" + System.nanoTime(), "password");
        assertFalse(result, "Should reject nonexistent user");
    }

    @Test
    void testUserExists() {
        String user = "existcheck_" + System.nanoTime();
        database.registerUser(user, "pass");
        assertTrue(database.userExists(user));
        assertFalse(database.userExists("notexist_" + System.nanoTime()));
    }

    @Test
    void testDeleteUser() {
        String user = "todelete_" + System.nanoTime();
        database.registerUser(user, "pass");
        boolean deleted = database.deleteUser(user);
        assertTrue(deleted, "Should delete existing user");
        assertFalse(database.userExists(user));
    }

    @Test
    void testDeleteUserWithPassword() {
        String user = "todelete2_" + System.nanoTime();
        database.registerUser(user, "mypass");
        boolean deleted = database.deleteUser(user, "mypass");
        assertTrue(deleted, "Should delete with correct password");
        assertFalse(database.userExists(user));
    }

    @Test
    void testDeleteUserWithWrongPassword() {
        String user = "todelete3_" + System.nanoTime();
        database.registerUser(user, "correct");
        boolean deleted = database.deleteUser(user, "wrong");
        assertFalse(deleted, "Should not delete with wrong password");
        assertTrue(database.userExists(user));
    }

    @Test
    void testChangePassword() {
        String user = "changepass_" + System.nanoTime();
        database.registerUser(user, "oldpass");
        boolean changed = database.changePassword(user, "oldpass", "newpass");
        assertTrue(changed, "Should change password");
        assertTrue(database.authenticateUser(user, "newpass"));
        assertFalse(database.authenticateUser(user, "oldpass"));
    }

    @Test
    void testChangePasswordWithWrongOld() {
        String user = "changepass2_" + System.nanoTime();
        database.registerUser(user, "correct");
        boolean changed = database.changePassword(user, "wrong", "new");
        assertFalse(changed, "Should not change with wrong old password");
    }
}
