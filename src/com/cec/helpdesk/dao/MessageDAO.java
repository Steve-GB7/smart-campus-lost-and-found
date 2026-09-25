package com.cec.helpdesk.dao;

import com.cec.helpdesk.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object for Message CRUD operations.
 * Connects to MySQL via Singleton DatabaseManager, falling back to
 * in-memory storage if MySQL server is offline.
 */
public class MessageDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    // ── Resilient In-Memory Fallback ──────────────────────────────────────────
    private static final Map<Integer, Message> memMessages = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    // ── CREATE ──────────────────────────────────────────────
    public Message create(Message msg) throws SQLException {
        try {
            String sql = "INSERT INTO messages (post_id, sender_id, receiver_id, content) "
                       + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = dbManager.getConnection()
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, msg.getPostId());
                ps.setInt(2, msg.getSenderId());
                ps.setInt(3, msg.getReceiverId());
                ps.setString(4, msg.getContent());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) msg.setMessageId(keys.getInt(1));
                }
            }
            return msg;
        } catch (SQLException e) {
            msg.setMessageId(idGen.incrementAndGet());
            msg.setSentAt(new Timestamp(System.currentTimeMillis()));
            memMessages.put(msg.getMessageId(), msg);
            return msg;
        }
    }

    // ── READ ────────────────────────────────────────────────
    public List<Message> findConversation(int postId, int userA, int userB)
            throws SQLException {
        try {
            String sql = "SELECT m.*, "
                       + "  s.full_name AS sender_name, "
                       + "  r.full_name AS receiver_name "
                       + "FROM messages m "
                       + "JOIN users s ON m.sender_id   = s.user_id "
                       + "JOIN users r ON m.receiver_id = r.user_id "
                       + "WHERE m.post_id = ? "
                       + "  AND ((m.sender_id = ? AND m.receiver_id = ?) "
                       + "    OR (m.sender_id = ? AND m.receiver_id = ?)) "
                       + "ORDER BY m.sent_at ASC";
            List<Message> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, postId);
                ps.setInt(2, userA);
                ps.setInt(3, userB);
                ps.setInt(4, userB);
                ps.setInt(5, userA);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            List<Message> list = new ArrayList<>();
            for (Message m : memMessages.values()) {
                if (m.getPostId() == postId &&
                    ((m.getSenderId() == userA && m.getReceiverId() == userB) ||
                     (m.getSenderId() == userB && m.getReceiverId() == userA))) {
                    list.add(m);
                }
            }
            return list;
        }
    }

    public List<Message> findUserConversations(int userId) throws SQLException {
        return findInboxForUser(userId);
    }

    public List<Message> findInboxForUser(int userId) throws SQLException {
        try {
            String sql = "SELECT m.*, "
                       + "  s.full_name AS sender_name, "
                       + "  r.full_name AS receiver_name "
                       + "FROM messages m "
                       + "JOIN users s ON m.sender_id   = s.user_id "
                       + "JOIN users r ON m.receiver_id = r.user_id "
                       + "WHERE m.sender_id = ? OR m.receiver_id = ? "
                       + "ORDER BY m.sent_at DESC";
            List<Message> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            List<Message> list = new ArrayList<>();
            for (Message m : memMessages.values()) {
                if (m.getSenderId() == userId || m.getReceiverId() == userId) {
                    list.add(m);
                }
            }
            return list;
        }
    }

    // ── Row Mapper ──────────────────────────────────────────
    private Message mapRow(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setMessageId(rs.getInt("message_id"));
        msg.setPostId(rs.getInt("post_id"));
        msg.setSenderId(rs.getInt("sender_id"));
        msg.setReceiverId(rs.getInt("receiver_id"));
        msg.setContent(rs.getString("content"));
        msg.setSentAt(rs.getTimestamp("sent_at"));

        try {
            msg.setSenderName(rs.getString("sender_name"));
            msg.setReceiverName(rs.getString("receiver_name"));
        } catch (SQLException ignored) { }

        return msg;
    }
}
