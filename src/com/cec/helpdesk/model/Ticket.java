package com.cec.helpdesk.model;

import com.cec.helpdesk.model.enums.TicketCategory;
import com.cec.helpdesk.model.enums.TicketStatus;
import java.sql.Timestamp;

/**
 * Represents a Grievance Ticket raised by a student.
 *
 * OOP — ENCAPSULATION: All fields are private; access only via getters/setters.
 */
public class Ticket {

    private int ticketId;
    private int studentId;
    private TicketCategory category;
    private String subject;
    private String description;
    private TicketStatus status;
    private Integer staffId;        // nullable — assigned after routing
    private String remarks;         // staff remarks upon resolution
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // ── transient display helpers (populated by adapter/service) ─
    private String studentName;
    private String staffName;

    public Ticket() {
        this.status = TicketStatus.PENDING;
    }

    public Ticket(int studentId, TicketCategory category,
                  String subject, String description) {
        this.studentId   = studentId;
        this.category    = category;
        this.subject     = subject;
        this.description = description;
        this.status      = TicketStatus.PENDING;
    }

    // ── getters & setters ───────────────────────────────────
    public int getTicketId()                    { return ticketId; }
    public void setTicketId(int ticketId)       { this.ticketId = ticketId; }

    public int getStudentId()                   { return studentId; }
    public void setStudentId(int studentId)     { this.studentId = studentId; }

    public TicketCategory getCategory()                     { return category; }
    public void setCategory(TicketCategory category)        { this.category = category; }

    public String getSubject()                  { return subject; }
    public void setSubject(String subject)      { this.subject = subject; }

    public String getDescription()                    { return description; }
    public void setDescription(String description)    { this.description = description; }

    public TicketStatus getStatus()                   { return status; }
    public void setStatus(TicketStatus status)        { this.status = status; }

    public Integer getStaffId()                 { return staffId; }
    public void setStaffId(Integer staffId)     { this.staffId = staffId; }

    public String getRemarks()                  { return remarks; }
    public void setRemarks(String remarks)      { this.remarks = remarks; }

    public Timestamp getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(Timestamp createdAt)   { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt)   { this.updatedAt = updatedAt; }

    public String getStudentName()                    { return studentName; }
    public void setStudentName(String studentName)    { this.studentName = studentName; }

    public String getStaffName()                { return staffName; }
    public void setStaffName(String staffName)  { this.staffName = staffName; }

    @Override
    public String toString() {
        return String.format("#%d [%s] %s — %s", ticketId, category, subject, status);
    }
}
