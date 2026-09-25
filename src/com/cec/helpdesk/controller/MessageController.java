package com.cec.helpdesk.controller;

import com.cec.helpdesk.model.Message;
import com.cec.helpdesk.service.MessageService;
import com.cec.helpdesk.service.impl.MessageServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for private messaging — mediates between View and MessageService.
 */
public class MessageController {

    private final MessageService messageService = new MessageServiceImpl();

    /**
     * Sends a message from one user to another about a specific post.
     */
    public Message sendMessage(int postId, int senderId,
                               int receiverId, String content) {
        try {
            Message msg = new Message(postId, senderId, receiverId, content);
            return messageService.sendMessage(msg);
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the conversation between two users about a specific post.
     */
    public List<Message> getConversation(int postId, int userA, int userB) {
        try {
            return messageService.getConversation(postId, userA, userB);
        } catch (Exception e) {
            System.err.println("Error fetching conversation: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Returns all conversation threads for the given user (inbox).
     */
    public List<Message> getInbox(int userId) {
        try {
            return messageService.getInbox(userId);
        } catch (Exception e) {
            System.err.println("Error fetching inbox: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
