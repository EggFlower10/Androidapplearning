package com.example.myapplication.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.entity.Note;

import java.util.List;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY sortOrder ASC, updateTime DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes ORDER BY sortOrder ASC")
    List<Note> getAllNotesBySortOrder();

    @Query("SELECT * FROM notes WHERE category = :category ORDER BY sortOrder ASC, updateTime DESC")
    List<Note> getNotesByCategory(String category);

    @Query("SELECT * FROM notes WHERE title LIKE :keyword OR content LIKE :keyword ORDER BY sortOrder ASC, updateTime DESC")
    List<Note> searchNotes(String keyword);

    @Query("SELECT * FROM notes WHERE category = :category AND (title LIKE :keyword OR content LIKE :keyword) ORDER BY sortOrder ASC, updateTime DESC")
    List<Note> searchNotesByCategory(String category, String keyword);

    @Query("SELECT * FROM notes ORDER BY " +
           "CASE priority WHEN 'high' THEN 3 WHEN 'medium' THEN 2 ELSE 1 END DESC, updateTime DESC")
    List<Note> getNotesSortedByPriority();

    @Query("SELECT * FROM notes ORDER BY updateTime ASC")
    List<Note> getAllNotesByCreateTimeAsc();

    @Query("SELECT * FROM notes WHERE reminderTime > 0 AND reminderTime < :currentTime AND hasSentExpiredReminder = 0 ORDER BY reminderTime ASC")
    List<Note> getExpiredNotes(long currentTime);

    @Query("SELECT * FROM notes WHERE reminderTime > :currentTime AND hasSentExpiredReminder = 0 ORDER BY reminderTime ASC")
    List<Note> getUpcomingNotes(long currentTime);

    @Query("SELECT * FROM notes WHERE reminderTime > 0 AND reminderTime < :currentTime AND hasSentExpiredReminder = 0 ORDER BY reminderTime ASC LIMIT 1")
    Note getFirstExpiredNote(long currentTime);

    @Query("SELECT * FROM notes WHERE reminderTime > :currentTime AND hasSentExpiredReminder = 0 ORDER BY reminderTime ASC LIMIT 1")
    Note getNextPendingReminder(long currentTime);

    @Query("SELECT COUNT(*) FROM notes")
    int getNoteCount();

    @Query("DELETE FROM notes")
    void deleteAllNotes();

    @Insert
    long insertNote(Note note);

    @Update
    int updateNote(Note note);

    @Delete
    int deleteNote(Note note);

    @Query("DELETE FROM notes WHERE id = :id")
    int deleteNoteById(long id);
}
