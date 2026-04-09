package com.ais.cafeteria.pos.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.adapters.OrderHistoryAdapter;
import com.ais.cafeteria.pos.models.Order;
import com.ais.cafeteria.pos.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrderHistory;
    private OrderHistoryAdapter adapter;
    private EditText etSearch;
    private List<Order> allOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        etSearch = findViewById(R.id.etSearch);
        ImageView btnBack = findViewById(R.id.btnBack);

        allOrders = CartManager.getInstance().getOrderHistory();

        adapter = new OrderHistoryAdapter(allOrders);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrderHistory.setAdapter(adapter);

        // onClick — Back
        btnBack.setOnClickListener(v -> onBackPressed());

        // onTextChanged — Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders(s.toString());
            }
        });
    }

    private void filterOrders(String query) {
        if (query.isEmpty()) {
            adapter.updateOrders(allOrders);
            return;
        }
        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrders) {
            if (o.getOrderId().toLowerCase().contains(query.toLowerCase()) ||
                    o.getDate().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(o);
            }
        }
        adapter.updateOrders(filtered);
    }
}
