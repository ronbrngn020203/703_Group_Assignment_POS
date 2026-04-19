package com.ais.cafeteria.pos.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ais.cafeteria.pos.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        animateContent();

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DURATION);
    }

    private void animateContent() {
        ImageView logo = findViewById(R.id.ivLogo);
        TextView title = findViewById(R.id.appTitleText);
        TextView tagline = findViewById(R.id.taglineText);
        View divider = findViewById(R.id.dividerLine);

        if (logo == null || title == null || tagline == null || divider == null) return;

        logo.setAlpha(0f);
        logo.setScaleX(0.8f);
        logo.setScaleY(0.8f);
        title.setAlpha(0f);
        tagline.setAlpha(0f);
        divider.setAlpha(0f);

        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(
                ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(logo, "scaleX", 0.8f, 1f),
                ObjectAnimator.ofFloat(logo, "scaleY", 0.8f, 1f)
        );
        logoAnim.setDuration(700);
        logoAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        divider.animate().alpha(1f).setStartDelay(600).setDuration(400).start();
        title.animate().alpha(1f).translationYBy(-10f).setStartDelay(800).setDuration(500).start();
        tagline.animate().alpha(1f).setStartDelay(1000).setDuration(500).start();

        logoAnim.start();
        animateLoadingDots();
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