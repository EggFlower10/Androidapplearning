package com.example.myapplication.utils;

import android.content.Context;

import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Message;
import com.example.myapplication.entity.MessageItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public final class MessageCenterRepository {
    public static final String RELATED_TYPE_NOTE = "note";
    public static final String RELATED_TYPE_TODO = "todo";
    public static final String RELATED_TYPE_NONE = "";

    private MessageCenterRepository() {
    }

    public static List<MessageItem> getMessages(Context context) {
        return runBlocking(() -> {
            List<Message> messages = getDatabase(context).messageDao().getAllMessages();
            List<MessageItem> items = new ArrayList<>();
            for (Message message : messages) {
                items.add(toMessageItem(message));
            }
            return items;
        }, new ArrayList<>());
    }

    public static void addMessage(Context context, String title, String summary, String category, boolean highlighted) {
        addMessage(context, title, summary, category, highlighted, RELATED_TYPE_NONE, 0L);
    }

    public static void addMessage(Context context, String title, String summary, String category, boolean highlighted,
                                  String relatedType, long relatedId) {
        runBlocking(() -> {
            Message message = new Message();
            message.setTitle(title);
            message.setSummary(summary);
            message.setTime(new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date()));
            message.setCategory(category);
            message.setUnread(true);
            message.setHighlighted(highlighted);
            message.setRelatedType(relatedType == null ? RELATED_TYPE_NONE : relatedType);
            message.setRelatedId(relatedId);
            message.setCreatedAt(System.currentTimeMillis());
            getDatabase(context).messageDao().insertMessage(message);
            return null;
        }, null);
    }

    public static void markAsRead(Context context, long id) {
        runBlocking(() -> {
            getDatabase(context).messageDao().markAsRead(id);
            return null;
        }, null);
    }

    public static void markAllAsRead(Context context) {
        runBlocking(() -> {
            getDatabase(context).messageDao().markAllAsRead();
            return null;
        }, null);
    }

    public static int getUnreadCount(Context context) {
        return runBlocking(() -> getDatabase(context).messageDao().getUnreadCount(), 0);
    }

    public static void deleteMessagesByRelated(Context context, String relatedType, long relatedId) {
        runBlocking(() -> {
            getDatabase(context).messageDao().deleteMessagesByRelated(relatedType, relatedId);
            return null;
        }, null);
    }

    public static void deleteMessagesByRelatedType(Context context, String relatedType) {
        runBlocking(() -> {
            getDatabase(context).messageDao().deleteMessagesByRelatedType(relatedType);
            return null;
        }, null);
    }

    public static void deleteNoteMessages(Context context, long noteId) {
        deleteMessagesByRelated(context, RELATED_TYPE_NOTE, noteId);
    }

    public static void deleteAllNoteMessages(Context context) {
        deleteMessagesByRelatedType(context, RELATED_TYPE_NOTE);
    }

    private static NoteDatabase getDatabase(Context context) {
        return NoteDatabase.getInstance(context.getApplicationContext());
    }

    private static MessageItem toMessageItem(Message message) {
        return new MessageItem(
                message.getId(),
                message.getTitle(),
                message.getSummary(),
                message.getTime(),
                message.getCategory(),
                message.isUnread(),
                message.isHighlighted(),
                message.getRelatedType(),
                message.getRelatedId()
        );
    }

    private static <T> T runBlocking(Callable<T> callable, T fallback) {
        FutureTask<T> task = new FutureTask<>(callable);
        Thread thread = new Thread(task, "message-center-repository");
        thread.start();
        try {
            return task.get();
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return fallback;
        }
    }
}
