package com.ecospend.app.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
    // Using free open API — no key required
    private static final String BASE_URL = "https://open.er-api.com/v6/latest/";

    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;

    // Cache rates to avoid excessive API calls
    private static Map<String, Double> cachedRates = new HashMap<>();
    private static String cachedBase = "";
    private static long cacheTimestamp = 0;
    private static final long CACHE_DURATION_MS = 10 * 60 * 1000; // 10 minutes

    public interface RatesCallback {
        void onSuccess(Map<String, Double> rates, String baseCurrency);
        void onError(String error);
    }

    public interface ConvertCallback {
        void onSuccess(double convertedAmount, double rate);
        void onError(String error);
    }

    public CurrencyApiClient() {
        client = new OkHttpClient();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getRates(String baseCurrency, RatesCallback callback) {
        long now = System.currentTimeMillis();
        if (!cachedBase.isEmpty() && cachedBase.equals(baseCurrency)
                && (now - cacheTimestamp) < CACHE_DURATION_MS && !cachedRates.isEmpty()) {
            callback.onSuccess(new HashMap<>(cachedRates), baseCurrency);
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
                    while (keys.hasNext()) {
                        String key = keys.next();
                        ratesMap.put(key, rates.getDouble(key));
                    }
                    cachedRates = ratesMap;
                    cachedBase = baseCurrency;
                    cacheTimestamp = System.currentTimeMillis();
                    mainHandler.post(() -> callback.onSuccess(new HashMap<>(ratesMap), baseCurrency));
                } else {
                    mainHandler.post(() -> callback.onError("API error: " + response.code()));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching rates", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void convert(double amount, String fromCurrency, String toCurrency, ConvertCallback callback) {
        getRates(fromCurrency, new RatesCallback() {
            @Override
            public void onSuccess(Map<String, Double> rates, String baseCurrency) {
                if (rates.containsKey(toCurrency)) {
                    double rate = rates.get(toCurrency);
                    double converted = amount * rate;
                    callback.onSuccess(converted, rate);
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

    // Get the rate to MYR for storing normalized amounts
    public void convertToMYR(double amount, String fromCurrency, ConvertCallback callback) {
        if (fromCurrency.equals("MYR")) {
            callback.onSuccess(amount, 1.0);
            return;
        }
        convert(amount, fromCurrency, "MYR", callback);
    }
}
