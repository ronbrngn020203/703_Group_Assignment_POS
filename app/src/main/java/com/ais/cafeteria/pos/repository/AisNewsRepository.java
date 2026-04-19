package com.ais.cafeteria.pos.repository;

import com.ais.cafeteria.pos.models.AisNewsItem;
import com.ais.cafeteria.pos.network.RetrofitClient;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AisNewsRepository {

    public interface OnNewsLoadedCallback {
        void onSuccess(List<AisNewsItem> items);
        void onError(String message);
    }

    private static final String AIS_NEWS_URL = "https://www.ais.ac.nz/news";

    /** Some networks block default OkHttp; a browser-like UA avoids odd HTTP responses. */
    private static final String NEWS_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/120.0.0.0 Mobile Safari/537.36";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build();

    public void fetchLatestNews(int maxItems, OnNewsLoadedCallback callback) {
        fetchNewsInternal(AIS_NEWS_URL, maxItems, callback, true);
    }

    /**
     * Order: OkHttp → (if AIS URL fails) Jsoup HTTPS → (optional) cafeteria backend proxy.
     * Conscrypt (see {@link com.ais.cafeteria.pos.CafeteriaApplication}) improves direct TLS.
     */
    private void fetchNewsInternal(String url, int maxItems, OnNewsLoadedCallback callback,
                                   boolean allowBackendFallback) {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", NEWS_USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-NZ,en;q=0.9")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (allowBackendFallback && isDirectAisNews(call.request().url())) {
                    tryJsoupThenMaybeProxy(maxItems, callback);
                    return;
                }
                String proxyUrl = buildBackendProxyUrl();
                if (allowBackendFallback && proxyUrl != null
                        && !call.request().url().toString().equals(proxyUrl)) {
                    fetchNewsInternal(proxyUrl, maxItems, callback, false);
                    return;
                }
                callback.onError("Unable to load AIS updates: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                HttpUrl reqUrl = call.request().url();
                boolean alreadyFromProxy = "/api/ais-news-html".equals(reqUrl.encodedPath());

                if (!response.isSuccessful()) {
                    if (allowBackendFallback && isDirectAisNews(reqUrl)) {
                        response.close();
                        tryJsoupThenMaybeProxy(maxItems, callback);
                        return;
                    }
                    String proxyUrl = buildBackendProxyUrl();
                    if (allowBackendFallback && proxyUrl != null && !alreadyFromProxy) {
                        response.close();
                        fetchNewsInternal(proxyUrl, maxItems, callback, false);
                        return;
                    }
                    callback.onError(httpErrorMessage(response.code(), alreadyFromProxy));
                    return;
                }

                String html = response.body() != null ? response.body().string() : "";
                List<AisNewsItem> items = parseNewsItems(html, maxItems);

                if (items.isEmpty()) {
                    String proxyUrl = buildBackendProxyUrl();
                    if (allowBackendFallback && proxyUrl != null && !alreadyFromProxy) {
                        fetchNewsInternal(proxyUrl, maxItems, callback, false);
                        return;
                    }
                    callback.onError("No AIS updates were found (page layout may have changed).");
                    return;
                }

                callback.onSuccess(items);
            }
        });
    }

    /** Jsoup uses HttpURLConnection; sometimes succeeds when OkHttp fails. Then optional Node proxy. */
    private void tryJsoupThenMaybeProxy(int maxItems, OnNewsLoadedCallback callback) {
        new Thread(() -> {
            try {
                Document doc = jsoupFetchNewsWithOneRetry();
                List<AisNewsItem> items = parseNewsItems(doc.html(), maxItems);
                if (!items.isEmpty()) {
                    callback.onSuccess(items);
                    return;
                }
            } catch (HttpStatusException e) {
                int code = e.getStatusCode();
                if (code >= 500) {
                    callback.onError(serverSideNewsMessage(code));
                    return;
                }
            } catch (Exception ignored) {
            }
            String proxyUrl = buildBackendProxyUrl();
            if (proxyUrl != null) {
                fetchNewsInternal(proxyUrl, maxItems, callback, false);
            } else {
                callback.onError(
                        "Unable to load AIS updates. Check your connection, or run the cafeteria "
                                + "backend (node backend/server.js) for an offline proxy.");
            }
        }, "ais-news-jsoup").start();
    }

    private static Document jsoupFetchNewsWithOneRetry() throws IOException {
        try {
            return jsoupFetchNews();
        } catch (HttpStatusException first) {
            int c = first.getStatusCode();
            if (c < 500 || c >= 600) {
                throw first;
            }
            try {
                Thread.sleep(2_000L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw first;
            }
            return jsoupFetchNews();
        }
    }

    private static Document jsoupFetchNews() throws IOException {
        return Jsoup.connect(AIS_NEWS_URL)
                .userAgent(NEWS_USER_AGENT)
                .timeout(30_000)
                .maxBodySize(0)
                .followRedirects(true)
                .get();
    }

    private static boolean isDirectAisNews(HttpUrl url) {
        if (url == null) {
            return false;
        }
        if (!"www.ais.ac.nz".equalsIgnoreCase(url.host())) {
            return false;
        }
        String path = url.encodedPath();
        return "/news".equals(path) || "/news/".equals(path);
    }

    private String buildBackendProxyUrl() {
        String base = RetrofitClient.getBaseUrl();
        if (base == null || base.trim().isEmpty()) {
            return null;
        }
        HttpUrl parsed = HttpUrl.parse(base.trim());
        if (parsed == null) {
            return null;
        }
        HttpUrl resolved = parsed.resolve("/api/ais-news-html");
        return resolved != null ? resolved.toString() : null;
    }

    private static String httpErrorMessage(int code, boolean alreadyFromProxy) {
        if (code >= 500 && code < 600) {
            return serverSideNewsMessage(code);
        }
        if (code == 404 && alreadyFromProxy) {
            return "AIS updates: optional news proxy not found (404). Run: node backend/server.js "
                    + "or ignore if the feed already loads via the website.";
        }
        if (code == 404) {
            return "AIS news page was not found (404). The site may have moved — try \"Open AIS News\".";
        }
        return "AIS updates request failed with code " + code;
    }

    /**
     * 502/503 from the live site or from the Node proxy both mean the AIS server (or its gateway)
     * did not return a normal page — usually temporary.
     */
    private static String serverSideNewsMessage(int code) {
        return "The AIS news website is not available right now (HTTP " + code + ", bad gateway / "
                + "server error). Wait a few minutes and tap Refresh, or use \"Open AIS News\" to try "
                + "in the browser.";
    }

    private List<AisNewsItem> parseNewsItems(String html, int maxItems) {
        List<AisNewsItem> items = new ArrayList<>();
        Document document = Jsoup.parse(html, AIS_NEWS_URL);
        Elements rows = document.select("section.blog-row");

        for (Element row : rows) {
            String title = row.select(".NewsSummaryLink h2").text().trim();
            if (title.isEmpty()) {
                title = row.select(".NewsSummaryLink a").attr("title").trim();
            }
            String summary = row.select(".NewsSummarySummary").text().trim();
            String publishedDate = row.select(".NewsSummaryPostdate").text().replace("Posted on:", "").trim();
            String tag = row.select("a.tag").text().trim();
            String articleUrl = row.select(".NewsSummaryMorelink a").attr("abs:href").trim();
            if (articleUrl.isEmpty()) {
                articleUrl = row.select(".NewsSummaryLink a[href]").attr("abs:href").trim();
            }

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
