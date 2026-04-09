package com.ais.cafeteria.pos.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ais.cafeteria.pos.R;


public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Animate dots
        animateLoadingDots();

        // Auto-navigate to login after delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DURATION);
    }


    private void animateLoadingDots() {
        View dot1 = findViewById(R.id.dot1);
        View dot2 = findViewById(R.id.dot2);
        View dot3 = findViewById(R.id.dot3);

        if (dot1 == null || dot2 == null || dot3 == null) return;

        animateDot(dot1, 0);
        animateDot(dot2, 300);
        animateDot(dot3, 600);
    }

    private void animateDot(View dot, long delay) {
        dot.setAlpha(0.3f);
        ObjectAnimator animator = ObjectAnimator.ofFloat(dot, "alpha", 0.3f, 1.0f, 0.3f);
        animator.setDuration(900);
        animator.setStartDelay(delay);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }
}
