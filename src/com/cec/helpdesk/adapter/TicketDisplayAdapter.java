package com.cec.helpdesk.adapter;

import com.cec.helpdesk.model.Ticket;

import java.text.SimpleDateFormat;

/**
 * DESIGN PATTERN — ADAPTER (concrete)
 *
 * Adapts a Ticket model object into a display-friendly row for the
 * Swing JTable used in the Grievance Ticketing view.
 *
 * This bridges the gap between the domain model (Ticket) and the
 * View layer's expected data format (String[]).
 */
public class TicketDisplayAdapter implements DataAdapter<Ticket> {

    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("dd-MMM-yyyy HH:mm");

    @Override
    public String[] getColumnNames() {
        return new String[] {
            "ID", "Subject", "Category", "Status",
            "Student", "Assigned To", "Created", "Remarks"
        };
    }

    @Override
    public Object[] toRowData(Ticket t) {
        return new Object[] {
            t.getTicketId(),
            t.getSubject(),
            t.getCategory().name(),
            t.getStatus().name(),
            t.getStudentName() != null ? t.getStudentName() : "—",
            t.getStaffName()   != null ? t.getStaffName()   : "Unassigned",
            t.getCreatedAt()   != null ? FMT.format(t.getCreatedAt()) : "—",
            t.getRemarks()     != null ? t.getRemarks()     : ""
        };
    }

    @Override
    public String toHtml(Ticket t, com.cec.helpdesk.model.User currentUser) {
        String statusClass;
        switch (t.getStatus()) {
            case RESOLVED:
                statusClass = "tag-resolved";
                break;
            case IN_PROGRESS:
                statusClass = "tag-progress";
                break;
            case PENDING:
            default:
                statusClass = "tag-pending";
                break;
        }

        String dateStr = t.getCreatedAt() != null ? FMT.format(t.getCreatedAt()) : "—";
        String studentStr = t.getStudentName() != null ? escape(t.getStudentName()) : "Student #" + t.getStudentId();
        String staffStr = t.getStaffName() != null ? escape(t.getStaffName()) : "Unassigned";
        
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"card ticket-card\" id=\"ticket-").append(t.getTicketId()).append("\">\n");
        sb.append("  <div class=\"card-header\">\n");
        sb.append("    <div class=\"card-meta\">\n");
        sb.append("      <span class=\"tag-chip ").append(statusClass).append("\">").append(t.getStatus().name()).append("</span>\n");
        sb.append("      <span class=\"tag-chip\">").append(t.getCategory().name()).append("</span>\n");
        sb.append("      <span class=\"meta-id mono\">#TKT-").append(t.getTicketId()).append("</span>\n");
        sb.append("    </div>\n");
        sb.append("    <time class=\"timestamp mono\">").append(dateStr).append("</time>\n");
        sb.append("  </div>\n");

        sb.append("  <h3 class=\"card-title\">").append(escape(t.getSubject())).append("</h3>\n");
        sb.append("  <p class=\"card-prose ticket-desc\">").append(escape(t.getDescription())).append("</p>\n");

        if (t.getRemarks() != null && !t.getRemarks().trim().isEmpty()) {
            sb.append("  <div style=\"margin-top:16px; padding:12px; background:var(--color-linen); border-radius:var(--radius-default); border:var(--border-hairline);\">\n");
            sb.append("    <div class=\"mono\" style=\"font-size:11px; text-transform:uppercase; color:var(--color-ash); margin-bottom:4px;\">Staff Remarks</div>\n");
            sb.append("    <p class=\"prose\" style=\"font-size:16px; margin:0;\">").append(escape(t.getRemarks())).append("</p>\n");
            sb.append("  </div>\n");
        }

        sb.append("  <div class=\"card-footer\">\n");
        sb.append("    <div class=\"card-meta\">\n");
        sb.append("      <span>Filed by: <strong style=\"font-weight:500; color:var(--color-ink);\">").append(studentStr).append("</strong></span>\n");
        sb.append("      <span>• Assigned: <strong style=\"font-weight:500; color:var(--color-ink);\">").append(staffStr).append("</strong></span>\n");
        sb.append("    </div>\n");

        // Role-based actions: Staff & Admin can update status
        if (currentUser != null && (currentUser.getRole() == com.cec.helpdesk.model.enums.Role.STAFF || currentUser.getRole() == com.cec.helpdesk.model.enums.Role.ADMIN)) {
            sb.append("    <form method=\"POST\" action=\"/tickets/update\" style=\"display:flex; gap:8px; align-items:center;\">\n");
            sb.append("      <input type=\"hidden\" name=\"ticketId\" value=\"").append(t.getTicketId()).append("\">\n");
            sb.append("      <select name=\"status\" class=\"form-select mono\" style=\"padding:4px 8px; font-size:12px;\">\n");
            sb.append("        <option value=\"PENDING\" ").append(t.getStatus() == com.cec.helpdesk.model.enums.TicketStatus.PENDING ? "selected" : "").append(">PENDING</option>\n");
            sb.append("        <option value=\"IN_PROGRESS\" ").append(t.getStatus() == com.cec.helpdesk.model.enums.TicketStatus.IN_PROGRESS ? "selected" : "").append(">IN_PROGRESS</option>\n");
            sb.append("        <option value=\"RESOLVED\" ").append(t.getStatus() == com.cec.helpdesk.model.enums.TicketStatus.RESOLVED ? "selected" : "").append(">RESOLVED</option>\n");
            sb.append("      </select>\n");
            sb.append("      <input type=\"text\" name=\"remarks\" placeholder=\"Add remarks...\" class=\"form-input\" style=\"padding:4px 8px; font-size:12px; width:160px;\">\n");
            sb.append("      <button type=\"submit\" class=\"btn btn-amber\" style=\"padding:4px 12px; font-size:12px;\">Update</button>\n");
            sb.append("    </form>\n");
        }

        sb.append("  </div>\n");
        sb.append("</div>\n");

        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
