package com.cec.helpdesk.service.impl;

import com.cec.helpdesk.dao.MessageDAO;
import com.cec.helpdesk.model.Message;
import com.cec.helpdesk.service.MessageService;

import java.util.List;

/**
 * Concrete implementation of MessageService.
 */
public class MessageServiceImpl implements MessageService {

    private final MessageDAO messageDAO = new MessageDAO();

    @Override
    public Message sendMessage(Message message) throws Exception {
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new Exception("Message content cannot be empty.");
        }
        return messageDAO.create(message);
    }

    @Override
    public List<Message> getConversation(int postId, int userA, int userB)
            throws Exception {
        return messageDAO.findConversation(postId, userA, userB);
    }

    @Override
    public List<Message> getInbox(int userId) throws Exception {
        return messageDAO.findUserConversations(userId);
    }
}
