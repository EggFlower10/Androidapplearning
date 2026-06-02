package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.TodoListAdapter;
import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Todo;
import com.example.myapplication.utils.NotificationHelper;
import com.example.myapplication.utils.TimeUtil;
import com.example.myapplication.utils.TodoRepeatHelper;

import java.util.ArrayList;
import java.util.List;

public class TodoListActivity extends AppCompatActivity {
    private RecyclerView rvTodos;
    private TodoListAdapter adapter;
    private List<Todo> todoList = new ArrayList<>();
    private List<Todo> filteredList = new ArrayList<>();
    private NoteDatabase noteDatabase;

    private TextView tvEdit;
    private LinearLayout llEditBar;
    private CheckBox cbSelectAll;
    private TextView tvDelete;
    private TextView tvMarkComplete;
    private TextView tvCancel;
    private TextView tvFilterAll;
    private TextView tvFilterUncompleted;
    private TextView tvFilterCompleted;
    private EditText etSearch;

    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        initViews();
        initDatabase();
        loadTodos();
        setupListeners();
    }

    private void initViews() {
        rvTodos = findViewById(R.id.rv_todos);
        tvEdit = findViewById(R.id.tv_edit);
        llEditBar = findViewById(R.id.ll_edit_bar);
        cbSelectAll = findViewById(R.id.cb_select_all);
        tvDelete = findViewById(R.id.tv_delete);
        tvMarkComplete = findViewById(R.id.tv_mark_complete);
        tvCancel = findViewById(R.id.tv_cancel);
        tvFilterAll = findViewById(R.id.tv_filter_all);
        tvFilterUncompleted = findViewById(R.id.tv_filter_uncompleted);
        tvFilterCompleted = findViewById(R.id.tv_filter_completed);
        etSearch = findViewById(R.id.et_search);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTodos();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        adapter = new TodoListAdapter(filteredList, new TodoListAdapter.OnTodoClickListener() {
            @Override
            public void onTodoClick(Todo todo) {
                Intent intent = new Intent(TodoListActivity.this, EditTodoActivity.class);
                intent.putExtra("todo_id", todo.getId());
                startActivity(intent);
            }

            @Override
            public void onTodoCompleteClick(Todo todo, boolean completed) {
                updateTodoCompleted(todo, completed);
            }
        });
        adapter.setOnSelectChangeListener(this::onSelectChange);
        rvTodos.setLayoutManager(new LinearLayoutManager(this));
        rvTodos.setAdapter(adapter);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void initDatabase() {
        noteDatabase = NoteDatabase.getInstance(this);
    }

    private void loadTodos() {
        new Thread(() -> {
            todoList = noteDatabase.todoDao().getAllTodos();
            filterTodos();
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                updateStats();
            });
        }).start();
    }

    private void filterTodos() {
        filteredList.clear();
        String searchText = etSearch != null ? etSearch.getText().toString().trim().toLowerCase() : "";

        for (Todo todo : todoList) {
            boolean matchesFilter = "all".equals(currentFilter)
                    || ("uncompleted".equals(currentFilter) && !todo.isCompleted())
                    || ("completed".equals(currentFilter) && todo.isCompleted());

            if (!matchesFilter) {
                continue;
            }

            if (searchText.isEmpty()) {
                filteredList.add(todo);
            } else {
                String content = todo.getContent() != null ? todo.getContent().toLowerCase() : "";
                if (content.contains(searchText)) {
                    filteredList.add(todo);
                }
            }
        }
    }

    private void updateStats() {
        int total = todoList.size();
        int completed = 0;
        for (Todo todo : todoList) {
            if (todo.isCompleted()) {
                completed++;
            }
        }
        TextView tvStats = findViewById(R.id.tv_stats);
        tvStats.setText("共 " + total + " 项待办，已完成 " + completed + " 项");
    }

    private void setupListeners() {
        tvEdit.setOnClickListener(v -> {
            if (adapter.isSelectMode()) {
                exitSelectMode();
            } else {
                enterSelectMode();
            }
        });

        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                adapter.selectAll();
            } else {
                adapter.deselectAll();
            }
        });

        tvDelete.setOnClickListener(v -> deleteSelectedTodos());
        tvMarkComplete.setOnClickListener(v -> markSelectedAsComplete());
        tvCancel.setOnClickListener(v -> exitSelectMode());

        tvFilterAll.setOnClickListener(v -> setFilter("all"));
        tvFilterUncompleted.setOnClickListener(v -> setFilter("uncompleted"));
        tvFilterCompleted.setOnClickListener(v -> setFilter("completed"));

        findViewById(R.id.fab_add).setOnClickListener(v -> startActivity(new Intent(this, EditTodoActivity.class)));
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        tvFilterAll.setBackgroundResource("all".equals(filter) ? R.color.filter_selected_bg : R.drawable.tag_grey_bg);
        tvFilterAll.setTextColor("all".equals(filter) ? getResources().getColor(R.color.white) : getResources().getColor(R.color.text_secondary));
        tvFilterUncompleted.setBackgroundResource("uncompleted".equals(filter) ? R.color.filter_selected_bg : R.drawable.tag_grey_bg);
        tvFilterUncompleted.setTextColor("uncompleted".equals(filter) ? getResources().getColor(R.color.white) : getResources().getColor(R.color.text_secondary));
        tvFilterCompleted.setBackgroundResource("completed".equals(filter) ? R.color.filter_selected_bg : R.drawable.tag_grey_bg);
        tvFilterCompleted.setTextColor("completed".equals(filter) ? getResources().getColor(R.color.white) : getResources().getColor(R.color.text_secondary));
        filterTodos();
        adapter.notifyDataSetChanged();
    }

    private void enterSelectMode() {
        adapter.setSelectMode(true);
        llEditBar.setVisibility(View.VISIBLE);
        tvEdit.setText("取消");
        cbSelectAll.setChecked(false);
    }

    private void exitSelectMode() {
        adapter.setSelectMode(false);
        llEditBar.setVisibility(View.GONE);
        tvEdit.setText("编辑");
        cbSelectAll.setChecked(false);
    }

    private void onSelectChange(int count) {
        cbSelectAll.setChecked(count > 0 && count == filteredList.size());
    }

    private void updateTodoCompleted(Todo todo, boolean completed) {
        new Thread(() -> {
            if (completed && TodoRepeatHelper.isRepeating(todo)) {
                TodoRepeatHelper.moveToNextOccurrence(todo);
                noteDatabase.todoDao().updateTodo(todo);
                NotificationHelper.notifyTodoReminderSet(TodoListActivity.this, todo.getContent(), todo.getDeadline());
                todoList = noteDatabase.todoDao().getAllTodos();
                runOnUiThread(() -> {
                    filterTodos();
                    adapter.notifyDataSetChanged();
                    updateStats();
                    Toast.makeText(TodoListActivity.this, "重复待办已顺延至 " + TimeUtil.formatDateTime(todo.getDeadline()), Toast.LENGTH_SHORT).show();
                });
                return;
            }

            todo.setCompleted(completed);
            todo.setUpdateTime(System.currentTimeMillis());
            noteDatabase.todoDao().updateTodo(todo);
            todoList = noteDatabase.todoDao().getAllTodos();
            runOnUiThread(() -> {
                filterTodos();
                adapter.notifyDataSetChanged();
                updateStats();
            });
        }).start();
    }

    private void deleteSelectedTodos() {
        List<Todo> selectedTodos = adapter.getSelectedTodos();
        if (selectedTodos.isEmpty()) {
            Toast.makeText(this, "请先选择要删除的待办", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("确定删除选中的 " + selectedTodos.size() + " 条待办吗？")
                .setPositiveButton("删除", (dialog, which) -> new Thread(() -> {
                    for (Todo todo : selectedTodos) {
                        noteDatabase.todoDao().deleteTodo(todo);
                    }
                    runOnUiThread(() -> {
                        loadTodos();
                        exitSelectMode();
                        Toast.makeText(TodoListActivity.this, "已删除", Toast.LENGTH_SHORT).show();
                    });
                }).start())
                .setNegativeButton("取消", null)
                .show();
    }

    private void markSelectedAsComplete() {
        List<Todo> selectedTodos = adapter.getSelectedTodos();
        if (selectedTodos.isEmpty()) {
            Toast.makeText(this, "请先选择要标记的待办", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            for (Todo todo : selectedTodos) {
                if (TodoRepeatHelper.isRepeating(todo)) {
                    TodoRepeatHelper.moveToNextOccurrence(todo);
                    NotificationHelper.notifyTodoReminderSet(TodoListActivity.this, todo.getContent(), todo.getDeadline());
                } else {
                    todo.setCompleted(true);
                    todo.setUpdateTime(System.currentTimeMillis());
                }
                noteDatabase.todoDao().updateTodo(todo);
            }
            runOnUiThread(() -> {
                loadTodos();
                exitSelectMode();
                Toast.makeText(TodoListActivity.this, "已处理完成", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodos();
    }
}
