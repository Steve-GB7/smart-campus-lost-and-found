package com.cec.helpdesk.service.impl;

import com.cec.helpdesk.dao.TicketDAO;
import com.cec.helpdesk.dao.UserDAO;
import com.cec.helpdesk.model.Ticket;
import com.cec.helpdesk.model.User;
import com.cec.helpdesk.model.enums.TicketStatus;
import com.cec.helpdesk.service.TicketService;

import java.util.List;

/**
 * Concrete implementation of TicketService.
 * Handles auto-routing of tickets to matching department staff.
 */
public class TicketServiceImpl implements TicketService {

    private final TicketDAO ticketDAO = new TicketDAO();
    private final UserDAO   userDAO   = new UserDAO();

    @Override
    public Ticket createTicket(Ticket ticket) throws Exception {
        // Auto-route: find a staff member in the matching department
        String department = ticket.getCategory().toDepartment();
        List<User> staffList = userDAO.findStaffByDepartment(department);
        if (!staffList.isEmpty()) {
            // Simple round-robin: assign to the first available staff
            ticket.setStaffId(staffList.get(0).getUserId());
        }
        ticket.setStatus(TicketStatus.PENDING);
        return ticketDAO.create(ticket);
    }

    @Override
    public List<Ticket> getTicketsForStudent(int studentId) throws Exception {
        return ticketDAO.findByStudentId(studentId);
    }

    @Override
    public List<Ticket> getTicketsForStaff(int staffId, String department) throws Exception {
        return ticketDAO.findByDepartment(department);
    }

    @Override
    public List<Ticket> getAllTickets() throws Exception {
        return ticketDAO.findAll();
    }

    @Override
    public void updateTicketStatus(int ticketId, TicketStatus newStatus,
                                   String remarks, int staffId) throws Exception {
        Ticket existing = ticketDAO.findById(ticketId);
        if (existing == null) {
            throw new Exception("Ticket #" + ticketId + " not found.");
        }
        ticketDAO.updateStatus(ticketId, newStatus, remarks, staffId);
    }

    @Override
    public Ticket getTicketById(int ticketId) throws Exception {
        return ticketDAO.findById(ticketId);
    }
}
