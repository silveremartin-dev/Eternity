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
package org.game.eternity2.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.game.eternity2.server.security.PasswordUtils;

/**
 * Modern JSON-based user database.
 * Replaces binary serialization with human-readable JSON format.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class JsonUserDatabase {
    private static final Logger logger = LogManager.getLogger(JsonUserDatabase.class);
    private static final String DATABASE_FILE = "users.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Map<String, UserData> users;

    public JsonUserDatabase() {
        this.users = new HashMap<>();
        load();
    }

    /**
     * Register a new user.
     *
     * @param username Username
     * @param password Password (will be hashed)
     * @return true if registered successfully
     */
    public synchronized boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }

        UserData userData = new UserData();
        userData.username = username;
        userData.passwordHash = hashPassword(password);
        userData.registeredAt = System.currentTimeMillis();
        userData.lastLogin = System.currentTimeMillis();

        users.put(username, userData);
        save();
        logger.info("User registered: {}", username);
        return true;
    }

    /**
     * Authenticate a user.
     *
     * @param username Username
     * @param password Password
     * @return true if authenticated
     */
    public synchronized boolean authenticateUser(String username, String password) {
        UserData userData = users.get(username);
        if (userData == null) {
            return false;
        }

        if (PasswordUtils.verify(password, userData.passwordHash)) {
            userData.lastLogin = System.currentTimeMillis();
            save();
            return true;
        }

        return false;
    }

    /**
     * Check if user exists.
     *
     * @param username Username
     * @return true if exists
     */
    public synchronized boolean userExists(String username) {
        return users.containsKey(username);
    }

    /**
     * Delete a user.
     *
     * @param username Username
     * @return true if deleted
     */
    public synchronized boolean deleteUser(String username) {
        if (users.remove(username) != null) {
            save();
            logger.info("User deleted: {}", username);
            return true;
        }
        return false;
    }

    /**
     * Delete user with password verification.
     *
     * @param username Username
     * @param password Password for verification
     * @return true if authenticated and deleted
     */
    public synchronized boolean deleteUser(String username, String password) {
        if (!authenticateUser(username, password)) {
            return false;
        }
        return deleteUser(username);
    }

    /**
     * Change user password.
     *
     * @param username    Username
     * @param oldPassword Old password
     * @param newPassword New password
     * @return true if changed
     */
    public synchronized boolean changePassword(String username, String oldPassword, String newPassword) {
        if (!authenticateUser(username, oldPassword)) {
            return false;
        }

        UserData userData = users.get(username);
        userData.passwordHash = hashPassword(newPassword);
        save();
        logger.info("Password changed for user: {}", username);
        return true;
    }

    private void load() {
        Path dbPath = Paths.get(DATABASE_FILE);
        if (!Files.exists(dbPath)) {
            logger.info("User database not found, creating new one");
            return;
        }

        try {
            String json = Files.readString(dbPath);
            DatabaseData data = gson.fromJson(json, DatabaseData.class);
            if (data != null && data.users != null) {
                this.users = data.users;
                logger.info("Loaded {} users from JSON database", users.size());
            }
        } catch (IOException e) {
            logger.error("Failed to load user database", e);
        }
    }

    private void save() {
        try {
            DatabaseData data = new DatabaseData();
            data.users = this.users;
            data.version = "2.0";

            String json = gson.toJson(data);
            Files.writeString(Paths.get(DATABASE_FILE), json);
        } catch (IOException e) {
            logger.error("Failed to save user database", e);
        }
    }

    private String hashPassword(String password) {
        return PasswordUtils.hash(password);
    }

    /**
     * Database container for JSON serialization.
     */
    @SuppressWarnings("unused")
    private static class DatabaseData {
        String version;
        Map<String, UserData> users;
    }

    /**
     * User data for JSON serialization.
     */
    @SuppressWarnings("unused")
    private static class UserData {
        String username;
        String passwordHash;
        long registeredAt;
        long lastLogin;
    }
}
