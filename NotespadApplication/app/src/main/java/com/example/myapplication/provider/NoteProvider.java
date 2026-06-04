package com.example.myapplication.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Note;
import com.example.myapplication.utils.ExpiredReminderScheduler;
import com.example.myapplication.utils.MessageCenterRepository;

import java.util.ArrayList;
import java.util.List;

public class NoteProvider extends ContentProvider {
    private static final String AUTHORITY = "com.example.myapplication.provider";
    private static final String PATH_NOTES = "notes";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_NOTES);

    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, PATH_NOTES, NOTES);
        uriMatcher.addURI(AUTHORITY, PATH_NOTES + "/#", NOTE_ID);
    }

    private NoteDatabase noteDatabase;

    @Override
    public boolean onCreate() {
        noteDatabase = NoteDatabase.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = uriMatcher.match(uri);
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "title", "content", "category", "priority", "reminderTime", "createTime", "updateTime"});

        new Thread(() -> {
            List<Note> notes;
            switch (match) {
                case NOTES:
                    notes = noteDatabase.noteDao().getAllNotes();
                    break;
                case NOTE_ID:
                    long queryId = ContentUris.parseId(uri);
                    notes = filterNotesById(noteDatabase.noteDao().getAllNotes(), queryId);
                    break;
                default:
                    notes = new ArrayList<>();
            }

            for (Note note : notes) {
                cursor.addRow(new Object[]{
                        note.getId(),
                        note.getTitle(),
                        note.getContent(),
                        note.getCategory(),
                        note.getPriority(),
                        note.getReminderTime(),
                        note.getCreateTime(),
                        note.getUpdateTime()
                });
            }
        }).run();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Log.e("NoteProvider", "Query interrupted", e);
        }

        return cursor;
    }

    private List<Note> filterNotesById(List<Note> allNotes, long id) {
        List<Note> filtered = new ArrayList<>();
        for (Note note : allNotes) {
            if (note.getId() == id) {
                filtered.add(note);
                break;
            }
        }
        return filtered;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + PATH_NOTES;
            case NOTE_ID:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + PATH_NOTES;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = uriMatcher.match(uri);
        if (match != NOTES) {
            throw new IllegalArgumentException("Invalid URI for insert: " + uri);
        }

        Note note = new Note();
        if (values != null) {
            note.setTitle(values.getAsString("title"));
            note.setContent(values.getAsString("content"));
            note.setCategory(values.getAsString("category"));
            note.setPriority(values.getAsString("priority"));
            note.setReminderTime(values.getAsLong("reminderTime"));
            note.setCreateTime(System.currentTimeMillis());
            note.setUpdateTime(System.currentTimeMillis());
        }

        long id = noteDatabase.noteDao().insertNote(note);
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        int deleted = 0;

        switch (match) {
            case NOTE_ID:
                long deleteId = ContentUris.parseId(uri);
                deleted = noteDatabase.noteDao().deleteNoteById(deleteId);
                if (deleted > 0 && getContext() != null) {
                    MessageCenterRepository.deleteNoteMessages(getContext(), deleteId);
                    ExpiredReminderScheduler.scheduleNextCheck(getContext());
                }
                break;
            case NOTES:
                noteDatabase.noteDao().deleteAllNotes();
                if (getContext() != null) {
                    MessageCenterRepository.deleteAllNoteMessages(getContext());
                    ExpiredReminderScheduler.scheduleNextCheck(getContext());
                }
                deleted = 1;
                break;
            default:
                throw new IllegalArgumentException("Invalid URI for delete: " + uri);
        }

        if (getContext() != null && deleted > 0) {
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }

        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        if (match != NOTE_ID) {
            throw new IllegalArgumentException("Invalid URI for update: " + uri);
        }

        long updateId = ContentUris.parseId(uri);
        List<Note> notes = filterNotesById(noteDatabase.noteDao().getAllNotes(), updateId);

        if (notes.isEmpty()) {
            return 0;
        }

        Note note = notes.get(0);
        if (values != null) {
            if (values.containsKey("title")) {
                note.setTitle(values.getAsString("title"));
            }
            if (values.containsKey("content")) {
                note.setContent(values.getAsString("content"));
            }
            if (values.containsKey("category")) {
                note.setCategory(values.getAsString("category"));
            }
            if (values.containsKey("priority")) {
                note.setPriority(values.getAsString("priority"));
            }
            if (values.containsKey("reminderTime")) {
                note.setReminderTime(values.getAsLong("reminderTime"));
            }
            note.setUpdateTime(System.currentTimeMillis());
        }

        int updated = noteDatabase.noteDao().updateNote(note);

        if (getContext() != null && updated > 0) {
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }

        return updated;
    }
}
