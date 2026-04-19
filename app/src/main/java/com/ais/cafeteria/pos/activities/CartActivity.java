package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.adapters.CartAdapter;
import com.ais.cafeteria.pos.models.CartItem;
import com.ais.cafeteria.pos.utils.CartManager;

import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private CartAdapter adapter;
    private LinearLayout emptyState, checkoutSection;
    private Button btnBrowseMenu, btnCheckout;
    private TextView tvSubtotal, tvGst, tvTotal;
    private ImageView btnBack;
    private EditText etOrderNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCartItems     = findViewById(R.id.rvCartItems);
        emptyState      = findViewById(R.id.emptyState);
        checkoutSection = findViewById(R.id.checkoutSection);
        btnBrowseMenu   = findViewById(R.id.btnBrowseMenu);
        btnCheckout     = findViewById(R.id.btnCheckout);
        tvSubtotal      = findViewById(R.id.tvSubtotal);
        tvGst           = findViewById(R.id.tvGst);
        tvTotal         = findViewById(R.id.tvTotal);
        btnBack         = findViewById(R.id.btnBack);
        etOrderNote     = findViewById(R.id.etOrderNote);

        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        adapter = new CartAdapter(cartItems, new CartAdapter.CartListener() {
            @Override
            public void onIncrement(CartItem item) {
                CartManager.getInstance().updateQuantity(item.getMenuItem().getId(), 1);
                refreshCart();
            }

            @Override
            public void onDecrement(CartItem item) {
                CartManager.getInstance().updateQuantity(item.getMenuItem().getId(), -1);
                refreshCart();
            }

            @Override
            public void onRemove(CartItem item) {
                CartManager.getInstance().removeItem(item.getMenuItem().getId());
                refreshCart();
            }
        });

        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(adapter);

        btnBack.setOnClickListener(v -> onBackPressed());

        btnBrowseMenu.setOnClickListener(v -> {
            startActivity(new Intent(this, MenuActivity.class));
            finish();
        });

        // ✅ Save note to CartManager before going to payment
        btnCheckout.setOnClickListener(v -> {
            String note = etOrderNote != null ?
                    etOrderNote.getText().toString().trim() : "";
            CartManager.getInstance().setOrderNote(note);
            startActivity(new Intent(this, PaymentActivity.class));
        });

        refreshCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCart();
    }

    private void refreshCart() {
        List<CartItem> items = CartManager.getInstance().getCartItems();
        adapter.updateItems(items);

        if (items.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            checkoutSection.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            checkoutSection.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.VISIBLE);

            double subtotal = CartManager.getInstance().getSubtotal();
            double gst      = CartManager.getInstance().getGst();
            double total    = CartManager.getInstance().getTotal();

            tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
            tvGst.setText(String.format(Locale.getDefault(), "$%.2f", gst));
            tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
        }
    }
}