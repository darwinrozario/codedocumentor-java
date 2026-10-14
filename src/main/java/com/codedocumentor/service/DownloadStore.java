package com.codedocumentor.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for generated documentation blobs that can be downloaded via a one-time token.
 * Mirrors Python's DOWNLOADS dict and the /download/<token> route.
 *
 * NOTE: This is intentionally simple (in-memory). In production you would want
 * expiry and possibly Redis-backed storage.
 */
@Service
public class DownloadStore {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    /**
     * Save documentation content and return a unique download token.
     */
    public String save(String documentation) {
        String token = UUID.randomUUID().toString();
        store.put(token, documentation);
        return token;
    }

    /**
     * Retrieve content by token, or null if not found / expired.
     */
    public String get(String token) {
        return store.get(token);
    }

    /**
     * Remove a token after it has been served (optional one-time use).
     */
    public void remove(String token) {
        store.remove(token);
    }
}
