package com.example.myapplication.entity;

public class MessageItem {
    private final long id;
    private final String title;
    private final String summary;
    private final String time;
    private final String category;
    private final boolean unread;
    private final boolean highlighted;
    private final String relatedType;
    private final long relatedId;

    public MessageItem(long id, String title, String summary, String time, String category, boolean unread, boolean highlighted, String relatedType, long relatedId) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.time = time;
        this.category = category;
        this.unread = unread;
        this.highlighted = highlighted;
        this.relatedType = relatedType;
        this.relatedId = relatedId;
    }

    public long getId() {
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

    public String getRelatedType() {
        return relatedType;
    }

    public long getRelatedId() {
        return relatedId;
    }
}
