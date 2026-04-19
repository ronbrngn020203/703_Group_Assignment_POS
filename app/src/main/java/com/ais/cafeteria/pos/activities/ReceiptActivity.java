package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.adapters.ReceiptAdapter;
import com.ais.cafeteria.pos.models.Order;
import com.ais.cafeteria.pos.repository.OrderRepository;
import com.ais.cafeteria.pos.utils.CartManager;

import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    private static final String TAG = "ReceiptActivity";
    private static final String PREF_NAME = "AIS_POS_PREFS";
    private static final String KEY_CURRENT_STAFF_ID = "current_staff_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        Order order = CartManager.getInstance().getLastOrder();

        if (order == null) {
            finish();
            return;
        }

        double orderSubtotal = order.getTotal();
        double orderTotal = Math.round(orderSubtotal * 1.15 * 100.0) / 100.0;
        order.setTotal(orderTotal);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String staffId = prefs.getString(KEY_CURRENT_STAFF_ID, "Guest");
        order.setStaffId(staffId);

        OrderRepository orderRepository = new OrderRepository();
        orderRepository.saveOrder(staffId, order, new OrderRepository.OnOrderSavedCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Order saved to Firebase: " + order.getOrderId());
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "❌ Failed to save order: " + message);
                // Retry once after 2 seconds
                new Handler().postDelayed(() ->
                        orderRepository.saveOrder(staffId, order, null), 2000);
            }
        });

        // ── Get payment status from intent ────────────────────
        String paymentStatus = getIntent().getStringExtra("payment_status");
        String paymentMethod = getIntent().getStringExtra("payment_method");
        if (paymentStatus == null) paymentStatus = "Completed";
        if (paymentMethod == null) paymentMethod = order.getPaymentMethod();

        // ── Populate receipt UI ───────────────────────────────
        TextView tvOrderId          = findViewById(R.id.tvOrderId);
        TextView tvSubtotal         = findViewById(R.id.tvSubtotal);
        TextView tvGst              = findViewById(R.id.tvGst);
        TextView tvTotal            = findViewById(R.id.tvTotal);
        TextView tvPayMethod        = findViewById(R.id.tvPayMethod);
        TextView tvNote             = findViewById(R.id.tvPayMethod);
        RecyclerView rvReceiptItems = findViewById(R.id.rvReceiptItems);
        TextView btnEmailReceipt    = findViewById(R.id.btnEmailReceipt);
        TextView btnPrint           = findViewById(R.id.btnPrint);
        TextView btnNewOrder        = findViewById(R.id.btnNewOrder);

        tvOrderId.setText(order.getOrderId());

        double total    = order.getTotal();
        double subtotal = total / 1.15;
        double gst      = total - subtotal;

        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        tvGst.setText(String.format(Locale.getDefault(), "$%.2f", gst));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));

        // ── Payment status ────────────────────────────────────
        if (paymentStatus.equals("Pending")) {
            tvPayMethod.setText("Payment: Cash — Please pay at the counter ⚠");
            tvPayMethod.setTextColor(Color.parseColor("#E67E22"));
        } else {
            tvPayMethod.setText("Payment: " + paymentMethod + " — Paid ✓");
            tvPayMethod.setTextColor(Color.parseColor("#27AE60"));
        }

        // ── Show note if exists ───────────────────────────────
        if (order.getNote() != null && !order.getNote().isEmpty()) {
            tvPayMethod.append("\n📝 Note: " + order.getNote());
        }

        ReceiptAdapter adapter = new ReceiptAdapter(order.getItems());
        rvReceiptItems.setLayoutManager(new LinearLayoutManager(this));
        rvReceiptItems.setAdapter(adapter);
        rvReceiptItems.setNestedScrollingEnabled(false);

        // ── Email receipt ─────────────────────────────────────
        final String finalPaymentStatus = paymentStatus;
        final String finalPaymentMethod = paymentMethod;
        btnEmailReceipt.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cafeteria@ais.ac.nz"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Manna Cafe Receipt " + order.getOrderId());
            emailIntent.putExtra(Intent.EXTRA_TEXT,
                    "Order: "    + order.getOrderId() +
                            "\nDate: "   + order.getDate() +
                            "\nTotal: $" + String.format(Locale.getDefault(), "%.2f", total) +
                            "\nPayment: " + finalPaymentMethod +
                            "\nStatus: "  + finalPaymentStatus +
                            (order.getNote() != null && !order.getNote().isEmpty() ?
                                    "\nNote: " + order.getNote() : "") +
                            (finalPaymentStatus.equals("Pending") ?
                                    "\n\n⚠ Please visit the counter to complete your cash payment." : "") +
                            "\n\nThank you for dining at Manna Cafe & Catering!" +
                            "\n28A Linwood Avenue, Mount Albert, Auckland 1025");
            try {
                startActivity(Intent.createChooser(emailIntent, "Send receipt via email"));
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // ── Print ─────────────────────────────────────────────
        btnPrint.setOnClickListener(v ->
                Toast.makeText(this, "Sending to printer…", Toast.LENGTH_SHORT).show());

        // ── New Order ─────────────────────────────────────────
        btnNewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
