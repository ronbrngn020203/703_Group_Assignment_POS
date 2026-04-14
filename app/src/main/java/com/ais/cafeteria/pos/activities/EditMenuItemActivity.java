package com.ais.cafeteria.pos.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.models.MenuItem;
import com.ais.cafeteria.pos.repository.MenuRepository;

/**
 * EditMenuItemActivity  —  Tier 1 (User Interface) for editing a menu item.
 *
 * Receives the current menu item fields via Intent extras, allows the staff
 * member to edit them, and calls MenuRepository.updateMenuItem() which fires
 * PUT /api/menu/{id} → Node.js → SQLite to persist the change.
 *
 * Register in AndroidManifest.xml:
 *   <activity android:name=".activities.EditMenuItemActivity" android:exported="false" />
 */
public class EditMenuItemActivity extends AppCompatActivity {

    // Intent extra keys
    public static final String EXTRA_ITEM_ID    = "item_id";
    public static final String EXTRA_ITEM_NAME  = "item_name";
    public static final String EXTRA_ITEM_DESC  = "item_desc";
    public static final String EXTRA_ITEM_PRICE = "item_price";
    public static final String EXTRA_ITEM_CAT   = "item_category";
    public static final String EXTRA_ITEM_EMOJI = "item_emoji";

    private EditText   etName, etDescription, etPrice, etCategory, etEmoji;
    private Button     btnSave;
    private ImageView  btnBack;
    private ProgressBar progressBar;
    private TextView   tvTitle;

    private int currentItemId;

    private final MenuRepository menuRepository = new MenuRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_menu_item);

        bindViews();
        populateFromIntent();
        setupListeners();
    }

    // ── Bind & Populate ───────────────────────────────────────

    private void bindViews() {
        tvTitle      = findViewById(R.id.tvEditTitle);
        etName       = findViewById(R.id.etEditName);
        etDescription= findViewById(R.id.etEditDescription);
        etPrice      = findViewById(R.id.etEditPrice);
        etCategory   = findViewById(R.id.etEditCategory);
        etEmoji      = findViewById(R.id.etEditEmoji);
        btnSave      = findViewById(R.id.btnSave);
        btnBack      = findViewById(R.id.btnBack);
        progressBar  = findViewById(R.id.progressBar);
    }

    private void populateFromIntent() {
        currentItemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        etName.setText(getIntent().getStringExtra(EXTRA_ITEM_NAME));
        etDescription.setText(getIntent().getStringExtra(EXTRA_ITEM_DESC));
        etPrice.setText(String.valueOf(getIntent().getDoubleExtra(EXTRA_ITEM_PRICE, 0.0)));
        etCategory.setText(getIntent().getStringExtra(EXTRA_ITEM_CAT));
        etEmoji.setText(getIntent().getStringExtra(EXTRA_ITEM_EMOJI));
        if (tvTitle != null) {
            tvTitle.setText("Edit: " + getIntent().getStringExtra(EXTRA_ITEM_NAME));
        }
    }

    // ── Event Listeners ───────────────────────────────────────

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    // ── Validate & Save (HTTP PUT) ────────────────────────────

    private void validateAndSave() {
        String name        = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr    = etPrice.getText().toString().trim();
        String category    = etCategory.getText().toString().trim();
        String emoji       = etEmoji.getText().toString().trim();

        // Input validation
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (priceStr.isEmpty()) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etPrice.setError("Enter a valid price (e.g. 9.50)");
            etPrice.requestFocus();
            return;
        }
        if (currentItemId < 0) {
            Toast.makeText(this, "Invalid menu item ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build updated MenuItem
        MenuItem updated = new MenuItem();
        updated.setId(currentItemId);
        updated.setName(name);
        updated.setDescription(description);
        updated.setPrice(price);
        updated.setCategory(category.isEmpty() ? "Mains" : category);
        updated.setEmoji(emoji.isEmpty() ? "🍽" : emoji);

        // Call DAL → HTTP PUT /api/menu/{id}
        setLoading(true);
        menuRepository.updateMenuItem(currentItemId, updated, new MenuRepository.OnMenuUpdateCallback() {
            @Override
            public void onSuccess(MenuItem updatedItem) {
                setLoading(false);
                Toast.makeText(EditMenuItemActivity.this,
                        "✅ Menu item updated successfully!", Toast.LENGTH_SHORT).show();
                // Return OK to MenuActivity so it can reload
                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(EditMenuItemActivity.this,
                        "❌ Update failed: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── UI Helper ─────────────────────────────────────────────

    private void setLoading(boolean loading) {
        if (progressBar != null)
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
        btnSave.setText(loading ? "Saving…" : "Save Changes");
    }
}