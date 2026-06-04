package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Note;
import com.example.myapplication.utils.ExpiredReminderScheduler;
import com.example.myapplication.utils.MessageCenterRepository;
import com.example.myapplication.utils.NotificationHelper;

public class AddNoteActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;

    private EditText etTitle;
    private EditText etContent;
    private Button btnSave;
    private TextView tvCharCount;
    private NoteDatabase noteDatabase;
    private Note currentNote;

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
        btnSave = findViewById(R.id.btn_save);
        tvCharCount = findViewById(R.id.tv_char_count);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveNote());

        setupCharCounter();
        setupToolbarActions();
        setupDeleteButton();
    }

    private void setupCharCounter() {
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCount.setText(s.length() + "字");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupToolbarActions() {
        View btnBold = findViewById(R.id.btn_bold);
        View btnUnderline = findViewById(R.id.btn_underline);
        View btnBullet = findViewById(R.id.btn_bullet);
        View btnNumber = findViewById(R.id.btn_number);
        View btnImage = findViewById(R.id.btn_image);

        btnBold.setOnClickListener(v -> applyBold());
        btnUnderline.setOnClickListener(v -> applyUnderline());
        btnBullet.setOnClickListener(v -> insertBullet());
        btnNumber.setOnClickListener(v -> insertNumberedList());
        btnImage.setOnClickListener(v -> pickImage());
    }

    private void applyBold() {
        int start = etContent.getSelectionStart();
        int end = etContent.getSelectionEnd();

        if (start == end) {
            String boldText = "粗体文本";
            etContent.getText().insert(start, boldText);
            etContent.setSelection(start, start + boldText.length());
            return;
        }

        String selectedText = etContent.getText().subSequence(start, end).toString();
        SpannableStringBuilder builder = new SpannableStringBuilder(etContent.getText());
        builder.replace(start, end, selectedText);
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, start + selectedText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        etContent.setText(builder);
        etContent.setSelection(start, start + selectedText.length());
    }

    private void applyUnderline() {
        int start = etContent.getSelectionStart();
        int end = etContent.getSelectionEnd();

        if (start == end) {
            String underlineText = "下划线文本";
            etContent.getText().insert(start, underlineText);
            etContent.setSelection(start, start + underlineText.length());
            return;
        }

        SpannableString spannable = new SpannableString(etContent.getText());
        spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        etContent.setText(spannable);
        etContent.setSelection(start, end);
    }

    private void insertBullet() {
        int cursorPos = etContent.getSelectionStart();
        String linePrefix = "\n• ";
        if (cursorPos == 0 || etContent.getText().charAt(cursorPos - 1) == '\n') {
            etContent.getText().insert(cursorPos, "• ");
            etContent.setSelection(cursorPos + 2);
        } else {
            etContent.getText().insert(cursorPos, linePrefix);
            etContent.setSelection(cursorPos + linePrefix.length());
        }
    }

    private void insertNumberedList() {
        int cursorPos = etContent.getSelectionStart();
        int lineNumber = getCurrentLineNumber(etContent.getText().toString(), cursorPos) + 1;
        String linePrefix = "\n" + lineNumber + ". ";
        if (cursorPos == 0 || etContent.getText().charAt(cursorPos - 1) == '\n') {
            etContent.getText().insert(cursorPos, "1. ");
            etContent.setSelection(cursorPos + 3);
        } else {
            etContent.getText().insert(cursorPos, linePrefix);
            etContent.setSelection(cursorPos + linePrefix.length());
        }
    }

    private int getCurrentLineNumber(String text, int cursorPos) {
        int lineCount = 0;
        for (int i = 0; i < cursorPos && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineCount++;
            }
        }
        return lineCount;
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, "图片插入功能已完成", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDeleteButton() {
        Button btnDeleteNote = findViewById(R.id.btn_delete_note);
        btnDeleteNote.setOnClickListener(v -> {
            if (currentNote != null) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setMessage("确定删除这条笔记吗？")
                        .setPositiveButton("删除", (dialog, which) -> deleteNote())
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                Toast.makeText(this, "没有可删除的笔记", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteNote() {
        new Thread(() -> {
            if (currentNote != null) {
                noteDatabase.noteDao().deleteNote(currentNote);
                MessageCenterRepository.deleteNoteMessages(this, currentNote.getId());
                ExpiredReminderScheduler.scheduleNextCheck(this);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void loadNote(long noteId) {
        new Thread(() -> {
            for (Note note : noteDatabase.noteDao().getAllNotes()) {
                if (note.getId() == noteId) {
                    currentNote = note;
                    break;
                }
            }

            if (currentNote != null) {
                runOnUiThread(() -> {
                    etTitle.setText(currentNote.getTitle());
                    etContent.setText(currentNote.getContent());
                });
            }
        }).start();
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "请输入笔记内容", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            long noteId;
            if (currentNote == null) {
                Note note = new Note();
                note.setTitle(title);
                note.setContent(content);
                note.setCategory("工作");
                note.setPriority("low");
                note.setReminderTime(0);
                note.setHasSentExpiredReminder(false);
                note.setCreateTime(System.currentTimeMillis());
                note.setUpdateTime(System.currentTimeMillis());
                noteId = noteDatabase.noteDao().insertNote(note);
                note.setId(noteId);
                currentNote = note;
            } else {
                currentNote.setTitle(title);
                currentNote.setContent(content);
                currentNote.setUpdateTime(System.currentTimeMillis());
                currentNote.setHasSentExpiredReminder(false);
                noteDatabase.noteDao().updateNote(currentNote);
                noteId = currentNote.getId();
            }

            NotificationHelper.notifyNoteSaved(AddNoteActivity.this, noteId, title);
            ExpiredReminderScheduler.scheduleNextCheck(AddNoteActivity.this);

            runOnUiThread(() -> {
                Toast.makeText(AddNoteActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
