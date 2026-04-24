package com.ecospend.app.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ecospend.app.utils.Constants;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CurrencyApiClient {

    private static final String TAG = "CurrencyApiClient";
    private static final String BASE_URL = "https://open.er-api.com/v6/latest/";

    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final SharedPreferences prefs;

    // Runtime cache
    private static Map<String, Double> cachedRates = new HashMap<>();
    private static String cachedBase = "";
    private static long cacheTimestamp = 0;
    private static final long CACHE_DURATION_MS = 10 * 60 * 1000; // 10 minutes

    public interface RatesCallback {
        void onSuccess(Map<String, Double> rates, String baseCurrency, boolean isOffline, long lastSyncTimestamp);
        void onError(String error);
    }

    public interface ConvertCallback {
        void onSuccess(double convertedAmount, double rate, boolean isOffline);
        void onError(String error);
    }

    // Constructor now requires Context for SharedPreferences
    public CurrencyApiClient(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, 0);
        client = new OkHttpClient();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getRates(String baseCurrency, RatesCallback callback) {
        long now = System.currentTimeMillis();
        // Check runtime memory cache first
        if (!cachedBase.isEmpty() && cachedBase.equals(baseCurrency)
                && (now - cacheTimestamp) < CACHE_DURATION_MS && !cachedRates.isEmpty()) {
            callback.onSuccess(new HashMap<>(cachedRates), baseCurrency, false, cacheTimestamp);
            return;
        }

        executor.execute(() -> {
            String url = BASE_URL + baseCurrency;
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    JSONObject rates = json.getJSONObject("rates");

                    Map<String, Double> ratesMap = new HashMap<>();
                    java.util.Iterator<String> keys = rates.keys();

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("offline_master_base", baseCurrency);
                    long syncTime = System.currentTimeMillis();
                    editor.putLong("offline_master_timestamp", syncTime);

                    // Add the base itself as 1.0 to simplify offline cross-math later
                    editor.putFloat("offline_rate_" + baseCurrency, 1.0f);
                    ratesMap.put(baseCurrency, 1.0);

                    while (keys.hasNext()) {
                        String key = keys.next();
                        double rate = rates.getDouble(key);
                        ratesMap.put(key, rate);
                        editor.putFloat("offline_rate_" + key, (float) rate); // Cache everything
                    }
                    editor.apply();

                    cachedRates = ratesMap;
                    cachedBase = baseCurrency;
                    cacheTimestamp = syncTime;

                    mainHandler.post(() -> callback.onSuccess(new HashMap<>(ratesMap), baseCurrency, false, syncTime));
                } else {
                    handleOfflineFallback(baseCurrency, callback);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching rates", e);
                handleOfflineFallback(baseCurrency, callback);
            }
        });
    }

    // Handles generating rates for ANY currency using cross-multiplication from the Master Base
    private void handleOfflineFallback(String targetBaseCurrency, RatesCallback callback) {
        String masterBase = prefs.getString("offline_master_base", "");
        if (masterBase.isEmpty()) {
            mainHandler.post(() -> callback.onError("No internet connection and no offline data available."));
            return;
        }

        // Find the conversion rate from the Target Base to the Master Base
        float rateTargetToMaster = prefs.getFloat("offline_rate_" + targetBaseCurrency, -1.0f);

        if (rateTargetToMaster != -1.0f) {
            Map<String, Double> generatedRates = new HashMap<>();
            Map<String, ?> allEntries = prefs.getAll();

            // Loop through all saved rates and cross-multiply
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith("offline_rate_")) {
                    String currencyCode = entry.getKey().replace("offline_rate_", "");
                    float rateCurrencyToMaster = (Float) entry.getValue();

                    // Cross-rate formula: (Currency/Master) / (TargetBase/Master)
                    double crossRate = rateCurrencyToMaster / (double) rateTargetToMaster;
                    generatedRates.put(currencyCode, crossRate);
                }
            }

            long lastSync = prefs.getLong("offline_master_timestamp", 0);
            mainHandler.post(() -> callback.onSuccess(generatedRates, targetBaseCurrency, true, lastSync));
        } else {
            mainHandler.post(() -> callback.onError("Target currency not found in offline cache."));
        }
    }

    public void convert(double amount, String fromCurrency, String toCurrency, ConvertCallback callback) {
        if (fromCurrency.equals(toCurrency)) {
            callback.onSuccess(amount, 1.0, false);
            return;
        }

        getRates(fromCurrency, new RatesCallback() {
            @Override
            public void onSuccess(Map<String, Double> rates, String baseCurrency, boolean isOffline, long lastSyncTimestamp) {
                if (rates.containsKey(toCurrency)) {
                    double rate = rates.get(toCurrency);
                    double converted = amount * rate;
                    callback.onSuccess(converted, rate, isOffline);
                } else {
                    callback.onError("Target currency not found: " + toCurrency);
                }
            }
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}