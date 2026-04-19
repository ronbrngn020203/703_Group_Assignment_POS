package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.utils.CartManager;

import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private RadioButton rbCard, rbCash, rbWallet;
    private LinearLayout cardDetailsSection;
    private TextView tvAmountDue, tvSubtotal, tvGst, tvTotal;
    private Button btnConfirmPayment;
    private ImageView btnBack;
    private String selectedMethod = "Card";

    private static final double GST_RATE = 0.15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        rbCard             = findViewById(R.id.rbCard);
        rbCash             = findViewById(R.id.rbCash);
        rbWallet           = findViewById(R.id.rbWallet);
        cardDetailsSection = findViewById(R.id.cardDetailsSection);
        tvAmountDue        = findViewById(R.id.tvAmountDue);
        tvSubtotal         = findViewById(R.id.tvSubtotal);
        tvGst              = findViewById(R.id.tvGst);
        tvTotal            = findViewById(R.id.tvTotal);
        btnConfirmPayment  = findViewById(R.id.btnConfirmPayment);
        btnBack            = findViewById(R.id.btnBack);

        double total = CartManager.getInstance().getTotal();
        updateAmountDisplay(total);

        selectMethod("Card");

        findViewById(R.id.cardPayCard).setOnClickListener(v -> selectMethod("Card"));
        rbCard.setOnClickListener(v -> selectMethod("Card"));

        findViewById(R.id.cardPayCash).setOnClickListener(v -> selectMethod("Cash"));
        rbCash.setOnClickListener(v -> selectMethod("Cash"));

        findViewById(R.id.cardPayWallet).setOnClickListener(v -> selectMethod("Wallet"));
        rbWallet.setOnClickListener(v -> selectMethod("Wallet"));

        btnConfirmPayment.setOnClickListener(v -> processPayment());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void updateAmountDisplay(double totalWithGst) {
        double subtotal = totalWithGst / (1 + GST_RATE);
        double gst = totalWithGst - subtotal;

        tvAmountDue.setText(String.format(Locale.getDefault(), "$%.2f", totalWithGst));
        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        tvGst.setText(String.format(Locale.getDefault(), "$%.2f", gst));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", totalWithGst));
    }

    private void selectMethod(String method) {
        selectedMethod = method;

        rbCard.setChecked(false);
        rbCash.setChecked(false);
        rbWallet.setChecked(false);

        switch (method) {
            case "Card":
                rbCard.setChecked(true);
                cardDetailsSection.setVisibility(View.VISIBLE);
                // Update button for card payment
                btnConfirmPayment.setText("✓  Confirm Payment");
                break;
            case "Cash":
                rbCash.setChecked(true);
                cardDetailsSection.setVisibility(View.GONE);
                // Update button to show cash instruction
                btnConfirmPayment.setText("📍  Place Order — Pay at Counter");
                break;
            case "Wallet":
                rbWallet.setChecked(true);
                cardDetailsSection.setVisibility(View.GONE);
                // Update button for wallet payment
                btnConfirmPayment.setText("✓  Confirm Payment");
                break;
        }
    }

    private void processPayment() {
        if (selectedMethod.equals("Card")) {
            EditText etCardNumber = findViewById(R.id.etCardNumber);
            EditText etExpiry     = findViewById(R.id.etExpiry);
            EditText etCvv        = findViewById(R.id.etCvv);

            if (etCardNumber.getText().toString().trim().isEmpty()) {
                etCardNumber.setError("Card number required");
                etCardNumber.requestFocus();
                return;
            }
            if (etExpiry.getText().toString().trim().isEmpty()) {
                etExpiry.setError("Expiry required");
                return;
            }
            if (etCvv.getText().toString().trim().isEmpty()) {
                etCvv.setError("CVV required");
                return;
            }
        }

        // ── Set status based on payment method ───────────────
        String paymentStatus;
        String toastMessage;

        if (selectedMethod.equals("Cash")) {
            paymentStatus = "Pending";
            toastMessage  = "Order placed! Please pay at the counter.";
        } else {
            paymentStatus = "Completed";
            toastMessage  = "Payment successful!";
        }

        // Pass status to CartManager and Receipt
        CartManager.getInstance().placeOrder(selectedMethod);
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, ReceiptActivity.class);
        intent.putExtra("payment_status", paymentStatus);
        intent.putExtra("payment_method", selectedMethod);
        finishAffinity();
        startActivity(new Intent(this, MainActivity.class));
        startActivity(intent);
    }
}