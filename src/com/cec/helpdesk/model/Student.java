package com.cec.helpdesk.model;

import com.cec.helpdesk.model.enums.Role;

/**
 * Concrete subclass representing a Student.
 *
 * OOP — INHERITANCE:   Extends abstract User.
 * OOP — POLYMORPHISM:  Overrides getDashboardTitle(), getPermissionLevel(),
 *                      getRoleDescription() with student-specific logic.
 */
public class Student extends User {

    public Student() {
        setRole(Role.STUDENT);
    }

    public Student(String username, String password, String fullName, String email) {
        super(username, password, fullName, email, Role.STUDENT, null);
    }

    // ── polymorphic overrides ───────────────────────────────
    @Override
    public String getDashboardTitle() {
        return "Student Dashboard — " + getFullName();
    }

    @Override
    public int getPermissionLevel() {
        return 1;
    }

    @Override
    public String getRoleDescription() {
        return "Students can create Lost & Found posts, send private messages, "
             + "create grievance tickets, and track their status.";
    }
}
