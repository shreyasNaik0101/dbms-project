package com.suraksha.setu.services;

import com.suraksha.setu.dao.AppUserDAO;
import com.suraksha.setu.models.User;

import java.sql.SQLException;

/**
 * Authentication service — login, session management.
 * Acts as a service layer over AppUserDAO.
 */
public class AuthService {

    private final AppUserDAO userDAO = new AppUserDAO();

    // Current logged-in user (session)
    private static User currentUser = null;

    /**
     * Login with username + password.
     * Returns authenticated User (Worker or Admin) or null on failure.
     */
    public User login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) return null;
        if (password == null || password.isEmpty()) return null;

        User user = userDAO.authenticate(username.trim(), password);
        if (user != null) {
            currentUser = user;
        }
        return user;
    }

    /** Logout current user. */
    public void logout() {
        currentUser = null;
    }

    /** Get the currently logged-in user. */
    public static User getCurrentUser() {
        return currentUser;
    }

    /** Check if current user is admin. */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /** Check if a username is already taken. */
    public boolean isUsernameTaken(String username) throws SQLException {
        return userDAO.usernameExists(username);
    }

    /** Register a new worker account. */
    public void registerWorkerAccount(int workerId, String username, String password)
            throws SQLException {
        userDAO.registerUser(workerId, username, password, "Worker");
    }
}
