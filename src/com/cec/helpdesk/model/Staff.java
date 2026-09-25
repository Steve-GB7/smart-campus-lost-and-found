package com.cec.helpdesk.model;

import com.cec.helpdesk.model.enums.Role;

/**
 * Concrete subclass representing a Staff member.
 *
 * OOP — INHERITANCE:   Extends abstract User.
 * OOP — POLYMORPHISM:  Overrides getDashboardTitle(), getPermissionLevel(),
 *                      getRoleDescription() with staff-specific logic.
 */
public class Staff extends User {

    public Staff() {
        setRole(Role.STAFF);
    }

    public Staff(String username, String password, String fullName,
                 String email, String department) {
        super(username, password, fullName, email, Role.STAFF, department);
    }

    // ── polymorphic overrides ───────────────────────────────
    @Override
    public String getDashboardTitle() {
        return "Staff Dashboard [" + getDepartment() + "] — " + getFullName();
    }

    @Override
    public int getPermissionLevel() {
        return 2;
    }

    @Override
    public String getRoleDescription() {
        return "Staff members manage grievance tickets assigned to their department, "
             + "update ticket status, and add resolution remarks.";
    }
}
