package com.cec.helpdesk.dao;

import com.cec.helpdesk.model.*;
import com.cec.helpdesk.model.enums.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object for User CRUD operations.
 * Uses the Singleton DatabaseManager for JDBC connections, with an automatic
 * in-memory fallback store if MySQL is not currently running locally.
 */
public class UserDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    // ── Resilient In-Memory Fallback (Active when MySQL is offline) ───────────
    private static final Map<Integer, User> memUsers = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        // Pre-seeded accounts (mirrors schema.sql)
        Admin admin = new Admin("admin", "admin123", "System Administrator", "admin@cec.ac.in");
        admin.setUserId(1);
        admin.setDepartment("Administration");
        memUsers.put(1, admin);

        Staff staffIt = new Staff("staff_it", "staff123", "Prof. Suresh Kumar", "suresh@cec.ac.in", "IT");
        staffIt.setUserId(2);
        memUsers.put(2, staffIt);

        Staff staffElec = new Staff("staff_elec", "staff123", "Prof. Meera Pillai", "meera@cec.ac.in", "ELECTRICAL");
        staffElec.setUserId(3);
        memUsers.put(3, staffElec);

        Student student = new Student("student", "student123", "Ananya Sharma", "ananya@cec.ac.in");
        student.setUserId(4);
        memUsers.put(4, student);

        Student steve = new Student("steve", "steve123", "Steve Joseph", "steve@cec.ac.in");
        steve.setUserId(5);
        memUsers.put(5, steve);
    }

    // ────────────────────────────────────────────────────────
    // CREATE
    // ────────────────────────────────────────────────────────
    public User create(User user) throws SQLException {
        try {
            String sql = "INSERT INTO users (username, password, full_name, email, role, department) "
                       + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = dbManager.getConnection()
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getRole().name());
                ps.setString(6, user.getDepartment());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setUserId(keys.getInt(1));
                    }
                }
            }
            return user;
        } catch (SQLException e) {
            // In-memory fallback
            user.setUserId(idGen.incrementAndGet());
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            memUsers.put(user.getUserId(), user);
            return user;
        }
    }

    // ────────────────────────────────────────────────────────
    // READ
    // ────────────────────────────────────────────────────────
    public User findById(int userId) throws SQLException {
        try {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            return memUsers.get(userId);
        }
    }

    public User findByUsername(String username) throws SQLException {
        try {
            String sql = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            for (User u : memUsers.values()) {
                if (u.getUsername().equalsIgnoreCase(username)) return u;
            }
            return null;
        }
    }

    public List<User> findByRole(Role role) throws SQLException {
        try {
            String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name";
            List<User> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, role.name());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            List<User> list = new ArrayList<>();
            for (User u : memUsers.values()) {
                if (u.getRole() == role) list.add(u);
            }
            return list;
        }
    }

    public List<User> findStaffByDepartment(String department) throws SQLException {
        try {
            String sql = "SELECT * FROM users WHERE role = 'STAFF' AND department = ?";
            List<User> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, department);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            List<User> list = new ArrayList<>();
            for (User u : memUsers.values()) {
                if (u.getRole() == Role.STAFF && department.equalsIgnoreCase(u.getDepartment())) {
                    list.add(u);
                }
            }
            return list;
        }
    }

    public List<User> findAll() throws SQLException {
        try {
            String sql = "SELECT * FROM users ORDER BY role, full_name";
            List<User> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(memUsers.values());
        }
    }

    // ────────────────────────────────────────────────────────
    // UPDATE
    // ────────────────────────────────────────────────────────
    public void update(User user) throws SQLException {
        try {
            String sql = "UPDATE users SET username=?, password=?, full_name=?, "
                       + "email=?, role=?, department=? WHERE user_id=?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getRole().name());
                ps.setString(6, user.getDepartment());
                ps.setInt(7, user.getUserId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            memUsers.put(user.getUserId(), user);
        }
    }

    // ────────────────────────────────────────────────────────
    // DELETE
    // ────────────────────────────────────────────────────────
    public void delete(int userId) throws SQLException {
        try {
            String sql = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            memUsers.remove(userId);
        }
    }

    // ────────────────────────────────────────────────────────
    // Row Mapper
    // ────────────────────────────────────────────────────────
    private User mapRow(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        User user;
        switch (role) {
            case STUDENT: user = new Student(); break;
            case STAFF:   user = new Staff();   break;
            case ADMIN:   user = new Admin();   break;
            default:      user = new Student(); break;
        }
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(role);
        user.setDepartment(rs.getString("department"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
