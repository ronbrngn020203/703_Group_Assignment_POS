package com.ais.cafeteria.pos.network;

import com.ais.cafeteria.pos.models.MenuItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * MenuApiService  —  Retrofit interface for the SQLite REST API.
 *
 * Maps Java method calls to HTTP requests against the Node.js backend.
 */
public interface MenuApiService {

    /**
     * GET /api/menu
     * Fetches all menu items from SQLite.
     */
    @GET("api/menu")
    Call<MenuApiResponse> getAllMenuItems();

    /**
     * GET /api/menu/search?q={query}
     * Searches menu items by name, description, or category.
     */
    @GET("api/menu/search")
    Call<MenuApiResponse> searchMenuItems(@Query("q") String query);

    /**
     * PUT /api/menu/{id}
     * Updates an existing menu item in SQLite.
     */
    @PUT("api/menu/{id}")
    Call<UpdateResponse> updateMenuItem(
            @Path("id") int id,
            @Body MenuItem menuItem
    );
}