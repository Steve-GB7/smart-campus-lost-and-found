package com.cec.helpdesk.controller;

import com.cec.helpdesk.model.User;
import com.sun.net.httpserver.HttpExchange;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory HTTP Session Manager.
 *
 * Tracks active authenticated users via session cookies and flash messages.
 */
public class SessionManager {

    private static final String COOKIE_NAME = "CEC_SESSION_ID";

    private static final Map<String, User> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> flashSuccess = new ConcurrentHashMap<>();
    private static final Map<String, String> flashError = new ConcurrentHashMap<>();

    public static User getUser(HttpExchange exchange) {
        String sessionId = HttpUtils.getCookie(exchange, COOKIE_NAME);
        if (sessionId == null) return null;
        return sessions.get(sessionId);
    }

    public static void setUser(HttpExchange exchange, User user) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, user);
        exchange.getResponseHeaders().add("Set-Cookie",
                COOKIE_NAME + "=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax");
    }

    public static void clearUser(HttpExchange exchange) {
        String sessionId = HttpUtils.getCookie(exchange, COOKIE_NAME);
        if (sessionId != null) {
            sessions.remove(sessionId);
            flashSuccess.remove(sessionId);
            flashError.remove(sessionId);
        }
        exchange.getResponseHeaders().add("Set-Cookie",
                COOKIE_NAME + "=deleted; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }

    public static void setFlashSuccess(HttpExchange exchange, String msg) {
        String sessionId = HttpUtils.getCookie(exchange, COOKIE_NAME);
        if (sessionId != null) {
            flashSuccess.put(sessionId, msg);
        }
    }

    public static void setFlashError(HttpExchange exchange, String msg) {
        String sessionId = HttpUtils.getCookie(exchange, COOKIE_NAME);
        if (sessionId != null) {
            flashError.put(sessionId, msg);
        }
    }

    public static String getFlashSuccess(HttpExchange exchange) {
        String sessionId = HttpUtils.getCookie(exchange, COOKIE_NAME);
        if (sessionId == null) return null;
        return flashSuccess.remove(sessionId);
    }

    public static String getFlashError(HttpExchange exchange) {
        String sessionId = HttpUtils.getCookie(exchange, COOKIE_NAME);
        if (sessionId == null) return null;
        return flashError.remove(sessionId);
    }
}
