package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.adapters.AisNewsAdapter;
import com.ais.cafeteria.pos.repository.AisNewsRepository;

public class NewsFeedActivity extends AppCompatActivity {

    private static final String AIS_NEWS_URL = "https://www.ais.ac.nz/news";

    private ProgressBar progressBar;
    private TextView tvStatus;
    private RecyclerView recyclerView;
    private AisNewsAdapter adapter;
    private AisNewsRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnRefresh = findViewById(R.id.btnRefresh);
        Button btnOpenWebsite = findViewById(R.id.btnOpenWebsite);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        recyclerView = findViewById(R.id.recyclerNews);

        adapter = new AisNewsAdapter();
        repository = new AisNewsRepository();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnRefresh.setOnClickListener(v -> loadNews());
        btnOpenWebsite.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AIS_NEWS_URL))));

        loadNews();
    }

    private void loadNews() {
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Loading AIS updates...");
        recyclerView.setVisibility(View.GONE);

        repository.fetchLatestNews(6, new AisNewsRepository.OnNewsLoadedCallback() {
            @Override
            public void onSuccess(java.util.List<com.ais.cafeteria.pos.models.AisNewsItem> items) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.submitItems(items);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    tvStatus.setVisibility(View.VISIBLE);
                    tvStatus.setText(message);
                });
            }
        });
    }
}
