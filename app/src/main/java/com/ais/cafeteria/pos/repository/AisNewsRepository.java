package com.ais.cafeteria.pos.repository;

import com.ais.cafeteria.pos.models.AisNewsItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AisNewsRepository {

    public interface OnNewsLoadedCallback {
        void onSuccess(List<AisNewsItem> items);
        void onError(String message);
    }

    private static final String AIS_NEWS_URL = "https://www.ais.ac.nz/news";

    private final OkHttpClient httpClient = new OkHttpClient();

    public void fetchLatestNews(int maxItems, OnNewsLoadedCallback callback) {
        Request request = new Request.Builder()
                .url(AIS_NEWS_URL)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Unable to load AIS updates: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("AIS updates request failed with code " + response.code());
                    return;
                }

                String html = response.body() != null ? response.body().string() : "";
                List<AisNewsItem> items = parseNewsItems(html, maxItems);

                if (items.isEmpty()) {
                    callback.onError("No AIS updates were found.");
                    return;
                }

                callback.onSuccess(items);
            }
        });
    }

    private List<AisNewsItem> parseNewsItems(String html, int maxItems) {
        List<AisNewsItem> items = new ArrayList<>();
        Document document = Jsoup.parse(html, AIS_NEWS_URL);
        Elements rows = document.select("section.blog-row");

        for (Element row : rows) {
            String title = row.select(".NewsSummaryLink h2").text().trim();
            String summary = row.select(".NewsSummarySummary").text().trim();
            String publishedDate = row.select(".NewsSummaryPostdate").text().replace("Posted on:", "").trim();
            String tag = row.select("a.tag").text().trim();
            String articleUrl = row.select(".NewsSummaryMorelink a").attr("abs:href").trim();

            if (title.isEmpty() || articleUrl.isEmpty()) {
                continue;
            }

            items.add(new AisNewsItem(tag, title, summary, publishedDate, articleUrl));

            if (items.size() >= maxItems) {
                break;
            }
        }

        return items;
    }
}
