package com.example.myapplication.entity;

public class MessageItem {
    private final int id;
    private final String title;
    private final String summary;
    private final String time;
    private final String category;
    private final boolean unread;
    private final boolean highlighted;

    public MessageItem(int id, String title, String summary, String time, String category, boolean unread, boolean highlighted) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.time = time;
        this.category = category;
        this.unread = unread;
        this.highlighted = highlighted;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getTime() {
        return time;
    }

    public String getCategory() {
        return category;
    }

    public boolean isUnread() {
        return unread;
    }

    public boolean isHighlighted() {
        return highlighted;
    }
}
