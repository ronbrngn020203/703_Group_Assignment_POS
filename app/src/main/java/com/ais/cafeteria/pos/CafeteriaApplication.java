package com.ais.cafeteria.pos;

import android.app.Application;

import org.conscrypt.Conscrypt;

import java.security.Security;

/**
 * Installs Conscrypt as the preferred JSSE provider so HTTPS (AIS News, APIs) uses a
 * modern TLS stack; helps devices that fail certificate validation with the default provider.
 */
public class CafeteriaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Security.insertProviderAt(Conscrypt.newProvider(), 1);
        } catch (Throwable ignored) {
            // If Conscrypt fails to load, the app still runs with the platform SSL implementation.
        }
    }
}
