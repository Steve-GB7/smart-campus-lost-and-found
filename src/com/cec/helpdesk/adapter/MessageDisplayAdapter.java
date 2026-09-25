package com.cec.helpdesk.adapter;

import com.cec.helpdesk.model.Message;

import java.text.SimpleDateFormat;

/**
 * DESIGN PATTERN — ADAPTER (concrete)
 *
 * Adapts a Message model object into a display-friendly row for the
 * inbox / conversation views in Swing.
 */
public class MessageDisplayAdapter implements DataAdapter<Message> {

    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("dd-MMM-yyyy HH:mm");

    @Override
    public String[] getColumnNames() {
        return new String[] {
            "Post #", "From", "To", "Message", "Sent At"
        };
    }

    @Override
    public Object[] toRowData(Message m) {
        return new Object[] {
            m.getPostId(),
            m.getSenderName()   != null ? m.getSenderName()   : "User#" + m.getSenderId(),
            m.getReceiverName() != null ? m.getReceiverName() : "User#" + m.getReceiverId(),
            m.getContent(),
            m.getSentAt() != null ? FMT.format(m.getSentAt()) : "—"
        };
    }

    @Override
    public String toHtml(Message m, com.cec.helpdesk.model.User currentUser) {
        boolean isOutgoing = (currentUser != null && currentUser.getUserId() == m.getSenderId());
        String bubbleClass = isOutgoing ? "bubble-out" : "bubble-in";
        String senderLabel = isOutgoing ? "You" : (m.getSenderName() != null ? escape(m.getSenderName()) : "User #" + m.getSenderId());
        String timeStr = m.getSentAt() != null ? FMT.format(m.getSentAt()) : "";

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"chat-bubble ").append(bubbleClass).append("\" id=\"msg-").append(m.getMessageId()).append("\">\n");
        sb.append("  <div class=\"chat-bubble-prose\">").append(escape(m.getContent())).append("</div>\n");
        sb.append("  <div class=\"bubble-meta mono\">\n");
        sb.append("    <span>").append(senderLabel).append("</span>\n");
        sb.append("    <span>").append(timeStr).append("</span>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
