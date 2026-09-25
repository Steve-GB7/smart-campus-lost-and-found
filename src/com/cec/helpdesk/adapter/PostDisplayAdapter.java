package com.cec.helpdesk.adapter;

import com.cec.helpdesk.model.Post;

import java.text.SimpleDateFormat;

/**
 * DESIGN PATTERN — ADAPTER (concrete)
 *
 * Adapts a Post model object into a display-friendly row for the
 * Swing JTable used in the Lost & Found feed view.
 */
public class PostDisplayAdapter implements DataAdapter<Post> {

    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("dd-MMM-yyyy HH:mm");

    @Override
    public String[] getColumnNames() {
        return new String[] {
            "ID", "Type", "Category", "Title",
            "Author", "Status", "Posted On"
        };
    }

    @Override
    public Object[] toRowData(Post p) {
        return new Object[] {
            p.getPostId(),
            p.getTypeTag(),               // [LOST] or [FOUND]
            p.getCategory(),
            p.getTitle(),
            p.getAuthorName() != null ? p.getAuthorName() : "—",
            p.isResolved() ? "Resolved" : "Active",
            p.getCreatedAt() != null ? FMT.format(p.getCreatedAt()) : "—"
        };
    }

    @Override
    public String toHtml(Post p, com.cec.helpdesk.model.User currentUser) {
        String typeClass = "LOST".equalsIgnoreCase(p.getPostType().name()) ? "tag-lost" : "tag-found";
        String statusBadge = p.isResolved()
            ? "<span class=\"tag-chip tag-resolved\">RESOLVED</span>"
            : "<span class=\"tag-chip " + typeClass + "\">" + p.getTypeTag() + "</span>";
        
        String dateStr = p.getCreatedAt() != null ? FMT.format(p.getCreatedAt()) : "Recent";
        String authorStr = p.getAuthorName() != null ? escape(p.getAuthorName()) : "Anonymous";
        
        StringBuilder sb = new StringBuilder();
        sb.append("<article class=\"card post-card\" id=\"post-").append(p.getPostId()).append("\">\n");
        sb.append("  <div class=\"card-header\">\n");
        sb.append("    <div class=\"card-meta\">\n");
        sb.append("      ").append(statusBadge).append("\n");
        sb.append("      <span class=\"tag-chip\">").append(escape(p.getCategory())).append("</span>\n");
        sb.append("      <span class=\"meta-id mono\">#").append(p.getPostId()).append("</span>\n");
        sb.append("    </div>\n");
        sb.append("    <time class=\"timestamp mono\">").append(dateStr).append("</time>\n");
        sb.append("  </div>\n");
        
        sb.append("  <h3 class=\"card-title\">").append(escape(p.getTitle())).append("</h3>\n");
        sb.append("  <p class=\"card-prose post-desc\">").append(escape(p.getDescription())).append("</p>\n");

        if (p.getImagePath() != null && !p.getImagePath().trim().isEmpty()) {
            sb.append("  <div class=\"post-attachment mono\" style=\"margin-top:12px; font-size:12px; color:var(--color-driftwood);\">\n");
            sb.append("    📎 Attachment: <span class=\"mono\">").append(escape(p.getImagePath())).append("</span>\n");
            sb.append("  </div>\n");
        }

        sb.append("  <div class=\"card-footer\">\n");
        sb.append("    <div class=\"card-meta\">\n");
        sb.append("      <span>By <strong style=\"font-weight:500; color:var(--color-ink);\">").append(authorStr).append("</strong></span>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"card-actions\" style=\"display:flex; gap:8px;\">\n");

        if (currentUser != null) {
            boolean isAuthor = (currentUser.getUserId() == p.getAuthorId());
            if (isAuthor && !p.isResolved()) {
                sb.append("      <form method=\"POST\" action=\"/feed/resolve\" style=\"display:inline;\">\n");
                sb.append("        <input type=\"hidden\" name=\"postId\" value=\"").append(p.getPostId()).append("\">\n");
                sb.append("        <button type=\"submit\" class=\"btn btn-secondary\">Mark Resolved</button>\n");
                sb.append("      </form>\n");
            } else if (!isAuthor && !p.isResolved()) {
                sb.append("      <a href=\"/messages?postId=").append(p.getPostId())
                  .append("&to=").append(p.getAuthorId())
                  .append("\" class=\"btn btn-secondary\">💬 Message Author</a>\n");
            }
        }

        sb.append("    </div>\n");
        sb.append("  </div>\n");
        sb.append("</article>\n");

        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
