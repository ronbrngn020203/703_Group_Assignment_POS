package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ais.cafeteria.pos.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etStaffId, etPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin, btnGuestLogin;
    private TextView tvForgotPassword;

    private SharedPreferences prefs;
    private static final String PREF_NAME = "AIS_POS_PREFS";
    private static final String KEY_STAFF_ID = "staff_id";
    private static final String KEY_CURRENT_STAFF_ID = "current_staff_id";
    private static final String KEY_REMEMBER = "remember_me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Find views
        etStaffId      = findViewById(R.id.etStaffId);
        etPassword     = findViewById(R.id.etPassword);
        cbRememberMe   = findViewById(R.id.cbRememberMe);
        btnLogin       = findViewById(R.id.btnLogin);
        btnGuestLogin  = findViewById(R.id.btnGuestLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Restore remembered staff ID
        loadSavedCredentials();

        // Sign In button
        btnLogin.setOnClickListener(v -> handleLogin());

        // Guest Login button — goes straight to MainActivity as Guest
        btnGuestLogin.setOnClickListener(v -> {
            saveCurrentStaffId("Guest");
            Toast.makeText(this, "Welcome, Guest!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("staff_id", "Guest");
            startActivity(intent);
            finish();
        });

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Please contact IT Support at it@ais.ac.nz", Toast.LENGTH_LONG).show();
        });

        // Validate fields on focus loss
        etStaffId.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && TextUtils.isEmpty(etStaffId.getText())) {
                etStaffId.setError("Staff ID is required");
            }
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && TextUtils.isEmpty(etPassword.getText())) {
                etPassword.setError("Password is required");
            }
        });
    }

    private void handleLogin() {
        String staffId = etStaffId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(staffId)) {
            etStaffId.setError("Please enter your Staff ID");
            etStaffId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 4) {
            etPassword.setError("Password must be at least 4 characters");
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CURRENT_STAFF_ID, staffId);
        if (cbRememberMe.isChecked()) {
            editor.putString(KEY_STAFF_ID, staffId);
            editor.putBoolean(KEY_REMEMBER, true);
        } else {
            editor.remove(KEY_STAFF_ID);
            editor.putBoolean(KEY_REMEMBER, false);
        }
        editor.apply();

        Toast.makeText(this, "Welcome, " + staffId + "!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("staff_id", staffId);
        startActivity(intent);
        finish();
    }

    private void loadSavedCredentials() {
        boolean remember = prefs.getBoolean(KEY_REMEMBER, false);
        if (remember) {
            String savedId = prefs.getString(KEY_STAFF_ID, "");
            etStaffId.setText(savedId);
            cbRememberMe.setChecked(true);
        }
    }

    private void saveCurrentStaffId(String staffId) {
        prefs.edit().putString(KEY_CURRENT_STAFF_ID, staffId).apply();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
