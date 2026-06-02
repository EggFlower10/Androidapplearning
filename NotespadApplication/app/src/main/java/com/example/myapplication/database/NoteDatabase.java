package com.example.myapplication.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.dao.NoteDao;
import com.example.myapplication.dao.TodoDao;
import com.example.myapplication.entity.Note;
import com.example.myapplication.entity.Todo;

@Database(entities = {Note.class, Todo.class}, version = 5, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    private static volatile NoteDatabase INSTANCE;

    public abstract NoteDao noteDao();

    public abstract TodoDao todoDao();

    public static NoteDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    NoteDatabase.class, "notes_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
