package com.ecospend.app.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeepSeekApiClient {

    private static final String TAG = "DeepSeekApiClient";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "openrouter/free";
    // API key should be set by user in settings; using placeholder
    private String apiKey;

    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface AiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public DeepSeekApiClient(String apiKey) {
        this.apiKey = apiKey;
        client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void analyzeSpending(String spendingContext, AiCallback callback) {
        if (apiKey == null || apiKey.isEmpty()
                || apiKey.equals("YOUR_DEEPSEEK_API_KEY")
                || (!apiKey.startsWith("sk-or-") && !apiKey.startsWith("sk-"))) {
            callback.onError("Please enter a valid OpenRouter API key (starts with sk-or-...) in Profile > Settings.");
            return;
        }

        executor.execute(() -> {
            try {
                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content",
                    "You are EcoSpend's friendly AI financial advisor. Analyze spending data and provide " +
                    "concise, actionable, encouraging advice. Keep responses under 300 words. " +
                    "Use simple language. Format with short paragraphs. Start with a brief summary, " +
                    "then 2-3 specific tips, then an encouraging closing line.");

                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", spendingContext);

                JSONArray messages = new JSONArray();
                messages.put(systemMsg);
                messages.put(userMsg);

                JSONObject body = new JSONObject();
                body.put("model", MODEL);
                body.put("messages", messages);
                body.put("max_tokens", 500);
                body.put("temperature", 0.7);

                RequestBody requestBody = RequestBody.create(
                        body.toString(),
                        MediaType.parse("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("X-Title", "EcoSpend")
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseStr = response.body().string();
                        Log.d(TAG, "Raw response: " + responseStr); // helpful for debugging
                        JSONObject json = new JSONObject(responseStr);
                        JSONObject message = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message");

                        // Some free models (e.g. R1) return reasoning_content
                        // and set content to null — fall back gracefully
                        String content = null;
                        if (!message.isNull("content")) {
                            content = message.getString("content");
                        }
                        if ((content == null || content.trim().isEmpty())
                                && !message.isNull("reasoning_content")) {
                            content = message.getString("reasoning_content");
                        }
                        if (content == null || content.trim().isEmpty()) {
                            content = "No response received. Try again.";
                        }

                        final String finalContent = content;
                        mainHandler.post(() -> callback.onSuccess(finalContent));
                    } else {
                        String errBody = response.body() != null ? response.body().string() : "Unknown error";
                        Log.e(TAG, "API error: " + response.code() + " " + errBody);
                        mainHandler.post(() -> callback.onError("AI service error (" + response.code() + "). Check your API key."));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "DeepSeek API error", e);
                mainHandler.post(() -> callback.onError("Connection error: " + e.getMessage()));
            }
        });
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
