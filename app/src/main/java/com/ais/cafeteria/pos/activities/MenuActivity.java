package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.adapters.MenuAdapter;
import com.ais.cafeteria.pos.models.MenuItem;
import com.ais.cafeteria.pos.repository.MenuRepository;
import com.ais.cafeteria.pos.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

/**
 * MenuActivity  —  Tier 1 (User Interface)
 *
 * Changes for Task 3:
 *  • Menu items are now fetched from the SQLite backend via MenuRepository (Retrofit)
 *  • Search is performed via HTTP web service call (GET /api/menu/search?q=)
 *  • Long-pressing a menu item opens EditMenuItemActivity for editing
 */
public class MenuActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT = 101;

    private RecyclerView  rvMenu;
    private MenuAdapter   adapter;
    private EditText      etSearch;
    private LinearLayout  categoryChips;
    private LinearLayout  cartBar;
    private Button        btnViewCart;
    private TextView      tvCartBadge;
    private ImageView     btnBack;
    private ProgressBar   progressBar;
    private TextView      tvEmptyState;

    private List<MenuItem> allItems       = new ArrayList<>();
    private String         selectedCategory = "All";

    // ── Data Access Layer ─────────────────────────────────────
    private final MenuRepository menuRepository = new MenuRepository();

    // Debounce handler — prevents firing an API call on every keystroke
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DEBOUNCE_MS = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        bindViews();
        setupRecyclerView();
        setupSearch();
        setupNavigation();

        // Fetch menu items from SQLite via HTTP web service
        loadMenuFromServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        updateCartBar();
    }

    // ── Bind Views ────────────────────────────────────────────

    private void bindViews() {
        rvMenu         = findViewById(R.id.rvMenu);
        etSearch       = findViewById(R.id.etSearch);
        categoryChips  = findViewById(R.id.categoryChips);
        cartBar        = findViewById(R.id.cartBar);
        btnViewCart    = findViewById(R.id.btnViewCart);
        tvCartBadge    = findViewById(R.id.tvCartBadge);
        btnBack        = findViewById(R.id.btnBack);
        progressBar    = findViewById(R.id.progressBar);    // add this to your layout
        tvEmptyState   = findViewById(R.id.tvEmptyState);  // add this to your layout
    }

    // ── RecyclerView ──────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new MenuAdapter(allItems, item -> {
            CartManager.getInstance().addItem(item);
            updateCartBar();
            Toast.makeText(this, item.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
        });

        // Long-press on a menu card opens the edit screen
        adapter.setOnItemLongClickListener(item -> {
            Intent intent = new Intent(this, EditMenuItemActivity.class);
            intent.putExtra(EditMenuItemActivity.EXTRA_ITEM_ID,    item.getId());
            intent.putExtra(EditMenuItemActivity.EXTRA_ITEM_NAME,  item.getName());
            intent.putExtra(EditMenuItemActivity.EXTRA_ITEM_DESC,  item.getDescription());
            intent.putExtra(EditMenuItemActivity.EXTRA_ITEM_PRICE, item.getPrice());
            intent.putExtra(EditMenuItemActivity.EXTRA_ITEM_CAT,   item.getCategory());
            intent.putExtra(EditMenuItemActivity.EXTRA_ITEM_EMOJI, item.getEmoji());
            startActivityForResult(intent, REQUEST_EDIT);
        });

        rvMenu.setLayoutManager(new GridLayoutManager(this, 2));
        rvMenu.setAdapter(adapter);
    }

    // ── Load from Server (Tier 2 → Tier 3) ───────────────────

    /**
     * Calls MenuRepository.fetchAllMenuItems() which fires
     * GET /api/menu → Node.js → SQLite and returns the result.
     */
    private void loadMenuFromServer() {
        showLoading(true);
        menuRepository.fetchAllMenuItems(new MenuRepository.OnMenuLoadedCallback() {
            @Override
            public void onSuccess(List<MenuItem> items) {
                allItems.clear();
                allItems.addAll(items);
                adapter.updateItems(new ArrayList<>(allItems));
                buildCategoryChips();
                showLoading(false);
                updateCartBar();
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(MenuActivity.this,
                        "⚠ Could not reach server.\n" + message,
                        Toast.LENGTH_LONG).show();
                // Fallback: load hardcoded items so app still works
                allItems = CartManager.getMenuItems();
                adapter.updateItems(new ArrayList<>(allItems));
                buildCategoryChips();
            }
        });
    }

    // ── Search (HTTP web service call) ────────────────────────

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Debounce: wait 400 ms after user stops typing before sending API request
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    // Show all items from local cache (no API call needed)
                    filterByCategory(allItems);
                    return;
                }

                searchRunnable = () -> {
                    // Fire HTTP search request to Node.js → SQLite
                    menuRepository.searchMenuItems(query, new MenuRepository.OnMenuLoadedCallback() {
                        @Override
                        public void onSuccess(List<MenuItem> items) {
                            filterByCategory(items);
                        }

                        @Override
                        public void onError(String message) {
                            // Fallback to local filter on network error
                            filterLocallyByQuery(query);
                        }
                    });
                };
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }
        });
    }

    /** Applies category chip filter on top of an already-searched list. */
    private void filterByCategory(List<MenuItem> source) {
        if (selectedCategory.equals("All")) {
            adapter.updateItems(new ArrayList<>(source));
            return;
        }
        List<MenuItem> filtered = new ArrayList<>();
        for (MenuItem item : source) {
            if (item.getCategory().equals(selectedCategory)) filtered.add(item);
        }
        adapter.updateItems(filtered);
    }

    /** Local fallback filter used when network is unavailable. */
    private void filterLocallyByQuery(String query) {
        List<MenuItem> filtered = new ArrayList<>();
        for (MenuItem item : allItems) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())
                    || item.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(item);
            }
        }
        filterByCategory(filtered);
    }

    // ── Category Chips ────────────────────────────────────────

    private void buildCategoryChips() {
        categoryChips.removeAllViews();
        List<String> categories = new ArrayList<>();
        categories.add("All");
        for (MenuItem item : allItems) {
            if (!categories.contains(item.getCategory())) categories.add(item.getCategory());
        }
        for (String cat : categories) {
            Button chip = new Button(this);
            chip.setText(cat);
            chip.setTextSize(12f);
            chip.setPadding(32, 12, 32, 12);
            chip.setAllCaps(false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(8);
            chip.setLayoutParams(params);
            updateChipStyle(chip, cat.equals(selectedCategory));
            chip.setOnClickListener(v -> {
                selectedCategory = cat;
                for (int i = 0; i < categoryChips.getChildCount(); i++) {
                    View child = categoryChips.getChildAt(i);
                    if (child instanceof Button) {
                        updateChipStyle((Button) child,
                                ((Button) child).getText().toString().equals(cat));
                    }
                }
                filterByCategory(allItems);
            });
            categoryChips.addView(chip);
        }
    }

    private void updateChipStyle(Button chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(getResources().getColor(R.color.text_secondary, null));
        }
    }

    // ── Navigation ────────────────────────────────────────────

    private void setupNavigation() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnViewCart.setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));
        FrameLayout cartHeader = findViewById(R.id.btnCart);
        if (cartHeader != null) {
            cartHeader.setOnClickListener(v ->
                    startActivity(new Intent(this, CartActivity.class)));
        }
        updateCartBar();
    }

    // ── After Edit Returns ────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            // Reload menu from server to reflect the update
            loadMenuFromServer();
            Toast.makeText(this, "Menu item updated!", Toast.LENGTH_SHORT).show();
        }
    }

    // ── UI Helpers ────────────────────────────────────────────

    private void showLoading(boolean show) {
        if (progressBar  != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
    }

    private void updateCartBar() {
        int count = CartManager.getInstance().getCartCount();
        if (count > 0) {
            cartBar.setVisibility(View.VISIBLE);
            btnViewCart.setText("🛒 View Cart (" + count + ")  $" +
                    String.format("%.2f", CartManager.getInstance().getTotal()));
            if (tvCartBadge != null) {
                tvCartBadge.setVisibility(View.VISIBLE);
                tvCartBadge.setText(String.valueOf(count));
            }
        } else {
            cartBar.setVisibility(View.GONE);
            if (tvCartBadge != null) tvCartBadge.setVisibility(View.GONE);
        }
    }
}