package com.cec.helpdesk.service;

import com.cec.helpdesk.model.Ticket;
import com.cec.helpdesk.model.enums.TicketStatus;

import java.util.List;

/**
 * OOP — ABSTRACTION
 *
 * Interface defining the contract for grievance-ticket operations.
 */
public interface TicketService {

    /**
     * Creates a new ticket and auto-routes it to the appropriate department staff.
     */
    Ticket createTicket(Ticket ticket) throws Exception;

    /**
     * Returns tickets visible to a student (only their own).
     */
    List<Ticket> getTicketsForStudent(int studentId) throws Exception;

    /**
     * Returns tickets assigned to a staff member's department.
     */
    List<Ticket> getTicketsForStaff(int staffId, String department) throws Exception;

    /**
     * Returns all tickets (admin view).
     */
    List<Ticket> getAllTickets() throws Exception;

    /**
     * Updates ticket status with optional remarks, performed by a staff member.
     */
    void updateTicketStatus(int ticketId, TicketStatus newStatus,
                            String remarks, int staffId) throws Exception;

    /**
     * Retrieves a single ticket by ID.
     */
    Ticket getTicketById(int ticketId) throws Exception;
}
