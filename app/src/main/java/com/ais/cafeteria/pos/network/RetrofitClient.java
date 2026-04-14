package com.ais.cafeteria.pos.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitClient  —  Middle-Tier HTTP client (singleton)
 *
 * BASE_URL points to the Node.js + SQLite backend.
 *  • Android Emulator  →  "http://10.0.2.2:3000/"  (maps to localhost on your PC)
 *  • Real device (same Wi-Fi) → replace with your PC's local IP, e.g. "http://192.168.1.5:3000/"
 *  • ngrok tunnel  →  "https://xxxx.ngrok.io/"
 */
public class RetrofitClient {

    // ── Change this URL to match your environment ──────────────
    private static final String BASE_URL = "http://10.0.2.2:3000/";

    private static Retrofit retrofitInstance = null;

    private RetrofitClient() {}

    /** Returns the shared Retrofit instance (creates it on first call). */
    public static Retrofit getInstance() {
        if (retrofitInstance == null) {
            // Logging interceptor — shows HTTP request/response in Logcat
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitInstance;
    }

    /** Convenience shortcut — returns a ready-to-use MenuApiService. */
    public static MenuApiService getMenuApiService() {
        return getInstance().create(MenuApiService.class);
    }
}