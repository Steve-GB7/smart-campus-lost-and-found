package com.cec.helpdesk.dao;

import com.cec.helpdesk.model.Post;
import com.cec.helpdesk.model.enums.PostType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object for Post (Lost & Found) CRUD operations.
 * Connects to MySQL via Singleton DatabaseManager, falling back to
 * in-memory storage if MySQL server is offline.
 */
public class PostDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    // ── Resilient In-Memory Fallback ──────────────────────────────────────────
    private static final Map<Integer, Post> memPosts = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        // Pre-seeded demo notices
        Post p1 = new Post(4, PostType.LOST, "Electronics", "HP Scientific Calculator in Lab 3",
                "Dark blue casing with silver buttons, last seen on desk 14 after afternoon lab.", "calculator.jpg");
        p1.setPostId(1);
        p1.setAuthorName("Ananya Sharma");
        p1.setCreatedAt(new Timestamp(System.currentTimeMillis() - 7200000));
        memPosts.put(1, p1);

        Post p2 = new Post(2, PostType.FOUND, "Keys & Wallets", "Set of Bike Keys near College Canteen",
                "Yamaha key with a black leather keychain found on the bench outside canteen.", null);
        p2.setPostId(2);
        p2.setAuthorName("Prof. Suresh Kumar");
        p2.setCreatedAt(new Timestamp(System.currentTimeMillis() - 3600000));
        memPosts.put(2, p2);
    }

    // ── CREATE ──────────────────────────────────────────────
    public Post create(Post post) throws SQLException {
        try {
            String sql = "INSERT INTO posts (author_id, post_type, category, title, "
                       + "description, image_path, is_resolved) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = dbManager.getConnection()
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, post.getAuthorId());
                ps.setString(2, post.getPostType().name());
                ps.setString(3, post.getCategory());
                ps.setString(4, post.getTitle());
                ps.setString(5, post.getDescription());
                ps.setString(6, post.getImagePath());
                ps.setBoolean(7, post.isResolved());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) post.setPostId(keys.getInt(1));
                }
            }
            return post;
        } catch (SQLException e) {
            post.setPostId(idGen.incrementAndGet());
            post.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            memPosts.put(post.getPostId(), post);
            return post;
        }
    }

    // ── READ ────────────────────────────────────────────────
    public Post findById(int postId) throws SQLException {
        try {
            String sql = "SELECT p.*, u.full_name AS author_name "
                       + "FROM posts p JOIN users u ON p.author_id = u.user_id "
                       + "WHERE p.post_id = ?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, postId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            return memPosts.get(postId);
        }
    }

    public List<Post> findAllActive() throws SQLException {
        try {
            String sql = "SELECT p.*, u.full_name AS author_name "
                       + "FROM posts p JOIN users u ON p.author_id = u.user_id "
                       + "WHERE p.is_resolved = FALSE ORDER BY p.created_at DESC";
            List<Post> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            List<Post> active = new ArrayList<>();
            for (Post p : memPosts.values()) {
                if (!p.isResolved()) active.add(p);
            }
            return active;
        }
    }

    public List<Post> findByUser(int userId) throws SQLException {
        try {
            String sql = "SELECT p.*, u.full_name AS author_name "
                       + "FROM posts p JOIN users u ON p.author_id = u.user_id "
                       + "WHERE p.author_id = ? ORDER BY p.created_at DESC";
            List<Post> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            List<Post> list = new ArrayList<>();
            for (Post p : memPosts.values()) {
                if (p.getAuthorId() == userId) list.add(p);
            }
            return list;
        }
    }

    public List<Post> findAll() throws SQLException {
        try {
            String sql = "SELECT p.*, u.full_name AS author_name "
                       + "FROM posts p JOIN users u ON p.author_id = u.user_id "
                       + "ORDER BY p.created_at DESC";
            List<Post> list = new ArrayList<>();
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(memPosts.values());
        }
    }

    // ── UPDATE ──────────────────────────────────────────────
    public void update(Post post) throws SQLException {
        try {
            String sql = "UPDATE posts SET category=?, title=?, description=?, "
                       + "image_path=?, is_resolved=? WHERE post_id=?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, post.getCategory());
                ps.setString(2, post.getTitle());
                ps.setString(3, post.getDescription());
                ps.setString(4, post.getImagePath());
                ps.setBoolean(5, post.isResolved());
                ps.setInt(6, post.getPostId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            memPosts.put(post.getPostId(), post);
        }
    }

    public List<Post> findByAuthorId(int authorId) throws SQLException {
        return findByUser(authorId);
    }

    public void markResolved(int postId) throws SQLException {
        resolvePost(postId);
    }

    public void resolvePost(int postId) throws SQLException {
        try {
            String sql = "UPDATE posts SET is_resolved = TRUE WHERE post_id = ?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, postId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Post p = memPosts.get(postId);
            if (p != null) p.setResolved(true);
        }
    }

    // ── DELETE ──────────────────────────────────────────────
    public void delete(int postId) throws SQLException {
        try {
            String sql = "DELETE FROM posts WHERE post_id = ?";
            try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, postId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            memPosts.remove(postId);
        }
    }

    // ── Row Mapper ──────────────────────────────────────────
    private Post mapRow(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setPostId(rs.getInt("post_id"));
        post.setAuthorId(rs.getInt("author_id"));
        post.setPostType(PostType.valueOf(rs.getString("post_type")));
        post.setCategory(rs.getString("category"));
        post.setTitle(rs.getString("title"));
        post.setDescription(rs.getString("description"));
        post.setImagePath(rs.getString("image_path"));
        post.setResolved(rs.getBoolean("is_resolved"));
        post.setCreatedAt(rs.getTimestamp("created_at"));
        post.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            post.setAuthorName(rs.getString("author_name"));
        } catch (SQLException ignored) { }

        return post;
    }
}
