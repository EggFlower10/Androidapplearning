package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.TodoAdapter;
import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Todo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoActivity extends AppCompatActivity {
    private RecyclerView recyclerViewTodo;
    private TodoAdapter todoAdapter;
    private NoteDatabase database;
    private EditText etTodoContent;
    private Button btnDeadline;
    private Spinner spinnerTag;
    private Button btnAddTodo;
    private Button btnFilter;
    private Button btnTodoBack;

    private List<Todo> todos = new ArrayList<>();
    private int filterMode = 0; // 0: 全部, 1: 未完成, 2: 已完成
    private long currentDeadline = 0;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        database = NoteDatabase.getInstance(this);

        initViews();
        setupRecyclerView();
        loadTodos();
    }

    private void initViews() {
        btnTodoBack = findViewById(R.id.btnTodoBack);
        btnTodoBack.setOnClickListener(v -> finish());

        btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> showFilterDialog());

        etTodoContent = findViewById(R.id.etTodoContent);
        btnDeadline = findViewById(R.id.btnDeadline);
        spinnerTag = findViewById(R.id.spinnerTag);
        btnAddTodo = findViewById(R.id.btnAddTodo);

        ArrayAdapter<CharSequence> tagAdapter = ArrayAdapter.createFromResource(this,
                R.array.tag_options, android.R.layout.simple_spinner_item);
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTag.setAdapter(tagAdapter);

        btnDeadline.setOnClickListener(v -> showDateTimePicker());
        btnAddTodo.setOnClickListener(v -> addTodo());

        etTodoContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                btnAddTodo.setEnabled(s.toString().trim().length() > 0);
            }
        });
    }

    private void setupRecyclerView() {
        recyclerViewTodo = findViewById(R.id.recyclerViewTodo);
        recyclerViewTodo.setLayoutManager(new LinearLayoutManager(this));

        todoAdapter = new TodoAdapter();
        recyclerViewTodo.setAdapter(todoAdapter);

        todoAdapter.setOnTodoClickListener(new TodoAdapter.OnTodoClickListener() {
            @Override
            public void onTodoClick(Todo todo) {
                showEditDialog(todo);
            }

            @Override
            public void onTodoLongClick(Todo todo) {
                showDeleteDialog(todo);
            }

            @Override
            public void onTodoCompleteClick(Todo todo, boolean completed) {
                toggleTodoComplete(todo, completed);
            }
        });
    }

    private void loadTodos() {
        new Thread(() -> {
            List<Todo> loadedTodos;
            switch (filterMode) {
                case 1:
                    loadedTodos = database.todoDao().getUncompletedTodos();
                    break;
                case 2:
                    loadedTodos = database.todoDao().getCompletedTodos();
                    break;
                default:
                    loadedTodos = database.todoDao().getAllTodos();
                    break;
            }
            todos.clear();
            todos.addAll(loadedTodos);

            runOnUiThread(() -> todoAdapter.setTodos(todos));
        }).start();
    }

    private void addTodo() {
        String content = etTodoContent.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        String tag = spinnerTag.getSelectedItem().toString();

        new Thread(() -> {
            Todo todo = new Todo(content, tag, currentDeadline);
            database.todoDao().insertTodo(todo);

            runOnUiThread(() -> {
                etTodoContent.setText("");
                currentDeadline = 0;
                btnDeadline.setText(R.string.set_deadline);
                loadTodos();
                Toast.makeText(this, R.string.todo_added, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void toggleTodoComplete(Todo todo, boolean completed) {
        new Thread(() -> {
            todo.setCompleted(completed);
            database.todoDao().updateTodo(todo);

            runOnUiThread(() -> {
                loadTodos();
                if (completed) {
                    Toast.makeText(this, R.string.todo_completed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.todo_uncompleted, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showFilterDialog() {
        String[] filters = {getString(R.string.filter_all), getString(R.string.filter_uncompleted), getString(R.string.filter_completed)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("筛选")
                .setSingleChoiceItems(filters, filterMode, (dialog, which) -> {
                    filterMode = which;
                    btnFilter.setText(filters[which]);
                    loadTodos();
                    dialog.dismiss();
                })
                .show();
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (currentDeadline > 0) {
            calendar.setTimeInMillis(currentDeadline);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                currentDeadline = calendar.getTimeInMillis();

                btnDeadline.setText(dateFormat.format(new Date(currentDeadline)));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showEditDialog(Todo todo) {
        EditText etContent = new EditText(this);
        etContent.setText(todo.getContent());
        etContent.setHint("请输入待办内容");
        etContent.setPadding(48, 24, 48, 24);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("编辑待办")
                .setView(etContent)
                .setPositiveButton("保存", (dialog1, which) -> {
                    String newContent = etContent.getText().toString().trim();
                    if (!newContent.isEmpty()) {
                        new Thread(() -> {
                            todo.setContent(newContent);
                            todo.setUpdateTime(System.currentTimeMillis());
                            database.todoDao().updateTodo(todo);
                            runOnUiThread(() -> {
                                loadTodos();
                                Toast.makeText(this, R.string.todo_updated, Toast.LENGTH_SHORT).show();
                            });
                        }).start();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    private void showDeleteDialog(Todo todo) {
        new AlertDialog.Builder(this)
                .setTitle("删除待办")
                .setMessage("确定要删除这个待办事项吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    new Thread(() -> {
                        database.todoDao().deleteTodo(todo);
                        runOnUiThread(() -> {
                            loadTodos();
                            Toast.makeText(this, R.string.todo_deleted, Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}