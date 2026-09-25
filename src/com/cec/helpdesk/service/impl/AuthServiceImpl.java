package com.cec.helpdesk.service.impl;

import com.cec.helpdesk.dao.UserDAO;
import com.cec.helpdesk.model.User;
import com.cec.helpdesk.service.AuthService;

/**
 * Concrete implementation of AuthService.
 * Delegates persistence to UserDAO; contains business logic only.
 */
public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public User register(User user) throws Exception {
        // Check for duplicate username
        if (userDAO.findByUsername(user.getUsername()) != null) {
            throw new Exception("Username '" + user.getUsername() + "' is already taken.");
        }
        return userDAO.create(user);
    }

    @Override
    public User login(String username, String password) throws Exception {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return null;  // user not found
        }
        // Simple password check (in production, use BCrypt)
        if (!user.getPassword().equals(password)) {
            return null;  // wrong password
        }
        return user;
    }

    @Override
    public boolean usernameExists(String username) throws Exception {
        return userDAO.findByUsername(username) != null;
    }

    @Override
    public User getUserById(int userId) throws Exception {
        return userDAO.findById(userId);
    }
}
