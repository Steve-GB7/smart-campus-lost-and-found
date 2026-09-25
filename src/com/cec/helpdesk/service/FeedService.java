package com.cec.helpdesk.service;

import com.cec.helpdesk.model.Post;
import java.util.List;

/**
 * OOP — ABSTRACTION
 *
 * Interface defining the contract for Lost & Found feed operations.
 */
public interface FeedService {

    /**
     * Creates a new Lost or Found post.
     */
    Post createPost(Post post) throws Exception;

    /**
     * Returns the active (unresolved) public feed.
     */
    List<Post> getActiveFeed() throws Exception;

    /**
     * Returns all posts by a specific user (including resolved).
     */
    List<Post> getPostsByUser(int userId) throws Exception;

    /**
     * Returns all posts (admin view).
     */
    List<Post> getAllPosts() throws Exception;

    /**
     * Marks a post as resolved (only the author should call this).
     */
    void resolvePost(int postId, int requestingUserId) throws Exception;

    /**
     * Retrieves a single post by ID.
     */
    Post getPostById(int postId) throws Exception;
}
