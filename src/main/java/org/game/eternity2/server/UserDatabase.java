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

package org.game.eternity2.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user accounts with persistent file-based storage.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
public class UserDatabase {
    private static final Logger logger = LogManager.getLogger(UserDatabase.class);
    private static final String DATABASE_FILE = "users.dat";

    private final Map<String, StoredUser> users;

    public UserDatabase() {
        this.users = new ConcurrentHashMap<>();
        load();
    }

    /**
     * Register a new user.
     *
     * @param login    User login
     * @param password User password
     * @return true if registration successful, false if user already exists
     */
    public synchronized boolean registerUser(String login, String password) {
        if (users.containsKey(login)) {
            logger.warn("Registration failed: user {} already exists", login);
            return false;
        }
        users.put(login, new StoredUser(login, password));
        save();
        logger.info("User {} registered successfully", login);
        return true;
    }

    /**
     * Authenticate a user.
     *
     * @param login    User login
     * @param password User password
     * @return true if authentication successful
     */
    public boolean authenticateUser(String login, String password) {
        StoredUser user = users.get(login);
        if (user != null && user.password.equals(password)) {
            logger.info("User {} authenticated successfully", login);
            return true;
        }
        logger.warn("Authentication failed for user {}", login);
        return false;
    }

    /**
     * Change user password.
     *
     * @param login       User login
     * @param oldPassword Current password
     * @param newPassword New password
     * @return true if password changed successfully
     */
    public synchronized boolean changePassword(String login, String oldPassword, String newPassword) {
        StoredUser user = users.get(login);
        if (user != null && user.password.equals(oldPassword)) {
            user.password = newPassword;
            save();
            logger.info("Password changed for user {}", login);
            return true;
        }
        logger.warn("Password change failed for user {}", login);
        return false;
    }

    /**
     * Delete a user account.
     *
     * @param login    User login
     * @param password User password
     * @return true if account deleted successfully
     */
    public synchronized boolean deleteUser(String login, String password) {
        StoredUser user = users.get(login);
        if (user != null && user.password.equals(password)) {
            users.remove(login);
            save();
            logger.info("User {} deleted", login);
            return true;
        }
        logger.warn("User deletion failed for {}", login);
        return false;
    }

    /**
     * Load users from file.
     */
    @SuppressWarnings("unchecked")
    private void load() {
        File file = new File(DATABASE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Map<String, StoredUser> loaded = (Map<String, StoredUser>) ois.readObject();
                users.putAll(loaded);
                logger.info("Loaded {} users from database", users.size());
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Failed to load user database: {}", e.getMessage());
            }
        } else {
            logger.info("No existing user database found, starting fresh");
        }
    }

    /**
     * Save users to file.
     */
    private void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATABASE_FILE))) {
            oos.writeObject(users);
            logger.debug("User database saved ({} users)", users.size());
        } catch (IOException e) {
            logger.error("Failed to save user database: {}", e.getMessage());
        }
    }

    /**
     * Stored user information.
     */
    private static class StoredUser implements Serializable {
        private static final long serialVersionUID = 1L;

        String login;
        String password;

        StoredUser(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }
}
