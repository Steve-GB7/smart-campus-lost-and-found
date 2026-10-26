package com.cec.helpdesk.controller;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP utilities for query parsing, form-urlencoded extraction,
 * multipart form-data handling, cookies, and standardized responses.
 */
public class HttpUtils {

    public static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB
    public static final int MAX_REQUEST_SIZE = 55 * 1024 * 1024; // 55 MB total payload limit

    public static class UploadedFile {
        private final String filename;
        private final String contentType;
        private final byte[] data;

        public UploadedFile(String filename, String contentType, byte[] data) {
            this.filename = filename;
            this.contentType = contentType;
            this.data = data != null ? data : new byte[0];
        }

        public String getFilename() { return filename; }
        public String getContentType() { return contentType; }
        public byte[] getData() { return data; }
        public int getSize() { return data.length; }
    }

    public static class MultipartRequest {
        private final Map<String, String> fields = new HashMap<>();
        private final Map<String, UploadedFile> files = new HashMap<>();

        public String getField(String name) { return fields.get(name); }
        public Map<String, String> getFields() { return fields; }
        public UploadedFile getFile(String name) { return files.get(name); }
        public Map<String, UploadedFile> getFiles() { return files; }
    }

    /**
     * Parses POST body (supports both application/x-www-form-urlencoded and multipart/form-data).
     */
    public static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        String cType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (cType != null && cType.toLowerCase().startsWith("multipart/form-data")) {
            MultipartRequest mr = parseMultipart(exchange);
            return mr.getFields();
        }

        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        String body = baos.toString(StandardCharsets.UTF_8.name());
        return parseQueryString(body);
    }

    /**
     * Parses multipart/form-data request body into fields and uploaded files.
     */
    public static MultipartRequest parseMultipart(HttpExchange exchange) throws IOException {
        MultipartRequest result = new MultipartRequest();
        String cType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (cType == null || !cType.toLowerCase().startsWith("multipart/form-data")) {
            result.fields.putAll(parseFormData(exchange));
            return result;
        }

        String boundary = null;
        int bIdx = cType.indexOf("boundary=");
        if (bIdx != -1) {
            boundary = cType.substring(bIdx + 9).trim();
            if (boundary.startsWith("\"") && boundary.endsWith("\"") && boundary.length() >= 2) {
                boundary = boundary.substring(1, boundary.length() - 1);
            }
        }
        if (boundary == null || boundary.isEmpty()) {
            throw new IOException("Missing boundary in multipart/form-data header");
        }

        byte[] rawBody = readBodyWithLimit(exchange.getRequestBody(), MAX_REQUEST_SIZE);
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1);

        int currentPos = findBytes(rawBody, 0, rawBody.length, boundaryBytes);
        if (currentPos == -1) {
            return result;
        }

        while (currentPos < rawBody.length) {
            int nextPos = findBytes(rawBody, currentPos + boundaryBytes.length, rawBody.length, boundaryBytes);
            if (nextPos == -1) {
                break;
            }

            int partStart = currentPos + boundaryBytes.length;
            // Skip CRLF or LF after boundary
            if (partStart + 1 < rawBody.length && rawBody[partStart] == '\r' && rawBody[partStart + 1] == '\n') {
                partStart += 2;
            } else if (partStart < rawBody.length && rawBody[partStart] == '\n') {
                partStart += 1;
            }

            int partEnd = nextPos;
            // Trim trailing CRLF before next boundary
            if (partEnd - 2 >= partStart && rawBody[partEnd - 2] == '\r' && rawBody[partEnd - 1] == '\n') {
                partEnd -= 2;
            } else if (partEnd - 1 >= partStart && rawBody[partEnd - 1] == '\n') {
                partEnd -= 1;
            }

            if (partEnd > partStart) {
                processPart(rawBody, partStart, partEnd, result);
            }

            currentPos = nextPos;
            // If terminating boundary "--boundary--"
            if (currentPos + boundaryBytes.length + 1 < rawBody.length &&
                rawBody[currentPos + boundaryBytes.length] == '-' &&
                rawBody[currentPos + boundaryBytes.length + 1] == '-') {
                break;
            }
        }

        return result;
    }

    private static void processPart(byte[] data, int start, int end, MultipartRequest result) {
        // Find header-body separator: "\r\n\r\n" or "\n\n"
        byte[] crlfcrlf = new byte[] { '\r', '\n', '\r', '\n' };
        byte[] lflf = new byte[] { '\n', '\n' };

        int sep = findBytes(data, start, end, crlfcrlf);
        int headerEnd = sep;
        int bodyStart = sep + 4;

        if (sep == -1) {
            sep = findBytes(data, start, end, lflf);
            if (sep == -1) return;
            headerEnd = sep;
            bodyStart = sep + 2;
        }

        String headersStr = new String(data, start, headerEnd - start, StandardCharsets.UTF_8);
        String[] headerLines = headersStr.split("\r?\n");

        String name = null;
        String filename = null;
        String partContentType = "application/octet-stream";

        for (String line : headerLines) {
            String lower = line.toLowerCase();
            if (lower.startsWith("content-disposition:")) {
                String[] parts = line.split(";");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (trimmed.toLowerCase().startsWith("name=")) {
                        name = extractQuotedValue(trimmed.substring(5));
                    } else if (trimmed.toLowerCase().startsWith("filename=")) {
                        filename = extractQuotedValue(trimmed.substring(9));
                        // Strip path if browser sent full path
                        if (filename != null) {
                            int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
                            if (slash != -1) filename = filename.substring(slash + 1);
                        }
                    }
                }
            } else if (lower.startsWith("content-type:")) {
                partContentType = line.substring(line.indexOf(':') + 1).trim();
            }
        }

        if (name == null) return;

        int bodyLen = end - bodyStart;
        if (bodyLen < 0) bodyLen = 0;
        byte[] partBody = new byte[bodyLen];
        if (bodyLen > 0) {
            System.arraycopy(data, bodyStart, partBody, 0, bodyLen);
        }

        if (filename != null) {
            // File upload part
            if (!filename.trim().isEmpty() || partBody.length > 0) {
                result.files.put(name, new UploadedFile(filename, partContentType, partBody));
            }
        } else {
            // Regular form field
            String value = new String(partBody, StandardCharsets.UTF_8);
            result.fields.put(name, value);
        }
    }

    private static String extractQuotedValue(String raw) {
        String s = raw.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static byte[] readBodyWithLimit(InputStream is, int maxBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int total = 0;
        int read;
        while ((read = is.read(buf)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new IOException("Request body exceeds maximum allowed size of " + (maxBytes / (1024 * 1024)) + " MB.");
            }
            baos.write(buf, 0, read);
        }
        return baos.toByteArray();
    }

    private static int findBytes(byte[] source, int start, int end, byte[] target) {
        if (target == null || target.length == 0 || end - start < target.length) return -1;
        for (int i = start; i <= end - target.length; i++) {
            boolean match = true;
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    /**
     * Parses URL query string into key-value pairs.
     */
    public static Map<String, String> parseQueryParams(HttpExchange exchange) {
        String query = exchange.getRequestURI().getRawQuery();
        return parseQueryString(query);
    }

    public static Map<String, String> parseQueryString(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            return map;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0 && idx < pair.length() - 1) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                    map.put(key, value);
                } else if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                    map.put(key, "");
                }
            } catch (UnsupportedEncodingException ignored) { }
        }
        return map;
    }

    /**
     * Extracts a cookie value by name from request headers.
     */
    public static String getCookie(HttpExchange exchange, String cookieName) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookieHeader : cookies) {
                String[] parts = cookieHeader.split(";");
                for (String part : parts) {
                    String[] kv = part.trim().split("=", 2);
                    if (kv.length == 2 && kv[0].trim().equals(cookieName)) {
                        return kv[1].trim();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sends an HTML response (200 OK), cleanly supporting GET and HEAD methods.
     */
    public static void sendHtml(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Sends a 302 redirect.
     */
    public static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}
