package com.cec.helpdesk.controller;

import com.cec.helpdesk.model.Post;
import com.cec.helpdesk.model.enums.PostType;
import com.cec.helpdesk.service.FeedService;
import com.cec.helpdesk.service.impl.FeedServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Lost & Found feed — mediates between View and FeedService.
 */
public class FeedController {

    private final FeedService feedService = new FeedServiceImpl();

    /**
     * Creates a new Lost or Found post.
     */
    public Post createPost(int authorId, PostType postType, String category,
                           String title, String description, String imagePath) {
        return createPost(authorId, postType, category, title, description, imagePath, null);
    }

    public Post createPost(int authorId, PostType postType, String category,
                           String title, String description, String imagePath, String videoPath) {
        try {
            Post post = new Post(authorId, postType, category, title, description, imagePath, videoPath);
            return feedService.createPost(post);
        } catch (Exception e) {
            System.err.println("Error creating post: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns the active public feed (unresolved posts).
     */
    public List<Post> getActiveFeed() {
        try {
            return feedService.getActiveFeed();
        } catch (Exception e) {
            System.err.println("Error fetching feed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Returns all posts by a specific user.
     */
    public List<Post> getUserPosts(int userId) {
        try {
            return feedService.getPostsByUser(userId);
        } catch (Exception e) {
            System.err.println("Error fetching user posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Returns all posts (admin view).
     */
    public List<Post> getAllPosts() {
        try {
            return feedService.getAllPosts();
        } catch (Exception e) {
            System.err.println("Error fetching all posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Marks a post as resolved (only the author can do this).
     */
    public boolean resolvePost(int postId, int requestingUserId) {
        try {
            feedService.resolvePost(postId, requestingUserId);
            return true;
        } catch (Exception e) {
            System.err.println("Error resolving post: " + e.getMessage());
            return false;
        }
    }

    public Post getPostById(int postId) {
        try {
            return feedService.getPostById(postId);
        } catch (Exception e) {
            System.err.println("Error fetching post: " + e.getMessage());
            return null;
        }
    }
}
