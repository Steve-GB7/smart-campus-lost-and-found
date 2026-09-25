package com.cec.helpdesk.view;

import com.cec.helpdesk.model.User;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * View Layer — Template Renderer.
 *
 * Merges data models into static HTML templates without embedding business logic.
 * Reads templates from the web/templates/ directory.
 */
public class TemplateEngine {

    private static final String TEMPLATE_DIR = "web/templates";

    /**
     * Reads a template file by name.
     */
    public static String loadTemplate(String templateName) {
        Path path = Paths.get(TEMPLATE_DIR, templateName);
        if (!Files.exists(path)) {
            // Also try relative to project root if executed from subfolder
            File file = new File(TEMPLATE_DIR, templateName);
            if (file.exists()) {
                path = file.toPath();
            } else {
                System.err.println("Template not found: " + path.toAbsolutePath());
                return "<p>Template " + templateName + " not found.</p>";
            }
        }
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading template: " + e.getMessage());
            return "<p>Error loading template " + templateName + "</p>";
        }
    }

    /**
     * Renders a full page with standard header, page content, and footer.
     */
    public static String renderPage(String pageTitle, String activeNav,
                                    User currentUser, String flashSuccess,
                                    String flashError, String contentHtml) {
        String headerTmpl = loadTemplate("header.html");
        String footerTmpl = loadTemplate("footer.html");

        Map<String, String> headerVars = new HashMap<>();
        headerVars.put("pageTitle", escape(pageTitle));
        headerVars.put("activeFeed", "feed".equalsIgnoreCase(activeNav) ? "active" : "");
        headerVars.put("activeTickets", "tickets".equalsIgnoreCase(activeNav) ? "active" : "");
        headerVars.put("activeMessages", "messages".equalsIgnoreCase(activeNav) ? "active" : "");

        // Build User Nav elements (Polymorphism & Role Display)
        if (currentUser != null) {
            String roleClass = "role-pill";
            String userBadgeHtml = "<li><div class=\"nav-user-badge\">"
                    + "<strong style=\"color:var(--color-ink);\">" + escape(currentUser.getFullName()) + "</strong>"
                    + "<span class=\"" + roleClass + "\">" + currentUser.getRole().name() + "</span>"
                    + "</div></li>"
                    + "<li><a href=\"/logout\" class=\"btn btn-ghost\">Sign Out</a></li>";
            headerVars.put("userNav", userBadgeHtml);
        } else {
            String guestNav = "<li><a href=\"/login\" class=\"btn btn-secondary\">Sign In</a></li>"
                    + "<li><a href=\"/register\" class=\"btn btn-primary\">Register</a></li>";
            headerVars.put("userNav", guestNav);
        }

        // Build Flash Banner HTML
        StringBuilder flashHtml = new StringBuilder();
        if (flashSuccess != null && !flashSuccess.trim().isEmpty()) {
            flashHtml.append("<div class=\"flash-message flash-success\">\n")
                    .append("  <span>✓ ").append(escape(flashSuccess)).append("</span>\n")
                    .append("</div>\n");
        }
        if (flashError != null && !flashError.trim().isEmpty()) {
            flashHtml.append("<div class=\"flash-message flash-error\">\n")
                    .append("  <span>⚠ ").append(escape(flashError)).append("</span>\n")
                    .append("</div>\n");
        }
        headerVars.put("flashHtml", flashHtml.toString());

        String renderedHeader = applyTokens(headerTmpl, headerVars);
        return renderedHeader + "\n" + contentHtml + "\n" + footerTmpl;
    }

    /**
     * Replaces {{key}} placeholders in a template with matching values.
     */
    public static String applyTokens(String template, Map<String, String> tokens) {
        String result = template;
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            String val = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(key, val);
        }
        return result;
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
