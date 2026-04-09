package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.adapters.ReceiptAdapter;
import com.ais.cafeteria.pos.models.Order;
import com.ais.cafeteria.pos.utils.CartManager;

import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        Order order = CartManager.getInstance().getLastOrder();

        if (order == null) {
            finish();
            return;
        }

        // Populate receipt
        TextView tvOrderId       = findViewById(R.id.tvOrderId);
        TextView tvSubtotal      = findViewById(R.id.tvSubtotal);
        TextView tvGst           = findViewById(R.id.tvGst);
        TextView tvTotal         = findViewById(R.id.tvTotal);
        TextView tvPayMethod     = findViewById(R.id.tvPayMethod);
        RecyclerView rvReceiptItems = findViewById(R.id.rvReceiptItems);
        TextView btnEmailReceipt = findViewById(R.id.btnEmailReceipt);
        TextView btnPrint        = findViewById(R.id.btnPrint);
        TextView btnNewOrder     = findViewById(R.id.btnNewOrder);

        tvOrderId.setText(order.getOrderId());

        double subtotal = order.getTotal();
        double gst      = subtotal * 0.15;
        double total    = subtotal + gst;

        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        tvGst.setText(String.format(Locale.getDefault(), "$%.2f", gst));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
        tvPayMethod.setText("Payment: " + order.getPaymentMethod());

        // Receipt items
        ReceiptAdapter adapter = new ReceiptAdapter(order.getItems());
        rvReceiptItems.setLayoutManager(new LinearLayoutManager(this));
        rvReceiptItems.setAdapter(adapter);
        rvReceiptItems.setNestedScrollingEnabled(false);

        // Email Receipt
        btnEmailReceipt.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cafeteria@ais.ac.nz"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "AIS Cafeteria Receipt " + order.getOrderId());
            emailIntent.putExtra(Intent.EXTRA_TEXT,
                    "Order: " + order.getOrderId() +
                            "\nDate: " + order.getDate() +
                            "\nTotal: $" + String.format(Locale.getDefault(), "%.2f", total) +
                            "\nPayment: " + order.getPaymentMethod() +
                            "\n\nThank you for dining at AIS Cafeteria!" +
                            "\n28A Linwood Avenue, Mount Albert, Auckland 1025");
            try {
                startActivity(Intent.createChooser(emailIntent, "Send receipt via email"));
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // Print
        btnPrint.setOnClickListener(v ->
                Toast.makeText(this, "Sending to printer…", Toast.LENGTH_SHORT).show());

        // New Order
        btnNewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}