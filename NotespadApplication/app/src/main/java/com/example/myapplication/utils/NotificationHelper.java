package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Note;
import com.example.myapplication.entity.Todo;
import com.example.myapplication.notification.NotificationManager;

import java.util.List;

public final class NotificationHelper {
    private NotificationHelper() {
    }

    public static void notifyNoteSaved(Context context, String title) {
        if (!isEnabled(context, "notify_save")) {
            return;
        }
        String displayTitle = isBlank(title) ? "未命名笔记" : title;
        String summary = "“" + displayTitle + "”已保存。";
        new NotificationManager(context).showNotification("笔记保存成功", summary);
        MessageCenterRepository.addMessage(context, "笔记保存成功", summary, "系统消息", false);
    }

    public static void notifyTodoSaved(Context context, String content) {
        if (!isEnabled(context, "notify_save")) {
            return;
        }
        String displayContent = isBlank(content) ? "未命名待办" : content;
        String summary = "“" + displayContent + "”已保存。";
        new NotificationManager(context).showNotification("待办保存成功", summary);
        MessageCenterRepository.addMessage(context, "待办保存成功", summary, "系统消息", false);
    }

    public static void notifyTodoReminderSet(Context context, String content, long deadline) {
        if (!isEnabled(context, "notify_todo") || deadline <= 0) {
            return;
        }
        String displayContent = isBlank(content) ? "未命名待办" : content;
        String deadlineText = TimeUtil.formatDateTime(deadline);
        String summary = "“" + displayContent + "”将在 " + deadlineText + " 提醒你。";
        new NotificationManager(context).showNotification("待办提醒已设置", summary);
        MessageCenterRepository.addMessage(context, "待办提醒已设置", summary, "待办提醒", true);
    }

    public static void notifyExpiredItemsIfNeeded(Context context) {
        if (!isEnabled(context, "notify_expired")) {
            return;
        }

        long now = System.currentTimeMillis();
        NoteDatabase database = NoteDatabase.getInstance(context);

        List<Note> expiredNotes = database.noteDao().getExpiredNotes(now);
        List<Todo> overdueTodos = database.todoDao().getOverdueTodos(now);

        if (!expiredNotes.isEmpty()) {
            String title = isBlank(expiredNotes.get(0).getTitle()) ? "未命名笔记" : expiredNotes.get(0).getTitle();
            String summary = "“" + title + "”已超过提醒时间。";
            new NotificationManager(context).showNotification("笔记已过期", summary);
            MessageCenterRepository.addMessage(context, "笔记已过期", summary, "过期提醒", true);
            return;
        }

        if (!overdueTodos.isEmpty()) {
            String content = isBlank(overdueTodos.get(0).getContent()) ? "未命名待办" : overdueTodos.get(0).getContent();
            String summary = "“" + content + "”已超过截止时间。";
            new NotificationManager(context).showNotification("待办已过期", summary);
            MessageCenterRepository.addMessage(context, "待办已过期", summary, "过期提醒", true);
        }
    }

    private static boolean isEnabled(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(ThemeManager.PREFERENCES_NAME, Context.MODE_PRIVATE);
        if ("notify_save".equals(key)) {
            return preferences.getBoolean(key, false);
        }
        return preferences.getBoolean(key, true);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
