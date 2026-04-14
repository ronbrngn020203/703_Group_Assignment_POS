package com.ais.cafeteria.pos.network;

import com.ais.cafeteria.pos.models.MenuItem;
import java.util.List;

/**
 * MenuApiResponse  —  Wraps the JSON response from GET /api/menu and GET /api/menu/search.
 *
 * Server returns: { "success": true, "data": [ {...}, {...} ], "query": "..." }
 */
public class MenuApiResponse {
    private boolean success;
    private List<MenuItem> data;
    private String query;
    private String message;

    public boolean isSuccess()        { return success; }
    public List<MenuItem> getData()   { return data; }
    public String getQuery()          { return query; }
    public String getMessage()        { return message; }
}