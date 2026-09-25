package com.cec.helpdesk;

import com.cec.helpdesk.controller.WebRouter;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

/**
 * Web Application Entry Point.
 *
 * Bootstraps the lightweight embedded HTTP web server and routes
 * requests to the MVC web controllers and Warm Parchment view layer.
 */
public class App {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String portProp = System.getProperty("port");
        if (portProp != null) {
            try {
                port = Integer.parseInt(portProp);
            } catch (NumberFormatException ignored) { }
        } else {
            String portEnv = System.getenv("PORT");
            if (portEnv != null) {
                try {
                    port = Integer.parseInt(portEnv);
                } catch (NumberFormatException ignored) { }
            }
        }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new WebRouter());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();

            String url = "http://localhost:" + port + "/";
            System.out.println("==================================================================");
            System.out.println("   Unified Smart Campus Helpdesk - Web Application");
            System.out.println("   College of Engineering Chengannur (CEC)");
            System.out.println("   Design System: Warm Parchment Atelier (Flat, Light, Editorial)");
            System.out.println("==================================================================");
            System.out.println("   Server is running at: " + url);
            System.out.println("   Default Admin Login : admin / admin123");
            System.out.println("   Press Ctrl+C to terminate the server.");
            System.out.println("==================================================================");

            // Attempt to open default browser if running in desktop environment
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ignored) { }
            }

        } catch (Exception e) {
            System.err.println("Failed to start HTTP server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
