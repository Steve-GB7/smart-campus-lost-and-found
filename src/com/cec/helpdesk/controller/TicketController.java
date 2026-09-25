package com.cec.helpdesk.controller;

import com.cec.helpdesk.model.Ticket;
import com.cec.helpdesk.model.enums.TicketCategory;
import com.cec.helpdesk.model.enums.TicketStatus;
import com.cec.helpdesk.service.TicketService;
import com.cec.helpdesk.service.impl.TicketServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for grievance ticketing — mediates between View and TicketService.
 */
public class TicketController {

    private final TicketService ticketService = new TicketServiceImpl();

    /**
     * Creates a new grievance ticket for a student.
     */
    public Ticket createTicket(int studentId, TicketCategory category,
                               String subject, String description) {
        try {
            Ticket ticket = new Ticket(studentId, category, subject, description);
            return ticketService.createTicket(ticket);
        } catch (Exception e) {
            System.err.println("Error creating ticket: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves tickets for a student (their own tickets only).
     */
    public List<Ticket> getStudentTickets(int studentId) {
        try {
            return ticketService.getTicketsForStudent(studentId);
        } catch (Exception e) {
            System.err.println("Error fetching student tickets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves tickets for a staff member's department.
     */
    public List<Ticket> getStaffTickets(int staffId, String department) {
        try {
            return ticketService.getTicketsForStaff(staffId, department);
        } catch (Exception e) {
            System.err.println("Error fetching staff tickets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all tickets (admin view).
     */
    public List<Ticket> getAllTickets() {
        try {
            return ticketService.getAllTickets();
        } catch (Exception e) {
            System.err.println("Error fetching all tickets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Updates ticket status (called by staff).
     */
    public boolean updateTicketStatus(int ticketId, TicketStatus status,
                                      String remarks, int staffId) {
        try {
            ticketService.updateTicketStatus(ticketId, status, remarks, staffId);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating ticket: " + e.getMessage());
            return false;
        }
    }

    public Ticket getTicketById(int ticketId) {
        try {
            return ticketService.getTicketById(ticketId);
        } catch (Exception e) {
            System.err.println("Error fetching ticket: " + e.getMessage());
            return null;
        }
    }
}
