package com.cec.helpdesk.model;

import com.cec.helpdesk.model.enums.Role;
import java.sql.Timestamp;

/**
 * Abstract base class for all users in the system.
 *
 * OOP — INHERITANCE:  Student, Staff, and Admin extend this class.
 * OOP — ENCAPSULATION: All fields are private with controlled getters/setters.
 * OOP — POLYMORPHISM:  getDashboardTitle() and getPermissionLevel() are
 *                      abstract — each subclass provides role-specific behavior.
 */
public abstract class User {

    // ── private fields (Encapsulation) ──────────────────────
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private Role role;
    private String department;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // ── constructors ────────────────────────────────────────
    protected User() { }

    protected User(String username, String password, String fullName,
                   String email, Role role, String department) {
        this.username   = username;
        this.password   = password;
        this.fullName   = fullName;
        this.email      = email;
        this.role       = role;
        this.department = department;
    }

    // ── abstract methods (Polymorphism) ─────────────────────
    /**
     * Returns the title shown on the user's dashboard.
     * Each role provides a unique implementation.
     */
    public abstract String getDashboardTitle();

    /**
     * Returns a numeric permission level used for access-control checks.
     *   Student = 1, Staff = 2, Admin = 3
     */
    public abstract int getPermissionLevel();

    /**
     * Describes what this role can do — used in the About/Help screen.
     */
    public abstract String getRoleDescription();

    // ── getters & setters (Encapsulation) ───────────────────
    public int getUserId()              { return userId; }
    public void setUserId(int id)       { this.userId = id; }

    public String getUsername()              { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword()              { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName()              { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail()           { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole()          { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getDepartment()                { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Timestamp getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(Timestamp createdAt)    { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt()                  { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt)    { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("%s [%s] — %s", fullName, role, username);
    }
}
