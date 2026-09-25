package com.cec.helpdesk.service;

import com.cec.helpdesk.model.User;

/**
 * OOP — ABSTRACTION
 *
 * Interface defining the contract for authentication and user-management
 * operations.  Any implementation must honour these signatures — decoupling
 * the controller/view from concrete persistence logic.
 */
public interface AuthService {

    /**
     * Registers a new user. Returns the created User with generated ID.
     * @throws Exception if the username or email already exists
     */
    User register(User user) throws Exception;

    /**
     * Authenticates a user by username and password.
     * @return the authenticated User, or null if credentials are invalid
     */
    User login(String username, String password) throws Exception;

    /**
     * Checks whether a username is already taken.
     */
    boolean usernameExists(String username) throws Exception;

    /**
     * Retrieves a user by their ID.
     */
    User getUserById(int userId) throws Exception;
}
