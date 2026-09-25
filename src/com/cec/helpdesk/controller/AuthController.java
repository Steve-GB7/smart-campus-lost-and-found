package com.cec.helpdesk.controller;

import com.cec.helpdesk.model.*;
import com.cec.helpdesk.model.enums.Role;
import com.cec.helpdesk.service.AuthService;
import com.cec.helpdesk.service.impl.AuthServiceImpl;

/**
 * Controller for authentication — mediates between View and AuthService.
 * Contains NO UI code; only orchestration logic.
 */
public class AuthController {

    private final AuthService authService = new AuthServiceImpl();

    // ── currently logged-in user (session state) ────────────
    private User currentUser;

    /**
     * Attempts login. Returns the User on success, null on failure.
     */
    public User login(String username, String password) {
        try {
            currentUser = authService.login(username, password);
            return currentUser;
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Registers a new user based on the selected role.
     * @return the created User, or null on failure
     */
    public User register(String username, String password, String fullName,
                         String email, Role role, String department) {
        try {
            User user;
            switch (role) {
                case STUDENT:
                    user = new Student(username, password, fullName, email);
                    break;
                case STAFF:
                    user = new Staff(username, password, fullName, email, department);
                    break;
                case ADMIN:
                    user = new Admin(username, password, fullName, email);
                    break;
                default:
                    return null;
            }
            return authService.register(user);
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
