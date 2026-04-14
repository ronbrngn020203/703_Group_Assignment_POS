package com.ais.cafeteria.pos.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.adapters.OrderHistoryAdapter;
import com.ais.cafeteria.pos.models.Order;
import com.ais.cafeteria.pos.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * OrderHistoryActivity  —  Tier 1 (User Interface) for Order History.
 *
 * Changes for Task 3:
 *  • Orders are now loaded from Firebase Firestore (NoSQL) via OrderRepository.
 *  • Search is performed via OrderRepository.searchOrders() (Firestore fetch + in-memory filter).
 */
public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView         rvOrderHistory;
    private OrderHistoryAdapter  adapter;
    private EditText             etSearch;
    private ProgressBar          progressBar;
    private TextView             tvEmptyState;

    // ── Data Access Layer (NoSQL) ─────────────────────────────
    private final OrderRepository orderRepository = new OrderRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        bindViews();
        setupRecyclerView();
        setupSearch();

        // Load orders from Firestore
        loadOrdersFromFirestore();
    }

    // ── Bind Views ────────────────────────────────────────────

    private void bindViews() {
        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        etSearch       = findViewById(R.id.etSearch);
        progressBar    = findViewById(R.id.progressBar);
        tvEmptyState   = findViewById(R.id.tvEmptyState);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── RecyclerView ──────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(new ArrayList<>());
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrderHistory.setAdapter(adapter);
    }

    // ── Load From Firestore ───────────────────────────────────

    /**
     * Fetches all orders from the Firestore "orders" collection.
     * Sorted newest-first by the server timestamp.
     */
    private void loadOrdersFromFirestore() {
        showLoading(true);
        orderRepository.getAllOrders(new OrderRepository.OnOrdersLoadedCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                showLoading(false);
                if (orders.isEmpty()) {
                    showEmptyState("No orders found.");
                } else {
                    hideEmptyState();
                    adapter.updateOrders(orders);
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(OrderHistoryActivity.this,
                        "⚠ Could not load orders: " + message, Toast.LENGTH_LONG).show();
                showEmptyState("Failed to load orders.\nCheck your internet connection.");
            }
        });
    }

    // ── Search (Firestore fetch + in-memory filter) ───────────

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadOrdersFromFirestore(); // Reload all
                    return;
                }
                // Search via repository (fetches all from Firestore, filters in-memory)
                orderRepository.searchOrders(query, new OrderRepository.OnOrdersLoadedCallback() {
                    @Override
                    public void onSuccess(List<Order> orders) {
                        if (orders.isEmpty()) {
                            showEmptyState("No orders match \"" + query + "\"");
                        } else {
                            hideEmptyState();
                            adapter.updateOrders(orders);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(OrderHistoryActivity.this,
                                "Search error: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // ── UI Helpers ────────────────────────────────────────────

    private void showLoading(boolean show) {
        if (progressBar != null)
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(String message) {
        if (tvEmptyState != null) {
            tvEmptyState.setText(message);
            tvEmptyState.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyState() {
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
    }
}