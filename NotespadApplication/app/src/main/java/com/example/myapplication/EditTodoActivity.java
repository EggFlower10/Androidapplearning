package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Todo;
import com.example.myapplication.utils.ExpiredReminderScheduler;
import com.example.myapplication.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditTodoActivity extends AppCompatActivity {
    private NoteDatabase database;
    private Todo currentTodo;

    private EditText etContent;
    private EditText etNote;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvNoteCount;
    private TextView tvSave;
    private TextView tvRepeatValue;

    private LinearLayout llUncompleted;
    private LinearLayout llCompleted;
    private LinearLayout llRepeat;
    private ImageView ivUncompleted;
    private ImageView ivCompleted;

    private TextView tvPriorityLow;
    private TextView tvPriorityMedium;
    private TextView tvPriorityHigh;

    private long deadlineTime = 0;
    private boolean completed = false;
    private String priority = "low";
    private String repeatType = "none";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_todo);

        database = NoteDatabase.getInstance(this);
        initViews();
        setupListeners();

        long todoId = getIntent().getLongExtra("todo_id", -1);
        if (todoId != -1) {
            loadTodo(todoId);
        } else {
            updateRepeatValue();
        }
    }

    private void initViews() {
        etContent = findViewById(R.id.et_content);
        etNote = findViewById(R.id.et_note);
        tvDate = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);
        tvNoteCount = findViewById(R.id.tv_note_count);
        tvSave = findViewById(R.id.tv_save);
        tvRepeatValue = findViewById(R.id.tv_repeat_value);

        llUncompleted = findViewById(R.id.ll_uncompleted);
        llCompleted = findViewById(R.id.ll_completed);
        llRepeat = findViewById(R.id.ll_repeat);
        ivUncompleted = findViewById(R.id.iv_uncompleted);
        ivCompleted = findViewById(R.id.iv_completed);

        tvPriorityLow = findViewById(R.id.tv_priority_low);
        tvPriorityMedium = findViewById(R.id.tv_priority_medium);
        tvPriorityHigh = findViewById(R.id.tv_priority_high);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        etNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvNoteCount.setText(s.length() + "字");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        llUncompleted.setOnClickListener(v -> setCompleted(false));
        llCompleted.setOnClickListener(v -> setCompleted(true));
        llRepeat.setOnClickListener(v -> showRepeatOptions());

        tvPriorityLow.setOnClickListener(v -> setPriority("low"));
        tvPriorityMedium.setOnClickListener(v -> setPriority("medium"));
        tvPriorityHigh.setOnClickListener(v -> setPriority("high"));

        tvDate.setOnClickListener(v -> showDatePicker());
        tvTime.setOnClickListener(v -> showTimePicker());
        tvSave.setOnClickListener(v -> saveTodo());
        findViewById(R.id.btn_delete).setOnClickListener(v -> showDeleteDialog());
    }

    private void loadTodo(long todoId) {
        new Thread(() -> {
            currentTodo = database.todoDao().getTodoById(todoId);
            if (currentTodo != null) {
                runOnUiThread(() -> {
                    etContent.setText(currentTodo.getContent());
                    etNote.setText(currentTodo.getTag() != null ? currentTodo.getTag() : "");
                    tvNoteCount.setText((currentTodo.getTag() != null ? currentTodo.getTag().length() : 0) + "字");

                    if (currentTodo.getDeadline() > 0) {
                        deadlineTime = currentTodo.getDeadline();
                        tvDate.setText(dateFormat.format(new Date(deadlineTime)));
                        tvTime.setText(timeFormat.format(new Date(deadlineTime)));
                    }

                    repeatType = currentTodo.getRepeatType() != null ? currentTodo.getRepeatType() : "none";
                    updateRepeatValue();
                    setCompleted(currentTodo.isCompleted());
                    setPriority(currentTodo.getPriority() != null ? currentTodo.getPriority() : "low");
                });
            }
        }).start();
    }

    private void setCompleted(boolean completedValue) {
        completed = completedValue;
        if (completedValue) {
            llCompleted.setBackgroundColor(getResources().getColor(R.color.green_light));
            llUncompleted.setBackgroundColor(getResources().getColor(R.color.grey_light));
            ivCompleted.setImageResource(R.drawable.ic_radio_selected);
            ivUncompleted.setImageResource(R.drawable.ic_radio_unselected);
        } else {
            llUncompleted.setBackgroundColor(getResources().getColor(R.color.green_light));
            llCompleted.setBackgroundColor(getResources().getColor(R.color.grey_light));
            ivUncompleted.setImageResource(R.drawable.ic_radio_selected);
            ivCompleted.setImageResource(R.drawable.ic_radio_unselected);
        }
    }

    private void setPriority(String value) {
        priority = value;

        tvPriorityLow.setBackgroundColor(getResources().getColor(R.color.grey_light));
        tvPriorityMedium.setBackgroundColor(getResources().getColor(R.color.grey_light));
        tvPriorityHigh.setBackgroundColor(getResources().getColor(R.color.grey_light));

        tvPriorityLow.setTextColor(getResources().getColor(R.color.grey_text));
        tvPriorityMedium.setTextColor(getResources().getColor(R.color.grey_text));
        tvPriorityHigh.setTextColor(getResources().getColor(R.color.grey_text));

        switch (value) {
            case "low":
                tvPriorityLow.setBackgroundColor(getResources().getColor(R.color.primary_green));
                tvPriorityLow.setTextColor(getResources().getColor(R.color.white));
                break;
            case "medium":
                tvPriorityMedium.setBackgroundColor(getResources().getColor(R.color.orange));
                tvPriorityMedium.setTextColor(getResources().getColor(R.color.white));
                break;
            case "high":
                tvPriorityHigh.setBackgroundColor(getResources().getColor(R.color.red));
                tvPriorityHigh.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (deadlineTime > 0) {
            calendar.setTimeInMillis(deadlineTime);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            deadlineTime = calendar.getTimeInMillis();
            tvDate.setText(dateFormat.format(new Date(deadlineTime)));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (deadlineTime > 0) {
            calendar.setTimeInMillis(deadlineTime);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            deadlineTime = calendar.getTimeInMillis();
            tvTime.setText(timeFormat.format(new Date(deadlineTime)));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void showRepeatOptions() {
        String[] labels = {"不重复", "每天", "每周", "每月"};
        String[] values = {"none", "daily", "weekly", "monthly"};
        int checkedIndex = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(repeatType)) {
                checkedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("选择重复方式")
                .setSingleChoiceItems(labels, checkedIndex, (dialog, which) -> {
                    repeatType = values[which];
                    updateRepeatValue();
                    dialog.dismiss();
                })
                .show();
    }

    private void updateRepeatValue() {
        if (tvRepeatValue == null) {
            return;
        }
        switch (repeatType) {
            case "daily":
                tvRepeatValue.setText("每天");
                break;
            case "weekly":
                tvRepeatValue.setText("每周");
                break;
            case "monthly":
                tvRepeatValue.setText("每月");
                break;
            default:
                tvRepeatValue.setText("不重复");
                break;
        }
    }

    private void saveTodo() {
        String content = etContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入待办事项", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            if (currentTodo == null) {
                currentTodo = new Todo();
            }

            currentTodo.setContent(content);
            currentTodo.setTag(etNote.getText().toString().trim());
            currentTodo.setDeadline(deadlineTime);
            currentTodo.setPriority(priority);
            currentTodo.setCompleted(completed);
            currentTodo.setRepeatType(repeatType);
            currentTodo.setHasSentExpiredReminder(false);
            currentTodo.setUpdateTime(System.currentTimeMillis());

            if (currentTodo.getId() == 0) {
                long todoId = database.todoDao().insertTodo(currentTodo);
                currentTodo.setId(todoId);
            } else {
                database.todoDao().updateTodo(currentTodo);
            }

            NotificationHelper.notifyTodoSaved(EditTodoActivity.this, currentTodo.getId(), content);
            NotificationHelper.notifyTodoReminderSet(EditTodoActivity.this, currentTodo.getId(), content, deadlineTime);
            ExpiredReminderScheduler.scheduleNextCheck(EditTodoActivity.this);

            runOnUiThread(() -> {
                Toast.makeText(EditTodoActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void showDeleteDialog() {
        if (currentTodo == null) {
            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage("确定删除这条待办吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteTodo())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteTodo() {
        new Thread(() -> {
            database.todoDao().deleteTodo(currentTodo);
            ExpiredReminderScheduler.scheduleNextCheck(EditTodoActivity.this);
            runOnUiThread(() -> {
                Toast.makeText(EditTodoActivity.this, "已删除", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
