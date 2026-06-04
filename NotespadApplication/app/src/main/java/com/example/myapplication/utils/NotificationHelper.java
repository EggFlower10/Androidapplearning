package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Note;
import com.example.myapplication.entity.Todo;
import com.example.myapplication.notification.NotificationManager;

import java.util.List;

public final class NotificationHelper {
    private static final String CATEGORY_SYSTEM = "系统消息";
    private static final String CATEGORY_TODO_REMINDER = "待办提醒";
    private static final String CATEGORY_EXPIRED = "过期提醒";

    private NotificationHelper() {
    }

    public static void notifyNoteSaved(Context context, long noteId, String title) {
        if (!isEnabled(context, "notify_save")) {
            return;
        }
        String displayTitle = isBlank(title) ? "未命名笔记" : title;
        String summary = "“" + displayTitle + "”已保存。";
        new NotificationManager(context).showNotification("笔记保存成功", summary);
        MessageCenterRepository.addMessage(
                context,
                "笔记保存成功",
                summary,
                CATEGORY_SYSTEM,
                false,
                MessageCenterRepository.RELATED_TYPE_NOTE,
                noteId
        );
    }

    public static void notifyTodoSaved(Context context, long todoId, String content) {
        if (!isEnabled(context, "notify_save")) {
            return;
        }
        String displayContent = isBlank(content) ? "未命名待办" : content;
        String summary = "“" + displayContent + "”已保存。";
        new NotificationManager(context).showNotification("待办保存成功", summary);
        MessageCenterRepository.addMessage(
                context,
                "待办保存成功",
                summary,
                CATEGORY_SYSTEM,
                false,
                MessageCenterRepository.RELATED_TYPE_TODO,
                todoId
        );
    }

    public static void notifyTodoReminderSet(Context context, long todoId, String content, long deadline) {
        if (!isEnabled(context, "notify_todo") || deadline <= 0) {
            return;
        }
        String displayContent = isBlank(content) ? "未命名待办" : content;
        String deadlineText = TimeUtil.formatDateTime(deadline);
        String summary = "“" + displayContent + "”将在 " + deadlineText + " 提醒你。";
        new NotificationManager(context).showNotification("待办提醒已设置", summary);
        MessageCenterRepository.addMessage(
                context,
                "待办提醒已设置",
                summary,
                CATEGORY_TODO_REMINDER,
                true,
                MessageCenterRepository.RELATED_TYPE_TODO,
                todoId
        );
    }

    public static void notifyExpiredItemsIfNeeded(Context context) {
        if (!isEnabled(context, "notify_expired")) {
            return;
        }

        long now = System.currentTimeMillis();
        NoteDatabase database = NoteDatabase.getInstance(context);

        List<Note> expiredNotes = database.noteDao().getExpiredNotes(now);
        for (Note expiredNote : expiredNotes) {
            String title = isBlank(expiredNote.getTitle()) ? "未命名笔记" : expiredNote.getTitle();
            String summary = "“" + title + "”已超过提醒时间。";
            new NotificationManager(context).showNotification("笔记已过期", summary);
            MessageCenterRepository.addMessage(
                    context,
                    "笔记已过期",
                    summary,
                    CATEGORY_EXPIRED,
                    true,
                    MessageCenterRepository.RELATED_TYPE_NOTE,
                    expiredNote.getId()
            );
            expiredNote.setHasSentExpiredReminder(true);
            database.noteDao().updateNote(expiredNote);
        }

        List<Todo> overdueTodos = database.todoDao().getOverdueTodos(now);
        for (Todo overdueTodo : overdueTodos) {
            String content = isBlank(overdueTodo.getContent()) ? "未命名待办" : overdueTodo.getContent();
            String summary = "“" + content + "”已超过截止时间。";
            new NotificationManager(context).showNotification("待办已过期", summary);
            MessageCenterRepository.addMessage(
                    context,
                    "待办已过期",
                    summary,
                    CATEGORY_EXPIRED,
                    true,
                    MessageCenterRepository.RELATED_TYPE_TODO,
                    overdueTodo.getId()
            );
            overdueTodo.setHasSentExpiredReminder(true);
            database.todoDao().updateTodo(overdueTodo);
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
