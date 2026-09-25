package com.cec.helpdesk.model.enums;

/**
 * Categories for grievance tickets — auto-routed to matching staff department.
 */
public enum TicketCategory {
    IT,
    ELECTRICAL,
    GENERAL_MAINTENANCE;

    /**
     * Maps this category to the department string used in the users table.
     */
    public String toDepartment() {
        switch (this) {
            case IT:                   return "IT";
            case ELECTRICAL:           return "Electrical";
            case GENERAL_MAINTENANCE:  return "General Maintenance";
            default:                   return "General Maintenance";
        }
    }

    /**
     * Converts a department string back to a TicketCategory.
     */
    public static TicketCategory fromDepartment(String dept) {
        if (dept == null) return GENERAL_MAINTENANCE;
        switch (dept.trim()) {
            case "IT":                    return IT;
            case "Electrical":            return ELECTRICAL;
            case "General Maintenance":   return GENERAL_MAINTENANCE;
            default:                      return GENERAL_MAINTENANCE;
        }
    }
}
