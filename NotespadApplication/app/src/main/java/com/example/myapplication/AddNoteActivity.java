package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Note;

import java.util.Calendar;

public class AddNoteActivity extends AppCompatActivity {
    private EditText etTitle;
    private EditText etContent;
    private Spinner spCategory;
    private Button btnPriorityHigh;
    private Button btnPriorityMedium;
    private Button btnPriorityLow;
    private Button btnReminder;

    private NoteDatabase noteDatabase;
    private Note currentNote;

    private String selectedPriority = "low";
    private long reminderTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        noteDatabase = NoteDatabase.getInstance(this);

        initViews();

        long noteId = getIntent().getLongExtra("note_id", -1);
        if (noteId != -1) {
            loadNote(noteId);
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        spCategory = findViewById(R.id.sp_category);
        btnPriorityHigh = findViewById(R.id.btn_priority_high);
        btnPriorityMedium = findViewById(R.id.btn_priority_medium);
        btnPriorityLow = findViewById(R.id.btn_priority_low);
        btnReminder = findViewById(R.id.btn_reminder);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveNote());

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_options, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        btnPriorityHigh.setOnClickListener(v -> setPriority("high"));
        btnPriorityMedium.setOnClickListener(v -> setPriority("medium"));
        btnPriorityLow.setOnClickListener(v -> setPriority("low"));

        btnReminder.setOnClickListener(v -> showDateTimePicker());
    }

    private void setPriority(String priority) {
        selectedPriority = priority;
        btnPriorityHigh.setBackgroundColor(getResources().getColor(
                "high".equals(priority) ? R.color.priority_high : R.color.background_search));
        btnPriorityMedium.setBackgroundColor(getResources().getColor(
                "medium".equals(priority) ? R.color.priority_medium : R.color.background_search));
        btnPriorityLow.setBackgroundColor(getResources().getColor(
                "low".equals(priority) ? R.color.priority_low : R.color.background_search));
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (reminderTime > 0) {
            calendar.setTimeInMillis(reminderTime);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                reminderTime = calendar.getTimeInMillis();

                btnReminder.setText(formatDateTime(reminderTime));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private String formatDateTime(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return String.format("%d-%02d-%02d %02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

    private void loadNote(long noteId) {
        new Thread(() -> {
            currentNote = noteDatabase.noteDao().getAllNotes().stream()
                    .filter(note -> note.getId() == noteId)
                    .findFirst()
                    .orElse(null);

            if (currentNote != null) {
                runOnUiThread(() -> {
                    TextView tvTitle = findViewById(R.id.tv_title);
                    tvTitle.setText(R.string.title_edit_note);

                    etTitle.setText(currentNote.getTitle());
                    etContent.setText(currentNote.getContent());

                    String[] categories = getResources().getStringArray(R.array.category_options);
                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i].equals(currentNote.getCategory())) {
                            spCategory.setSelection(i);
                            break;
                        }
                    }

                    selectedPriority = currentNote.getPriority();
                    setPriority(selectedPriority);

                    reminderTime = currentNote.getReminderTime();
                    if (reminderTime > 0) {
                        btnReminder.setText(formatDateTime(reminderTime));
                    }
                });
            }
        }).start();
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "请输入笔记内容", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            if (currentNote == null) {
                Note note = new Note();
                note.setTitle(title);
                note.setContent(content);
                note.setCategory(category);
                note.setPriority(selectedPriority);
                note.setReminderTime(reminderTime);
                noteDatabase.noteDao().insertNote(note);
            } else {
                currentNote.setTitle(title);
                currentNote.setContent(content);
                currentNote.setCategory(category);
                currentNote.setPriority(selectedPriority);
                currentNote.setReminderTime(reminderTime);
                currentNote.setUpdateTime(System.currentTimeMillis());
                noteDatabase.noteDao().updateNote(currentNote);
            }

            runOnUiThread(() -> {
                Toast.makeText(AddNoteActivity.this, R.string.saved_success, Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}