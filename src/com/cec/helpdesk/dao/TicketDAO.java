package com.cec.helpdesk.dao;

import com.cec.helpdesk.model.Ticket;
import com.cec.helpdesk.model.enums.TicketCategory;
import com.cec.helpdesk.model.enums.TicketStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object for Ticket CRUD operations.
 * Supports MySQL JDBC and automatic in-memory fallback when MySQL is offline.
 */
public class TicketDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    // ── Resilient In-Memory Fallback ──────────────────────────────────────────
    private static final Map<Integer, Ticket> memTickets = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        // Pre-seeded demo tickets
        Ticket t1 = new Ticket(4, TicketCategory.IT, "Lab 2 Workstation #14 network cable damaged",
                "Ethernet port clip broken and cable constantly disconnecting during coding labs.");
        t1.setTicketId(1);
        t1.setStatus(TicketStatus.PENDING);
        t1.setStudentName("Ananya Sharma");
        t1.setCreatedAt(new Timestamp(System.currentTimeMillis() - 86400000));
        memTickets.put(1, t1);

        Ticket t2 = new Ticket(5, TicketCategory.ELECTRICAL, "Power tripping in Seminar Hall 2",
                "Projector power socket sparks when HDMI source is plugged in.");
        t2.setTicketId(2);
        t2.setStatus(TicketStatus.IN_PROGRESS);
        t2.setStudentName("Steve Joseph");
        t2.setStaffId(3);
        t2.setStaffName("Prof. Meera Pillai");
        t2.setRemarks("Electrician team assigned to inspect the circuit panel.");
        t2.setCreatedAt(new Timestamp(System.currentTimeMillis() - 43200000));
        memTickets.put(2, t2);

        Ticket t3 = new Ticket(4, TicketCategory.GENERAL_MAINTENANCE, "Broken window latch in Library reading room",
                "Window pane shakes during heavy rains; needs latch replacement.");
        t3.setTicketId(3);
        t3.setStatus(TicketStatus.RESOLVED);
        t3.setStudentName("Ananya Sharma");
        t3.setStaffId(2);
        t3.setStaffName("Prof. Suresh Kumar");
        t3.setRemarks("Replacement latch installed by carpentry team.");
        t3.setCreatedAt(new Timestamp(System.currentTimeMillis() - 172800000));
        memTickets.put(3, t3);
    }

    // ── CREATE ──────────────────────────────────────────────
    public Ticket create(Ticket ticket) throws SQLException {
        try {
            String sql = "INSERT INTO tickets (student_id, category, subject, description, status, staff_id) "
                       + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = dbManager.getConnection()
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, ticket.getStudentId());
                ps.setString(2, ticket.getCategory().name());
                ps.setString(3, ticket.getSubject());
                ps.setString(4, ticket.getDescription());
                ps.setString(5, ticket.getStatus().name());
                if (ticket.getStaffId() != null) {
                    ps.setInt(6, ticket.getStaffId());
                } else {
                    ps.setNull(6, Types.INTEGER);
                }
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) ticket.setTicketId(keys.getInt(1));
                }
            }
            return ticket;
        } catch (SQLException e) {
            ticket.setTicketId(idGen.incrementAndGet());
            ticket.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            memTickets.put(ticket.getTicketId(), ticket);
            return ticket;
        }
    }

    // ── READ ────────────────────────────────────────────────
    public Ticket findById(int ticketId) throws SQLException {
        try {
            String sql = "SELECT t.*, "
                       + "s.full_name AS student_name, "
                       + "st.full_name AS staff_name "
                       + "FROM tickets t "
                       + "LEFT JOIN users s  ON t.student_id = s.user_id "
                       + "LEFT JOIN users st ON t.staff_id   = st.user_id "
                       + "WHERE t.ticket_id = ?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, ticketId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            return memTickets.get(ticketId);
        }
    }

    public List<Ticket> findByStudentId(int studentId) throws SQLException {
        return findByStudent(studentId);
    }

    public List<Ticket> findByStudent(int studentId) throws SQLException {
        try {
            String sql = "SELECT t.*, "
                       + "s.full_name AS student_name, "
                       + "st.full_name AS staff_name "
                       + "FROM tickets t "
                       + "LEFT JOIN users s  ON t.student_id = s.user_id "
                       + "LEFT JOIN users st ON t.staff_id   = st.user_id "
                       + "WHERE t.student_id = ? ORDER BY t.created_at DESC";
            List<Ticket> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            List<Ticket> list = new ArrayList<>();
            for (Ticket t : memTickets.values()) {
                if (t.getStudentId() == studentId) list.add(t);
            }
            return list;
        }
    }

    public List<Ticket> findByDepartment(String department) throws SQLException {
        return findByDepartmentOrStaff(0, department);
    }

    public List<Ticket> findByDepartmentOrStaff(int staffId, String department) throws SQLException {
        try {
            String sql = "SELECT t.*, "
                       + "s.full_name AS student_name, "
                       + "st.full_name AS staff_name "
                       + "FROM tickets t "
                       + "LEFT JOIN users s  ON t.student_id = s.user_id "
                       + "LEFT JOIN users st ON t.staff_id   = st.user_id "
                       + "WHERE t.category = ? OR t.staff_id = ? "
                       + "ORDER BY t.created_at DESC";
            List<Ticket> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, department);
                ps.setInt(2, staffId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            List<Ticket> list = new ArrayList<>();
            for (Ticket t : memTickets.values()) {
                if (t.getCategory().name().equalsIgnoreCase(department) ||
                    (t.getStaffId() != null && t.getStaffId() == staffId)) {
                    list.add(t);
                }
            }
            return list;
        }
    }

    public List<Ticket> findAll() throws SQLException {
        try {
            String sql = "SELECT t.*, "
                       + "s.full_name AS student_name, "
                       + "st.full_name AS staff_name "
                       + "FROM tickets t "
                       + "LEFT JOIN users s  ON t.student_id = s.user_id "
                       + "LEFT JOIN users st ON t.staff_id   = st.user_id "
                       + "ORDER BY t.created_at DESC";
            List<Ticket> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(memTickets.values());
        }
    }

    // ── UPDATE ──────────────────────────────────────────────
    public void updateStatus(int ticketId, TicketStatus status, String remarks, int staffId)
            throws SQLException {
        try {
            String sql = "UPDATE tickets SET status=?, remarks=?, staff_id=? WHERE ticket_id=?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, status.name());
                ps.setString(2, remarks);
                ps.setInt(3, staffId);
                ps.setInt(4, ticketId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Ticket t = memTickets.get(ticketId);
            if (t != null) {
                t.setStatus(status);
                t.setRemarks(remarks);
                t.setStaffId(staffId);
            }
        }
    }

    public void update(Ticket ticket) throws SQLException {
        try {
            String sql = "UPDATE tickets SET category=?, subject=?, description=?, "
                       + "status=?, staff_id=?, remarks=? WHERE ticket_id=?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, ticket.getCategory().name());
                ps.setString(2, ticket.getSubject());
                ps.setString(3, ticket.getDescription());
                ps.setString(4, ticket.getStatus().name());
                if (ticket.getStaffId() != null) {
                    ps.setInt(5, ticket.getStaffId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ps.setString(6, ticket.getRemarks());
                ps.setInt(7, ticket.getTicketId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            memTickets.put(ticket.getTicketId(), ticket);
        }
    }

    public void delete(int ticketId) throws SQLException {
        try {
            String sql = "DELETE FROM tickets WHERE ticket_id = ?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, ticketId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            memTickets.remove(ticketId);
        }
    }

    // ── Row Mapper ──────────────────────────────────────────
    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setTicketId(rs.getInt("ticket_id"));
        t.setStudentId(rs.getInt("student_id"));
        t.setCategory(TicketCategory.valueOf(rs.getString("category")));
        t.setSubject(rs.getString("subject"));
        t.setDescription(rs.getString("description"));
        t.setStatus(TicketStatus.valueOf(rs.getString("status")));

        int staffId = rs.getInt("staff_id");
        if (!rs.wasNull()) t.setStaffId(staffId);

        t.setRemarks(rs.getString("remarks"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            t.setStudentName(rs.getString("student_name"));
            t.setStaffName(rs.getString("staff_name"));
        } catch (SQLException ignored) { }

        return t;
    }
}
