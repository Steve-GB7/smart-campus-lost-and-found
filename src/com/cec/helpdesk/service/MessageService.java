package com.cec.helpdesk.service;

import com.cec.helpdesk.model.Message;
import java.util.List;

/**
 * OOP — ABSTRACTION
 *
 * Interface defining the contract for private-messaging operations.
 */
public interface MessageService {

    /**
     * Sends a private message linked to a specific post.
     */
    Message sendMessage(Message message) throws Exception;

    /**
     * Retrieves the full conversation between two users about a post.
     */
    List<Message> getConversation(int postId, int userA, int userB) throws Exception;

    /**
     * Returns all conversation threads for a user (inbox view).
     */
    List<Message> getInbox(int userId) throws Exception;
}
