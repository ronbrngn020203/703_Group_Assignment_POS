package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.adapters.MenuAdapter;
import com.ais.cafeteria.pos.models.MenuItem;
import com.ais.cafeteria.pos.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView rvMenu;
    private MenuAdapter adapter;
    private EditText etSearch;
    private LinearLayout categoryChips;
    private LinearLayout cartBar;
    private Button btnViewCart;
    private TextView tvCartBadge;
    private ImageView btnBack;

    private List<MenuItem> allItems;
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Find views
        rvMenu = findViewById(R.id.rvMenu);
        etSearch = findViewById(R.id.etSearch);
        categoryChips = findViewById(R.id.categoryChips);
        cartBar = findViewById(R.id.cartBar);
        btnViewCart = findViewById(R.id.btnViewCart);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        btnBack = findViewById(R.id.btnBack);

        // Load menu data
        allItems = CartManager.getMenuItems();

        // Set up RecyclerView (2-column grid)
        adapter = new MenuAdapter(allItems, item -> {
            CartManager.getInstance().addItem(item);
            updateCartBar();
            Toast.makeText(this, item.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
        });
        rvMenu.setLayoutManager(new GridLayoutManager(this, 2));
        rvMenu.setAdapter(adapter);

        // Build category chips
        buildCategoryChips();

        // ── Event: onTextChanged — Search filter ───────────────
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenu(s.toString());
            }
        });

        // ── Event: onClick — Back button ───────────────────────
        btnBack.setOnClickListener(v -> onBackPressed());

        // ── Event: onClick — View Cart ─────────────────────────
        btnViewCart.setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));

        FrameLayout cartHeader = findViewById(R.id.btnCart);
        if (cartHeader != null) {
            cartHeader.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        }

        updateCartBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        updateCartBar();
    }

    private void buildCategoryChips() {
        List<String> categories = new ArrayList<>();
        categories.add("All");
        for (MenuItem item : allItems) {
            if (!categories.contains(item.getCategory())) {
                categories.add(item.getCategory());
            }
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
                // Update all chip styles
                for (int i = 0; i < categoryChips.getChildCount(); i++) {
                    View child = categoryChips.getChildAt(i);
                    if (child instanceof Button) {
                        updateChipStyle((Button) child, ((Button) child).getText().toString().equals(cat));
                    }
                }
                filterMenu(etSearch.getText().toString());
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

    private void filterMenu(String query) {
        List<MenuItem> filtered = new ArrayList<>();
        for (MenuItem item : allItems) {
            boolean matchCategory = selectedCategory.equals("All") ||
                    item.getCategory().equals(selectedCategory);
            boolean matchQuery = query.isEmpty() ||
                    item.getName().toLowerCase().contains(query.toLowerCase()) ||
                    item.getDescription().toLowerCase().contains(query.toLowerCase());
            if (matchCategory && matchQuery) {
                filtered.add(item);
            }
        }
        adapter.updateItems(filtered);
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
