package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.entity.MessageItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class MessageCenterRepository {
    private static final String PREFS_NAME = "message_center";
    private static final String KEY_PREFIX_READ = "message_read_";
    private static final String KEY_DYNAMIC_MESSAGES = "dynamic_messages";

    private MessageCenterRepository() {
    }

    public static List<MessageItem> getMessages(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return readDynamicMessages(preferences);
    }

    public static void addMessage(Context context, String title, String summary, String category, boolean highlighted) {
        try {
            SharedPreferences preferences = getPreferences(context);
            JSONArray array = new JSONArray(preferences.getString(KEY_DYNAMIC_MESSAGES, "[]"));

            int nextId = array.length() == 0 ? 1 : array.getJSONObject(0).optInt("id", 0) + 1;
            JSONObject object = new JSONObject();
            object.put("id", nextId);
            object.put("title", title);
            object.put("summary", summary);
            object.put("time", new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date()));
            object.put("category", category);
            object.put("highlighted", highlighted);

            JSONArray updated = new JSONArray();
            updated.put(object);
            for (int i = 0; i < array.length(); i++) {
                updated.put(array.getJSONObject(i));
            }

            preferences.edit()
                    .putString(KEY_DYNAMIC_MESSAGES, updated.toString())
                    .putBoolean(KEY_PREFIX_READ + nextId, false)
                    .apply();
        } catch (Exception ignored) {
        }
    }

    public static void markAsRead(Context context, int id) {
        getPreferences(context).edit().putBoolean(KEY_PREFIX_READ + id, true).apply();
    }

    public static void markAllAsRead(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        for (MessageItem item : getMessages(context)) {
            editor.putBoolean(KEY_PREFIX_READ + item.getId(), true);
        }
        editor.apply();
    }

    public static int getUnreadCount(Context context) {
        int count = 0;
        for (MessageItem item : getMessages(context)) {
            if (item.isUnread()) {
                count++;
            }
        }
        return count;
    }

    private static List<MessageItem> readDynamicMessages(SharedPreferences preferences) {
        List<MessageItem> items = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(KEY_DYNAMIC_MESSAGES, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                int id = object.getInt("id");
                boolean unread = !preferences.getBoolean(KEY_PREFIX_READ + id, false);
                items.add(new MessageItem(
                        id,
                        object.optString("title"),
                        object.optString("summary"),
                        object.optString("time"),
                        object.optString("category"),
                        unread,
                        object.optBoolean("highlighted", false)
                ));
            }
        } catch (Exception ignored) {
        }
        return items;
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
