package com.cec.helpdesk.model;

import com.cec.helpdesk.model.enums.Role;

/**
 * Concrete subclass representing a System Administrator.
 *
 * OOP — INHERITANCE:   Extends abstract User.
 * OOP — POLYMORPHISM:  Overrides getDashboardTitle(), getPermissionLevel(),
 *                      getRoleDescription() with admin-specific logic.
 */
public class Admin extends User {

    public Admin() {
        setRole(Role.ADMIN);
    }

    public Admin(String username, String password, String fullName, String email) {
        super(username, password, fullName, email, Role.ADMIN, "Administration");
    }

    // ── polymorphic overrides ───────────────────────────────
    @Override
    public String getDashboardTitle() {
        return "Admin Control Panel — " + getFullName();
    }

    @Override
    public int getPermissionLevel() {
        return 3;   // highest permission
    }

    @Override
    public String getRoleDescription() {
        return "Administrators have full access: manage all users, view all tickets, "
             + "oversee the Lost & Found feed, and configure system settings.";
    }
}
