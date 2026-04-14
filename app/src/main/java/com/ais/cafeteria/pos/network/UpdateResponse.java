package com.ais.cafeteria.pos.network;

import com.ais.cafeteria.pos.models.MenuItem;

/**
 * UpdateResponse  —  Wraps the JSON response from PUT /api/menu/:id.
 *
 * Server returns: { "success": true, "message": "...", "data": { updated item } }
 */
public class UpdateResponse {
    private boolean success;
    private String message;
    private MenuItem data;

    public boolean isSuccess()      { return success; }
    public String getMessage()      { return message; }
    public MenuItem getData()       { return data; }
}