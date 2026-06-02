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
    private static final String QUOTE_URL = "https://v1.hitokoto.cn/";

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
                    Log.e(TAG, "网络请求失败: HTTP " + response.code());
                    callback.onError("网络请求失败");
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                if (responseBody.isEmpty()) {
                    Log.e(TAG, "响应体为空");
                    callback.onError("响应体为空");
                    return;
                }

                Log.d(TAG, "API响应: " + responseBody);

                try {
                    JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                    String quote = "";
                    String author = "";

                    if (jsonObject.has("hitokoto")) {
                        quote = jsonObject.get("hitokoto").getAsString();
                    } else if (jsonObject.has("content")) {
                        quote = jsonObject.get("content").getAsString();
                    } else if (jsonObject.has("text")) {
                        quote = jsonObject.get("text").getAsString();
                    } else if (jsonObject.has("quote")) {
                        quote = jsonObject.get("quote").getAsString();
                    } else if (jsonObject.has("data")) {
                        JsonObject data = jsonObject.getAsJsonObject("data");
                        if (data.has("content")) {
                            quote = data.get("content").getAsString();
                        } else if (data.has("text")) {
                            quote = data.get("text").getAsString();
                        }
                    }

                    if (jsonObject.has("from")) {
                        author = jsonObject.get("from").getAsString();
                    } else if (jsonObject.has("author")) {
                        author = jsonObject.get("author").getAsString();
                    } else if (jsonObject.has("source")) {
                        author = jsonObject.get("source").getAsString();
                    }

                    if (quote.isEmpty()) {
                        Log.e(TAG, "未找到语录内容字段");
                        callback.onError("解析失败");
                        return;
                    }

                    callback.onSuccess(quote, author);

                } catch (Exception e) {
                    Log.e(TAG, "JSON解析异常", e);
                    callback.onError("解析异常");
                }

            } catch (IOException e) {
                Log.e(TAG, "网络请求异常", e);
                callback.onError("网络请求异常");
            } catch (Exception e) {
                Log.e(TAG, "未知异常", e);
                callback.onError("未知异常");
            }
        }).start();
    }
}
