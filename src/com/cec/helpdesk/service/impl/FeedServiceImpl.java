package com.cec.helpdesk.service.impl;

import com.cec.helpdesk.dao.PostDAO;
import com.cec.helpdesk.model.Post;
import com.cec.helpdesk.service.FeedService;

import java.util.List;

/**
 * Concrete implementation of FeedService.
 */
public class FeedServiceImpl implements FeedService {

    private final PostDAO postDAO = new PostDAO();

    @Override
    public Post createPost(Post post) throws Exception {
        return postDAO.create(post);
    }

    @Override
    public List<Post> getActiveFeed() throws Exception {
        return postDAO.findAllActive();
    }

    @Override
    public List<Post> getPostsByUser(int userId) throws Exception {
        return postDAO.findByAuthorId(userId);
    }

    @Override
    public List<Post> getAllPosts() throws Exception {
        return postDAO.findAll();
    }

    @Override
    public void resolvePost(int postId, int requestingUserId) throws Exception {
        Post post = postDAO.findById(postId);
        if (post == null) {
            throw new Exception("Post #" + postId + " not found.");
        }
        if (post.getAuthorId() != requestingUserId) {
            throw new Exception("Only the author can mark their own post as resolved.");
        }
        postDAO.markResolved(postId);
    }

    @Override
    public Post getPostById(int postId) throws Exception {
        return postDAO.findById(postId);
    }
}
