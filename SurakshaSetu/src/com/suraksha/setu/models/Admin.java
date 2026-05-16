package com.suraksha.setu.models;

/**
 * Represents an admin user.
 * Extends User — demonstrates inheritance and method overriding.
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;
    private String adminLevel; // "SuperAdmin" or "Moderator"

    public Admin() {
        super();
        this.role       = "Admin";
        this.adminLevel = "Moderator";
    }

    public Admin(int userId, String username, String adminLevel) {
        super(userId, username, "Admin");
        this.adminLevel = adminLevel;
    }

    @Override
    public String getDisplayName() {
        return "[Admin] " + username + " (" + adminLevel + ")";
    }

    @Override
    public boolean isAdmin() { return true; }

    public String getAdminLevel()              { return adminLevel; }
    public void   setAdminLevel(String level)  { this.adminLevel = level; }
}
