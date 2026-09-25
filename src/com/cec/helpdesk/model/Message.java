package com.cec.helpdesk.model;

import java.sql.Timestamp;

/**
 * Represents a private message between two users, linked to a post.
 *
 * OOP — ENCAPSULATION: All fields are private; access only via getters/setters.
 */
public class Message {

    private int messageId;
    private int postId;
    private int senderId;
    private int receiverId;
    private String content;
    private Timestamp sentAt;

    // ── transient display helpers ────────────────────────────
    private String senderName;
    private String receiverName;

    public Message() { }

    public Message(int postId, int senderId, int receiverId, String content) {
        this.postId     = postId;
        this.senderId   = senderId;
        this.receiverId = receiverId;
        this.content    = content;
    }

    // ── getters & setters ───────────────────────────────────
    public int getMessageId()                   { return messageId; }
    public void setMessageId(int messageId)     { this.messageId = messageId; }

    public int getPostId()              { return postId; }
    public void setPostId(int postId)   { this.postId = postId; }

    public int getSenderId()                { return senderId; }
    public void setSenderId(int senderId)   { this.senderId = senderId; }

    public int getReceiverId()                  { return receiverId; }
    public void setReceiverId(int receiverId)   { this.receiverId = receiverId; }

    public String getContent()                { return content; }
    public void setContent(String content)    { this.content = content; }

    public Timestamp getSentAt()              { return sentAt; }
    public void setSentAt(Timestamp sentAt)   { this.sentAt = sentAt; }

    public String getSenderName()                   { return senderName; }
    public void setSenderName(String senderName)    { this.senderName = senderName; }

    public String getReceiverName()                     { return receiverName; }
    public void setReceiverName(String receiverName)    { this.receiverName = receiverName; }

    @Override
    public String toString() {
        return String.format("[%s] %s → %s: %s",
                sentAt, senderName, receiverName, content);
    }
}
