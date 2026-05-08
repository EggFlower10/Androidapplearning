package com.example.myapplication.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuoteService {
    private static final String TAG = "QuoteService";
    private static final String QUOTE_URL = "https://api.quotable.io/random";

    private final OkHttpClient client;
    private final Gson gson;

    public QuoteService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public interface QuoteCallback {
        void onSuccess(String quote, String author);
        void onError(String error);
    }

    public void fetchQuote(QuoteCallback callback) {
        new Thread(() -> {
            Request request = new Request.Builder()
                    .url(QUOTE_URL)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    callback.onError("网络请求失败: " + response.code());
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                String quote = jsonObject.get("content").getAsString();
                String author = jsonObject.get("author").getAsString();

                callback.onSuccess(quote, author);

            } catch (IOException e) {
                Log.e(TAG, "网络请求异常", e);
                callback.onError("网络请求异常: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "解析异常", e);
                callback.onError("解析异常: " + e.getMessage());
            }
        }).start();
    }
}