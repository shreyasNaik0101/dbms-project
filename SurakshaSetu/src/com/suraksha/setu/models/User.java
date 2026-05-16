package com.suraksha.setu.models;

import java.io.Serializable;

/**
 * Abstract base class for all system users.
 * Demonstrates: abstract class, abstract methods, Serializable interface,
 * default + parameterized constructors, access modifiers.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int    userId;
    protected String username;
    protected String role;

    // Default constructor
    public User() {
        this.userId   = 0;
        this.username = "";
        this.role     = "Worker";
    }

    // Parameterized constructor — used by subclasses via super()
    public User(int userId, String username, String role) {
        this.userId   = userId;
        this.username = username;
        this.role     = role;
    }

    // Abstract methods — must be overridden
    public abstract String getDisplayName();
    public abstract boolean isAdmin();

    // Getters & setters
    public int    getUserId()              { return userId; }
    public void   setUserId(int userId)    { this.userId = userId; }
    public String getUsername()            { return username; }
    public void   setUsername(String u)    { this.username = u; }
    public String getRole()               { return role; }
    public void   setRole(String role)    { this.role = role; }

    @Override
    public String toString() {
        return getDisplayName() + " [" + role + "]";
    }
}
