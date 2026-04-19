package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.utils.CartManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvDate, tvGreeting, tvCartBadge, tvDrawerName;
    private FrameLayout btnCart;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fab;
    private DrawerLayout drawerLayout;
    private LinearLayout btnHamburger;

    private static final String PREF_NAME = "AIS_POS_PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDate       = findViewById(R.id.tvDate);
        tvGreeting   = findViewById(R.id.tvGreeting);
        tvCartBadge  = findViewById(R.id.tvCartBadge);
        btnCart      = findViewById(R.id.btnCart);
        bottomNav    = findViewById(R.id.bottomNav);
        fab          = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawerLayout);
        btnHamburger = findViewById(R.id.btnHamburger);
        tvDrawerName = findViewById(R.id.tvDrawerName);

        String date = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
                .format(new Date()).toUpperCase(Locale.getDefault());
        tvDate.setText(date);

        String staffId = getIntent().getStringExtra("staff_id");
        String name = (staffId != null && !staffId.isEmpty()) ? staffId : "Guest";
        tvGreeting.setText(getTimeBasedGreeting() + ", " + name + "!");
        tvDrawerName.setText(name);

        updateCafeStatus();

        btnHamburger.setOnClickListener(v ->
                drawerLayout.openDrawer(findViewById(R.id.navDrawer)));

        findViewById(R.id.drawerHome).setOnClickListener(v ->
                drawerLayout.closeDrawers());

        findViewById(R.id.drawerMenu).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            navigateTo(MenuActivity.class);
        });

        findViewById(R.id.drawerCart).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            navigateTo(CartActivity.class);
        });

        findViewById(R.id.drawerOrders).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            navigateTo(OrderHistoryActivity.class);
        });

        findViewById(R.id.drawerGallery).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            navigateTo(GalleryActivity.class);
        });

        findViewById(R.id.drawerFindUs).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            navigateTo(FindUsActivity.class);
        });

        findViewById(R.id.drawerContact).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            navigateTo(ContactActivity.class);
        });

        findViewById(R.id.drawerLogout).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                        boolean remember = prefs.getBoolean("remember_me", false);
                        if (!remember) {
                            prefs.edit().remove("staff_id").apply();
                        }
                        CartManager.getInstance().clearCart();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        CardView btnViewMenu     = findViewById(R.id.btnViewMenu);
        CardView btnViewCart     = findViewById(R.id.btnViewCart);
        CardView btnGallery      = findViewById(R.id.btnGallery);
        CardView btnFindUs       = findViewById(R.id.btnFindUs);
        CardView btnContact      = findViewById(R.id.btnContact);
        CardView btnOrderHistory = findViewById(R.id.btnOrderHistory);
        TextView btnOrderNow     = findViewById(R.id.btnOrderNow);

        btnViewMenu.setOnClickListener(v -> navigateTo(MenuActivity.class));
        btnViewCart.setOnClickListener(v -> navigateTo(CartActivity.class));
        btnGallery.setOnClickListener(v -> navigateTo(GalleryActivity.class));
        btnFindUs.setOnClickListener(v -> navigateTo(FindUsActivity.class));
        btnContact.setOnClickListener(v -> navigateTo(ContactActivity.class));
        btnOrderHistory.setOnClickListener(v -> navigateTo(OrderHistoryActivity.class));
        btnOrderNow.setOnClickListener(v -> navigateTo(MenuActivity.class));

        btnCart.setOnClickListener(v -> navigateTo(CartActivity.class));
        fab.setOnClickListener(v -> navigateTo(MenuActivity.class));

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                return true;
            } else if (id == R.id.nav_menu) {
                navigateTo(MenuActivity.class);
                return true;
            } else if (id == R.id.nav_orders) {
                navigateTo(OrderHistoryActivity.class);
                return true;
            }
            return false;
        });
    }

    private String getTimeBasedGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return "Good Morning";
        else if (hour >= 12 && hour < 17) return "Good Afternoon";
        else if (hour >= 17 && hour < 21) return "Good Evening";
        else return "Good Night";
    }

    private void updateCafeStatus() {
        LinearLayout statusPill = findViewById(R.id.statusPill);
        TextView tvStatus = null;

        if (statusPill != null && statusPill.getChildCount() > 1) {
            View child = statusPill.getChildAt(1);
            if (child instanceof TextView) {
                tvStatus = (TextView) child;
            }
        }

        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int totalMinutes = hour * 60 + minute;

        boolean isOpen = false;
        String statusText;

        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
            // Mon-Fri: 8:00 AM - 2:30 PM
            if (totalMinutes >= 480 && totalMinutes < 870) {
                isOpen = true;
                statusText = "OPEN · Closes 2:30 PM";
            } else if (totalMinutes >= 870) {
                statusText = "CLOSED · Opens Tomorrow 8:00 AM";
            } else {
                statusText = "CLOSED · Opens at 8:00 AM";
            }
        } else if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            // Sat-Sun: 9:00 AM - 3:00 PM
            if (totalMinutes >= 540 && totalMinutes < 900) {
                isOpen = true;
                statusText = "OPEN · Closes 3:00 PM";
            } else if (totalMinutes >= 900) {
                statusText = "CLOSED · Opens Tomorrow 9:00 AM";
            } else {
                statusText = "CLOSED · Opens at 9:00 AM";
            }
        } else {
            statusText = "CLOSED";
        }

        if (tvStatus != null) tvStatus.setText(statusText);

        if (statusPill != null && statusPill.getChildCount() > 0) {
            View dot = statusPill.getChildAt(0);
            dot.setBackgroundColor(Color.parseColor(isOpen ? "#27AE60" : "#E74C3C"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        tvGreeting.setText(getTimeBasedGreeting() + ", " +
                tvDrawerName.getText().toString() + "!");
        updateCafeStatus();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(findViewById(R.id.navDrawer))) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    private void updateCartBadge() {
        int count = CartManager.getInstance().getCartCount();
        if (count > 0) {
            tvCartBadge.setVisibility(View.VISIBLE);
            tvCartBadge.setText(String.valueOf(count));
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void navigateTo(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}