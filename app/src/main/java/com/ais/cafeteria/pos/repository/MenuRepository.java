package com.ais.cafeteria.pos.repository;

import com.ais.cafeteria.pos.models.MenuItem;
import com.ais.cafeteria.pos.network.MenuApiResponse;
import com.ais.cafeteria.pos.network.MenuApiService;
import com.ais.cafeteria.pos.network.RetrofitClient;
import com.ais.cafeteria.pos.network.UpdateResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MenuRepository  —  Data Access Layer for Menu Items.
 *
 * Sits between the UI (MenuActivity / EditMenuItemActivity) and the
 * HTTP web service (Node.js + SQLite backend via Retrofit).
 *
 * Tier mapping:
 *   UI  →  MenuRepository  →  [HTTP]  →  Node.js API  →  SQLite DB
 */
public class MenuRepository {

    // Callback interfaces — UI implements these to receive results
    public interface OnMenuLoadedCallback {
        void onSuccess(List<MenuItem> items);
        void onError(String message);
    }

    public interface OnMenuUpdateCallback {
        void onSuccess(MenuItem updatedItem);
        void onError(String message);
    }

    private final MenuApiService apiService;

    public MenuRepository() {
        this.apiService = RetrofitClient.getMenuApiService();
    }

    // ── Fetch All Menu Items ──────────────────────────────────

    /**
     * Fetches all menu items from the SQLite backend via GET /api/menu.
     * Calls onSuccess(items) on the main thread when done.
     */
    public void fetchAllMenuItems(OnMenuLoadedCallback callback) {
        apiService.getAllMenuItems().enqueue(new Callback<MenuApiResponse>() {
            @Override
            public void onResponse(Call<MenuApiResponse> call, Response<MenuApiResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to load menu. Server returned: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MenuApiResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage()
                        + "\n\nMake sure the backend server is running.");
            }
        });
    }

    // ── Search Menu Items ─────────────────────────────────────

    /**
     * Searches menu items via GET /api/menu/search?q={query}.
     * Uses the SQLite LIKE query on the server — satisfies the
     * "search using HTTP web services" requirement.
     */
    public void searchMenuItems(String query, OnMenuLoadedCallback callback) {
        apiService.searchMenuItems(query).enqueue(new Callback<MenuApiResponse>() {
            @Override
            public void onResponse(Call<MenuApiResponse> call, Response<MenuApiResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Search failed. Server returned: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MenuApiResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // ── Update Menu Item ──────────────────────────────────────

    /**
     * Updates a menu item via PUT /api/menu/{id}.
     * Sends the full updated MenuItem as JSON body.
     * Uses the HTTP web service — satisfies the "edit/update using HTTP" requirement.
     */
    public void updateMenuItem(int id, MenuItem menuItem, OnMenuUpdateCallback callback) {
        apiService.updateMenuItem(id, menuItem).enqueue(new Callback<UpdateResponse>() {
            @Override
            public void onResponse(Call<UpdateResponse> call, Response<UpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    String msg = (response.body() != null)
                            ? response.body().getMessage()
                            : "Update failed. Server returned: " + response.code();
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<UpdateResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}