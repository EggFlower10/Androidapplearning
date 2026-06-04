package com.example.myapplication.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.entity.Message;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY createdAt DESC, id DESC")
    List<Message> getAllMessages();

    @Insert
    long insertMessage(Message message);

    @Query("UPDATE messages SET unread = 0 WHERE id = :id")
    void markAsRead(long id);

    @Query("UPDATE messages SET unread = 0 WHERE unread = 1")
    void markAllAsRead();

    @Query("SELECT COUNT(*) FROM messages WHERE unread = 1")
    int getUnreadCount();

    @Query("DELETE FROM messages WHERE relatedType = :relatedType AND relatedId = :relatedId")
    int deleteMessagesByRelated(String relatedType, long relatedId);

    @Query("DELETE FROM messages WHERE relatedType = :relatedType")
    int deleteMessagesByRelatedType(String relatedType);
}
