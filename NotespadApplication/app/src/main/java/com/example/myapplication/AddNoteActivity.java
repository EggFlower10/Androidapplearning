package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

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
    private Button btnSave;
    private TextView tvCharCount;
    private LinearLayout bottomSheet;

    private NoteDatabase noteDatabase;
    private Note currentNote;

    private String selectedPriority = "low";
    private long reminderTime = 0;
    private boolean isSaveButtonHighlighted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        noteDatabase = NoteDatabase.getInstance(this);

        initViews();
        setupAnimations();

        long noteId = getIntent().getLongExtra("note_id", -1);
        if (noteId != -1) {
            loadNote(noteId);
        }

        updateEditTime();
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(R.anim.fade_in_slide_up, R.anim.fade_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out_slide_down);
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        spCategory = findViewById(R.id.sp_category);
        btnPriorityHigh = findViewById(R.id.btn_priority_high);
        btnPriorityMedium = findViewById(R.id.btn_priority_medium);
        btnPriorityLow = findViewById(R.id.btn_priority_low);
        btnReminder = findViewById(R.id.btn_reminder);
        btnSave = findViewById(R.id.btn_save);
        tvCharCount = findViewById(R.id.tv_char_count);
        bottomSheet = findViewById(R.id.bottom_sheet);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            animateSaveButton();
            saveNote();
        });

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_options, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        btnPriorityHigh.setOnClickListener(v -> setPriority("high"));
        btnPriorityMedium.setOnClickListener(v -> setPriority("medium"));
        btnPriorityLow.setOnClickListener(v -> setPriority("low"));

        btnReminder.setOnClickListener(v -> showDateTimePicker());

        setupCharCounter();
        setupToolbarActions();
        setupBottomSheet();
    }

    private void setupAnimations() {
        btnSave.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                animateButtonPress(btnSave, true);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                animateButtonPress(btnSave, false);
            }
            return false;
        });
    }

    private void animateButtonPress(View view, boolean isPressed) {
        ScaleAnimation scaleAnimation;
        if (isPressed) {
            scaleAnimation = new ScaleAnimation(1f, 0.95f, 1f, 0.95f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        } else {
            scaleAnimation = new ScaleAnimation(0.95f, 1f, 0.95f, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        }
        scaleAnimation.setDuration(100);
        scaleAnimation.setFillAfter(true);
        view.startAnimation(scaleAnimation);
    }

    private void animateSaveButton() {
        isSaveButtonHighlighted = !isSaveButtonHighlighted;
        if (isSaveButtonHighlighted) {
            btnSave.setBackgroundResource(R.drawable.save_button_selector);
            btnSave.setTextColor(getResources().getColor(R.color.white));
        } else {
            btnSave.setBackgroundResource(android.R.color.transparent);
            btnSave.setTextColor(getResources().getColor(R.color.primary_green));
        }
    }

    private void setupCharCounter() {
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvCharCount.setText(length + "/500");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupToolbarActions() {
        ImageView btnBold = findViewById(R.id.btn_bold);
        ImageView btnAlign = findViewById(R.id.btn_align);
        ImageView btnQuickCategory = findViewById(R.id.btn_quick_category);

        btnBold.setOnClickListener(v -> {
            Toast.makeText(this, "加粗功能", Toast.LENGTH_SHORT).show();
        });

        btnAlign.setOnClickListener(v -> {
            Toast.makeText(this, "对齐功能", Toast.LENGTH_SHORT).show();
        });

        btnQuickCategory.setOnClickListener(v -> {
            showBottomSheet();
        });
    }

    private void setupBottomSheet() {
        findViewById(R.id.btn_cat_work).setOnClickListener(v -> selectCategory("工作"));
        findViewById(R.id.btn_cat_study).setOnClickListener(v -> selectCategory("学习"));
        findViewById(R.id.btn_cat_life).setOnClickListener(v -> selectCategory("生活"));
        findViewById(R.id.btn_cat_important).setOnClickListener(v -> selectCategory("重要"));
    }

    private void showBottomSheet() {
        bottomSheet.setVisibility(View.VISIBLE);
        bottomSheet.startAnimation(createSlideUpAnimation());
    }

    private void hideBottomSheet() {
        Animation slideDown = createSlideDownAnimation();
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomSheet.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        bottomSheet.startAnimation(slideDown);
    }

    private Animation createSlideUpAnimation() {
        ScaleAnimation slideUp = new ScaleAnimation(1f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
        slideUp.setDuration(200);
        slideUp.setFillAfter(true);
        return slideUp;
    }

    private Animation createSlideDownAnimation() {
        ScaleAnimation slideDown = new ScaleAnimation(1f, 1f, 1f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
        slideDown.setDuration(200);
        slideDown.setFillAfter(true);
        return slideDown;
    }

    private void selectCategory(String category) {
        String[] categories = getResources().getStringArray(R.array.category_options);
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                spCategory.setSelection(i);
                break;
            }
        }
        hideBottomSheet();
    }

    private void setPriority(String priority) {
        selectedPriority = priority;

        btnPriorityHigh.setTextColor(getResources().getColor(
                "high".equals(priority) ? R.color.white : R.color.text_primary));
        btnPriorityMedium.setTextColor(getResources().getColor(
                "medium".equals(priority) ? R.color.white : R.color.text_primary));
        btnPriorityLow.setTextColor(getResources().getColor(
                "low".equals(priority) ? R.color.white : R.color.text_primary));

        btnPriorityHigh.setBackgroundResource(
                "high".equals(priority) ? R.drawable.priority_button_selected : R.drawable.priority_button_bg);
        btnPriorityMedium.setBackgroundResource(
                "medium".equals(priority) ? R.drawable.priority_button_selected : R.drawable.priority_button_bg);
        btnPriorityLow.setBackgroundResource(
                "low".equals(priority) ? R.drawable.priority_button_selected : R.drawable.priority_button_bg);
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

    private void updateEditTime() {
        TextView tvEditTime = findViewById(R.id.tv_edit_time);
        Calendar calendar = Calendar.getInstance();
        String timeStr = String.format("%s%d-%02d-%02d %02d:%02d",
                getString(R.string.edit_time_hint),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
        tvEditTime.setText(timeStr);
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