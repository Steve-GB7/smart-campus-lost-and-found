package com.cec.helpdesk.controller;

import com.cec.helpdesk.adapter.MessageDisplayAdapter;
import com.cec.helpdesk.adapter.PostDisplayAdapter;
import com.cec.helpdesk.adapter.TicketDisplayAdapter;
import com.cec.helpdesk.model.*;
import com.cec.helpdesk.model.enums.*;
import com.cec.helpdesk.view.TemplateEngine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * Main Web Router and HTTP Dispatcher.
 *
 * Coordinates HTTP requests between the View Layer (HTML/CSS) and
 * the Controllers/Services/Adapters in adherence to MVC principles.
 */
public class WebRouter implements HttpHandler {

    private final AuthController authController = new AuthController();
    private final FeedController feedController = new FeedController();
    private final TicketController ticketController = new TicketController();
    private final MessageController messageController = new MessageController();

    private final PostDisplayAdapter postAdapter = new PostDisplayAdapter();
    private final TicketDisplayAdapter ticketAdapter = new TicketDisplayAdapter();
    private final MessageDisplayAdapter messageAdapter = new MessageDisplayAdapter();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            if (path.startsWith("/static/")) {
                handleStatic(exchange, path);
                return;
            }

            switch (path) {
                case "/":
                    handleHome(exchange);
                    break;
                case "/login":
                    if ("POST".equals(method)) handleLoginPost(exchange);
                    else handleLoginGet(exchange);
                    break;
                case "/register":
                    if ("POST".equals(method)) handleRegisterPost(exchange);
                    else handleRegisterGet(exchange);
                    break;
                case "/logout":
                    handleLogout(exchange);
                    break;
                case "/feed":
                    handleFeed(exchange);
                    break;
                case "/feed/create":
                    if ("POST".equals(method)) handleFeedCreate(exchange);
                    else HttpUtils.redirect(exchange, "/feed");
                    break;
                case "/feed/resolve":
                    if ("POST".equals(method)) handleFeedResolve(exchange);
                    else HttpUtils.redirect(exchange, "/feed");
                    break;
                case "/tickets":
                    handleTickets(exchange);
                    break;
                case "/tickets/create":
                    if ("POST".equals(method)) handleTicketCreate(exchange);
                    else HttpUtils.redirect(exchange, "/tickets");
                    break;
                case "/tickets/update":
                    if ("POST".equals(method)) handleTicketUpdate(exchange);
                    else HttpUtils.redirect(exchange, "/tickets");
                    break;
                case "/messages":
                    handleMessages(exchange);
                    break;
                case "/messages/send":
                    if ("POST".equals(method)) handleMessageSend(exchange);
                    else HttpUtils.redirect(exchange, "/messages");
                    break;
                default:
                    HttpUtils.redirect(exchange, "/feed");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorHtml = "<h1>500 Internal Server Error</h1><pre>" + e.getMessage() + "</pre>";
            HttpUtils.sendHtml(exchange, errorHtml);
        }
    }

    // ── Static Asset Serving (/static/css/style.css) ───────────
    private void handleStatic(HttpExchange exchange, String path) throws IOException {
        String relPath = "web" + path;
        File file = new File(relPath);
        if (!file.exists() || file.isDirectory()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String mime = "text/plain";
        if (path.endsWith(".css")) mime = "text/css; charset=UTF-8";
        else if (path.endsWith(".js")) mime = "application/javascript; charset=UTF-8";
        else if (path.endsWith(".png")) mime = "image/png";
        else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) mime = "image/jpeg";
        else if (path.endsWith(".svg")) mime = "image/svg+xml";

        byte[] bytes = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", mime);
        if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ── Root / Home ─────────────────────────────────────────────
    private void handleHome(HttpExchange exchange) throws IOException {
        User user = SessionManager.getUser(exchange);
        if (user != null) {
            HttpUtils.redirect(exchange, "/feed");
        } else {
            HttpUtils.redirect(exchange, "/login");
        }
    }

    // ── Authentication Handlers ────────────────────────────────
    private void handleLoginGet(HttpExchange exchange) throws IOException {
        User user = SessionManager.getUser(exchange);
        if (user != null) {
            HttpUtils.redirect(exchange, "/feed");
            return;
        }
        String content = TemplateEngine.loadTemplate("login.html");
        String html = TemplateEngine.renderPage("Sign In", "login", null,
                SessionManager.getFlashSuccess(exchange),
                SessionManager.getFlashError(exchange),
                content);
        HttpUtils.sendHtml(exchange, html);
    }

    private void handleLoginPost(HttpExchange exchange) throws IOException {
        Map<String, String> form = HttpUtils.parseFormData(exchange);
        String username = form.get("username");
        String password = form.get("password");

        User user = authController.login(username, password);
        if (user != null) {
            SessionManager.setUser(exchange, user);
            SessionManager.setFlashSuccess(exchange, "Welcome back, " + user.getFullName() + "!");
            HttpUtils.redirect(exchange, "/feed");
        } else {
            SessionManager.setFlashError(exchange, "Invalid credentials. Please verify your username and password.");
            HttpUtils.redirect(exchange, "/login");
        }
    }

    private void handleRegisterGet(HttpExchange exchange) throws IOException {
        User user = SessionManager.getUser(exchange);
        if (user != null) {
            HttpUtils.redirect(exchange, "/feed");
            return;
        }
        String content = TemplateEngine.loadTemplate("register.html");
        String html = TemplateEngine.renderPage("Create Account", "register", null,
                SessionManager.getFlashSuccess(exchange),
                SessionManager.getFlashError(exchange),
                content);
        HttpUtils.sendHtml(exchange, html);
    }

    private void handleRegisterPost(HttpExchange exchange) throws IOException {
        Map<String, String> form = HttpUtils.parseFormData(exchange);
        String username = form.get("username");
        String password = form.get("password");
        String fullName = form.get("fullName");
        String email = form.get("email");
        String roleStr = form.get("role");
        String department = form.get("department");

        Role role = Role.STUDENT;
        try {
            role = Role.valueOf(roleStr);
        } catch (Exception ignored) { }

        User newUser = authController.register(username, password, fullName, email, role, department);
        if (newUser != null) {
            SessionManager.setUser(exchange, newUser);
            SessionManager.setFlashSuccess(exchange, "Registration successful! Welcome to CEC Helpdesk.");
            HttpUtils.redirect(exchange, "/feed");
        } else {
            SessionManager.setFlashError(exchange, "Registration failed. Username or email may already be registered.");
            HttpUtils.redirect(exchange, "/register");
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        SessionManager.clearUser(exchange);
        HttpUtils.redirect(exchange, "/login");
    }

    // ── Lost & Found Feed Handlers ──────────────────────────────
    private void handleFeed(HttpExchange exchange) throws IOException {
        User currentUser = SessionManager.getUser(exchange);
        Map<String, String> params = HttpUtils.parseQueryParams(exchange);
        String typeFilter = params.get("type");

        List<Post> posts = feedController.getActiveFeed();
        if (typeFilter != null && !typeFilter.isEmpty()) {
            List<Post> filtered = new ArrayList<>();
            for (Post p : posts) {
                if (typeFilter.equalsIgnoreCase(p.getPostType().name())) {
                    filtered.add(p);
                }
            }
            posts = filtered;
        }

        StringBuilder cardsHtml = new StringBuilder();
        if (posts.isEmpty()) {
            cardsHtml.append("<div class=\"card\" style=\"grid-column:1/-1; text-align:center; padding:48px;\">\n")
                    .append("  <h3 style=\"color:var(--color-driftwood);\">No notice records currently found</h3>\n")
                    .append("  <p class=\"prose\" style=\"color:var(--color-mist); margin-top:8px;\">Misplaced or discovered something? Click '+ Post Notice' above to report.</p>\n")
                    .append("</div>\n");
        } else {
            for (Post post : posts) {
                // Adapts domain entity into Warm Parchment card via DataAdapter
                cardsHtml.append(postAdapter.toHtml(post, currentUser)).append("\n");
            }
        }

        String tmpl = TemplateEngine.loadTemplate("feed.html");
        Map<String, String> vars = new HashMap<>();
        vars.put("cardsHtml", cardsHtml.toString());
        vars.put("postCount", String.valueOf(posts.size()));
        vars.put("filterAllActive", (typeFilter == null || typeFilter.isEmpty()) ? "btn-primary" : "");
        vars.put("filterLostActive", "LOST".equalsIgnoreCase(typeFilter) ? "btn-primary" : "");
        vars.put("filterFoundActive", "FOUND".equalsIgnoreCase(typeFilter) ? "btn-primary" : "");

        if (currentUser != null) {
            vars.put("postButton", "<button type=\"button\" class=\"btn btn-primary\" onclick=\"openModal('postModal')\">+ Post Notice</button>");
        } else {
            vars.put("postButton", "<a href=\"/login\" class=\"btn btn-primary\">Sign In to Post</a>");
        }

        String content = TemplateEngine.applyTokens(tmpl, vars);
        String html = TemplateEngine.renderPage("Lost & Found Feed", "feed", currentUser,
                SessionManager.getFlashSuccess(exchange),
                SessionManager.getFlashError(exchange),
                content);
        HttpUtils.sendHtml(exchange, html);
    }

    private void handleFeedCreate(HttpExchange exchange) throws IOException {
        User currentUser = SessionManager.getUser(exchange);
        if (currentUser == null) {
            HttpUtils.redirect(exchange, "/login");
            return;
        }

        Map<String, String> form = HttpUtils.parseFormData(exchange);
        String postTypeStr = form.get("postType");
        String category = form.get("category");
        String title = form.get("title");
        String description = form.get("description");
        String imagePath = form.get("imagePath");

        PostType postType = "LOST".equalsIgnoreCase(postTypeStr) ? PostType.LOST : PostType.FOUND;

        Post created = feedController.createPost(currentUser.getUserId(), postType, category, title, description, imagePath);
        if (created != null) {
            SessionManager.setFlashSuccess(exchange, "Notice published successfully to the campus feed!");
        } else {
            SessionManager.setFlashError(exchange, "Failed to publish notice. Please verify all required fields.");
        }
        HttpUtils.redirect(exchange, "/feed");
    }

    private void handleFeedResolve(HttpExchange exchange) throws IOException {
        User currentUser = SessionManager.getUser(exchange);
        if (currentUser == null) {
            HttpUtils.redirect(exchange, "/login");
            return;
        }

        Map<String, String> form = HttpUtils.parseFormData(exchange);
        try {
            int postId = Integer.parseInt(form.get("postId"));
            boolean ok = feedController.resolvePost(postId, currentUser.getUserId());
            if (ok) {
                SessionManager.setFlashSuccess(exchange, "Notice marked as RESOLVED.");
            } else {
                SessionManager.setFlashError(exchange, "Unable to resolve notice. Only the author can perform this action.");
            }
        } catch (Exception e) {
            SessionManager.setFlashError(exchange, "Invalid notice request.");
        }
        HttpUtils.redirect(exchange, "/feed");
    }

    // ── Grievance Tickets Handlers ──────────────────────────────
    private void handleTickets(HttpExchange exchange) throws IOException {
        User currentUser = SessionManager.getUser(exchange);
        if (currentUser == null) {
            SessionManager.setFlashError(exchange, "Please sign in to access the Grievance Ticketing portal.");
            HttpUtils.redirect(exchange, "/login");
            return;
        }

        List<Ticket> tickets;
        String subtitle;

        // Polymorphic role-based dashboard retrieval
        if (currentUser.getRole() == Role.STUDENT) {
            tickets = ticketController.getStudentTickets(currentUser.getUserId());
            subtitle = "Track and manage grievances you have submitted to college administration and department maintenance.";
        } else if (currentUser.getRole() == Role.STAFF) {
            Staff staff = (Staff) currentUser;
            tickets = ticketController.getStaffTickets(staff.getUserId(), staff.getDepartment());
            subtitle = "Review and address grievances auto-routed to your department (" + staff.getDepartment() + ").";
        } else {
            tickets = ticketController.getAllTickets();
            subtitle = "Campus-wide grievance registry across IT, Electrical, and Facilities.";
        }

        int pendingCount = 0;
        int progressCount = 0;
        int resolvedCount = 0;

        StringBuilder listHtml = new StringBuilder();
        if (tickets.isEmpty()) {
            listHtml.append("<div class=\"card\" style=\"text-align:center; padding:48px;\">\n")
                    .append("  <h3 style=\"color:var(--color-driftwood);\">No tickets in this view</h3>\n")
                    .append("  <p class=\"prose\" style=\"color:var(--color-mist); margin-top:8px;\">Everything appears clear!</p>\n")
                    .append("</div>\n");
        } else {
            for (Ticket t : tickets) {
                if (t.getStatus() == TicketStatus.PENDING) pendingCount++;
                else if (t.getStatus() == TicketStatus.IN_PROGRESS) progressCount++;
                else if (t.getStatus() == TicketStatus.RESOLVED) resolvedCount++;

                listHtml.append(ticketAdapter.toHtml(t, currentUser)).append("\n");
            }
        }

        String tmpl = TemplateEngine.loadTemplate("tickets.html");
        Map<String, String> vars = new HashMap<>();
        // Polymorphism in action: getDashboardTitle()
        vars.put("dashboardTitle", currentUser.getDashboardTitle());
        vars.put("dashboardSubtitle", subtitle);
        vars.put("pendingCount", String.valueOf(pendingCount));
        vars.put("progressCount", String.valueOf(progressCount));
        vars.put("resolvedCount", String.valueOf(resolvedCount));
        vars.put("totalTicketCount", String.valueOf(tickets.size()));
        vars.put("ticketListHtml", listHtml.toString());

        if (currentUser.getRole() == Role.STUDENT) {
            vars.put("createTicketButton", "<button type=\"button\" class=\"btn btn-primary\" onclick=\"openModal('ticketModal')\">+ File Grievance</button>");
        } else {
            vars.put("createTicketButton", "");
        }

        String content = TemplateEngine.applyTokens(tmpl, vars);
        String html = TemplateEngine.renderPage("Grievance Tickets", "tickets", currentUser,
                SessionManager.getFlashSuccess(exchange),
                SessionManager.getFlashError(exchange),
                content);
        HttpUtils.sendHtml(exchange, html);
    }

    private void handleTicketCreate(HttpExchange exchange) throws IOException {
        User currentUser = SessionManager.getUser(exchange);
        if (currentUser == null) {
            HttpUtils.redirect(exchange, "/login");
            return;
        }

        Map<String, String> form = HttpUtils.parseFormData(exchange);
        String categoryStr = form.get("category");
        String subject = form.get("subject");
        String description = form.get("description");

        TicketCategory category = TicketCategory.GENERAL_MAINTENANCE;
        try {
            category = TicketCategory.valueOf(categoryStr);
        } catch (Exception ignored) { }

        Ticket created = ticketController.createTicket(currentUser.getUserId(), category, subject, description);
        if (created != null) {
            SessionManager.setFlashSuccess(exchange, "Grievance ticket #TKT-" + created.getTicketId() + " submitted and auto-routed to " + category + " staff.");
        } else {
            SessionManager.setFlashError(exchange, "Failed to create ticket. Please check input parameters.");
        }
        HttpUtils.redirect(exchange, "/tickets");
    }

    private void handleTicketUpdate(HttpExchange exchange) throws IOException {
        User currentUser = SessionManager.getUser(exchange);
        if (currentUser == null || currentUser.getRole() == Role.STUDENT) {
            HttpUtils.redirect(exchange, "/tickets");
            return;
        }

        Map<String, String> form = HttpUtils.parseFormData(exchange);
        try {
            int ticketId = Integer.parseInt(form.get("ticketId"));
            TicketStatus status = TicketStatus.valueOf(form.get("status"));
            String remarks = form.get("remarks");

            boolean ok = ticketController.updateTicketStatus(ticketId, status, remarks, currentUser.getUserId());
            if (ok) {
                SessionManager.setFlashSuccess(exchange, "Ticket #TKT-" + ticketId + " updated to " + status + ".");
            } else {
                SessionManager.setFlashError(exchange, "Failed to update ticket.");
            }
        } catch (Exception e) {
            SessionManager.setFlashError(exchange, "Invalid ticket update parameters.");
        }
        HttpUtils.redirect(exchange, "/tickets");
    }

    // ── Private Messaging Handlers ──────────────────────────────
    private void handleMessages(HttpExchange exchange) throws IOException {
        User currentUser = SessionManager.getUser(exchange);
        if (currentUser == null) {
            SessionManager.setFlashError(exchange, "Please sign in to view private communications.");
            HttpUtils.redirect(exchange, "/login");
            return;
        }

        Map<String, String> params = HttpUtils.parseQueryParams(exchange);
        int activePostId = -1;
        int activeOtherId = -1;

        if (params.containsKey("postId")) {
            try { activePostId = Integer.parseInt(params.get("postId")); } catch (Exception ignored) { }
        }
        if (params.containsKey("to")) {
            try { activeOtherId = Integer.parseInt(params.get("to")); } catch (Exception ignored) { }
        }

        List<Message> inbox = messageController.getInbox(currentUser.getUserId());

        // Auto-select first thread if none specified
        if (activePostId == -1 && !inbox.isEmpty()) {
            Message first = inbox.get(0);
            activePostId = first.getPostId();
            activeOtherId = (first.getSenderId() == currentUser.getUserId()) ? first.getReceiverId() : first.getSenderId();
        }

        StringBuilder threadListHtml = new StringBuilder();
        if (inbox.isEmpty() && activePostId == -1) {
            threadListHtml.append("<div style=\"padding:24px; text-align:center; color:var(--color-driftwood); font-size:13px;\">")
                    .append("No active conversations.<br><span style=\"color:var(--color-mist);\">Click 'Message Author' on any feed post to begin.</span>")
                    .append("</div>");
        } else {
            // Group threads by postId + otherUserId
            Set<String> seenThreads = new HashSet<>();
            for (Message msg : inbox) {
                int otherId = (msg.getSenderId() == currentUser.getUserId()) ? msg.getReceiverId() : msg.getSenderId();
                String otherName = (msg.getSenderId() == currentUser.getUserId()) ? msg.getReceiverName() : msg.getSenderName();
                String threadKey = msg.getPostId() + "_" + otherId;

                if (!seenThreads.contains(threadKey)) {
                    seenThreads.add(threadKey);
                    boolean isCurrent = (msg.getPostId() == activePostId && otherId == activeOtherId);
                    String activeClass = isCurrent ? "active" : "";

                    threadListHtml.append("<a href=\"/messages?postId=").append(msg.getPostId())
                            .append("&to=").append(otherId)
                            .append("\" class=\"thread-item ").append(activeClass).append("\">\n")
                            .append("  <div style=\"display:flex; justify-content:space-between; margin-bottom:4px;\">\n")
                            .append("    <strong style=\"font-size:14px; font-weight:500;\">").append(TemplateEngine.escape(otherName != null ? otherName : "User #" + otherId)).append("</strong>\n")
                            .append("    <span class=\"tag-chip\" style=\"font-size:10px;\">#POST-").append(msg.getPostId()).append("</span>\n")
                            .append("  </div>\n")
                            .append("  <div style=\"font-size:12px; color:var(--color-driftwood); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;\">")
                            .append(TemplateEngine.escape(msg.getContent()))
                            .append("</div>\n")
                            .append("</a>\n");
                }
            }
        }

        // Active conversation messages
        StringBuilder chatMessagesHtml = new StringBuilder();
        String chatTitle = "No Active Conversation Selected";
        String chatSubtitle = "Select a conversation thread from the left, or initiate one from the feed.";
        String chatBadge = "";

        if (activePostId != -1 && activeOtherId != -1) {
            Post relatedPost = feedController.getPostById(activePostId);
            if (relatedPost != null) {
                chatTitle = relatedPost.getTitle();
                chatSubtitle = "Regarding Notice #" + relatedPost.getPostId() + " (" + relatedPost.getCategory() + ")";
                chatBadge = "<span class=\"tag-chip " + ("LOST".equalsIgnoreCase(relatedPost.getPostType().name()) ? "tag-lost" : "tag-found") + "\">"
                        + relatedPost.getTypeTag() + "</span>";
            } else {
                chatTitle = "Notice #" + activePostId;
            }

            List<Message> convo = messageController.getConversation(activePostId, currentUser.getUserId(), activeOtherId);
            if (convo.isEmpty()) {
                chatMessagesHtml.append("<div style=\"text-align:center; padding:48px; color:var(--color-driftwood);\">\n")
                        .append("  <p class=\"prose\">No messages exchanged yet. Send a message below to start this conversation.</p>\n")
                        .append("</div>\n");
            } else {
                for (Message m : convo) {
                    chatMessagesHtml.append(messageAdapter.toHtml(m, currentUser)).append("\n");
                }
            }
        }

        String tmpl = TemplateEngine.loadTemplate("messages.html");
        Map<String, String> vars = new HashMap<>();
        vars.put("threadListHtml", threadListHtml.toString());
        vars.put("chatMessagesHtml", chatMessagesHtml.toString());
        vars.put("activeChatTitle", TemplateEngine.escape(chatTitle));
        vars.put("activeChatSubtitle", TemplateEngine.escape(chatSubtitle));
        vars.put("activeChatBadge", chatBadge);
        vars.put("activePostId", String.valueOf(activePostId));
        vars.put("activeReceiverId", String.valueOf(activeOtherId));
        vars.put("inputDisabled", (activePostId == -1 || activeOtherId == -1) ? "disabled" : "");
        vars.put("buttonDisabled", (activePostId == -1 || activeOtherId == -1) ? "disabled" : "");

        String content = TemplateEngine.applyTokens(tmpl, vars);
        String html = TemplateEngine.renderPage("Private Messages", "messages", currentUser,
                SessionManager.getFlashSuccess(exchange),
                SessionManager.getFlashError(exchange),
                content);
        HttpUtils.sendHtml(exchange, html);
    }

    private void handleMessageSend(HttpExchange exchange) throws IOException {
        User currentUser = SessionManager.getUser(exchange);
        if (currentUser == null) {
            HttpUtils.redirect(exchange, "/login");
            return;
        }

        Map<String, String> form = HttpUtils.parseFormData(exchange);
        try {
            int postId = Integer.parseInt(form.get("postId"));
            int receiverId = Integer.parseInt(form.get("receiverId"));
            String content = form.get("content");

            if (content != null && !content.trim().isEmpty()) {
                messageController.sendMessage(postId, currentUser.getUserId(), receiverId, content.trim());
            }
            HttpUtils.redirect(exchange, "/messages?postId=" + postId + "&to=" + receiverId);
        } catch (Exception e) {
            SessionManager.setFlashError(exchange, "Unable to send message.");
            HttpUtils.redirect(exchange, "/messages");
        }
    }
}
