package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.HorizontalTodoAdapter;
import com.example.myapplication.adapter.NoteAdapter;
import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Note;
import com.example.myapplication.entity.Todo;
import com.example.myapplication.network.QuoteService;
import com.example.myapplication.utils.ExpiredReminderScheduler;
import com.example.myapplication.utils.MessageCenterRepository;
import com.example.myapplication.utils.NotificationHelper;
import com.example.myapplication.utils.TimeUtil;
import com.example.myapplication.utils.TodoRepeatHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 2001;

    private RecyclerView rvNotes;
    private RecyclerView rvTodos;
    private NoteAdapter noteAdapter;
    private HorizontalTodoAdapter todoAdapter;
    private List<Note> allNotes = new ArrayList<>();
    private List<Todo> allTodos = new ArrayList<>();
    private NoteDatabase noteDatabase;
    private QuoteService quoteService;

    private TextView tvQuote;
    private TextView tvGreeting;
    private EditText etSearch;
    private ImageView ivMessageCenter;
    private ImageView ivSettings;
    private LinearLayout llQuote;
    private View viewMessageBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteDatabase = NoteDatabase.getInstance(this);
        quoteService = new QuoteService();

        initViews();
        loadNotes();
        loadTodos();
        fetchQuote();
        updateGreeting();
        requestNotificationPermissionIfNeeded();
    }

    private void initViews() {
        rvNotes = findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));

        rvTodos = findViewById(R.id.rv_todos);
        rvTodos.setLayoutManager(new LinearLayoutManager(this));

        tvQuote = findViewById(R.id.tv_quote);
        tvGreeting = findViewById(R.id.tv_greeting);
        etSearch = findViewById(R.id.et_search);
        ivMessageCenter = findViewById(R.id.iv_message_center);
        ivSettings = findViewById(R.id.iv_settings);
        llQuote = findViewById(R.id.ll_quote);
        viewMessageBadge = findViewById(R.id.view_message_badge);

        if (tvQuote != null) {
            tvQuote.setHorizontallyScrolling(false);
            tvQuote.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                    adjustQuoteContainerHeight());
            adjustQuoteContainerHeight();
        }

        if (ivMessageCenter != null) {
            ivMessageCenter.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MessageCenterActivity.class)));
        }

        if (ivSettings != null) {
            ivSettings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        TextView tvSwitchQuote = findViewById(R.id.tv_switch_quote);
        if (tvSwitchQuote != null) {
            tvSwitchQuote.setOnClickListener(v -> fetchQuote());
        }

        Button btnAddNoteMain = findViewById(R.id.btn_add_note_main);
        if (btnAddNoteMain != null) {
            btnAddNoteMain.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddNoteActivity.class)));
        }

        Button btnClearNotes = findViewById(R.id.btn_clear_notes);
        if (btnClearNotes != null) {
            btnClearNotes.setOnClickListener(v -> clearAllNotes());
        }

        Button btnViewAllNotes = findViewById(R.id.btn_view_all_notes);
        if (btnViewAllNotes != null) {
            btnViewAllNotes.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NoteListActivity.class)));
        }

        Button btnClearTodos = findViewById(R.id.btn_clear_todos);
        if (btnClearTodos != null) {
            btnClearTodos.setOnClickListener(v -> clearAllTodos());
        }

        Button btnViewAllTodos = findViewById(R.id.btn_view_all_todos);
        if (btnViewAllTodos != null) {
            btnViewAllTodos.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TodoListActivity.class)));
        }

        Button btnAddTodo = findViewById(R.id.btn_add_todo);
        if (btnAddTodo != null) {
            btnAddTodo.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EditTodoActivity.class)));
        }
    }

    private void updateGreeting() {
        if (tvGreeting == null) {
            return;
        }
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        tvGreeting.setText(hour < 12 ? "早上好" : hour < 18 ? "下午好" : "晚上好");
    }

    private void fetchQuote() {
        if (tvQuote == null) {
            return;
        }
        quoteService.fetchQuote(new QuoteService.QuoteCallback() {
            @Override
            public void onSuccess(String quote, String author) {
                runOnUiThread(() -> updateQuoteText(quote));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> tvQuote.setText("慢慢来，会好的。"));
            }
        });
    }

    private void updateQuoteText(String quote) {
        if (tvQuote == null) {
            return;
        }
        tvQuote.setText(quote);
        adjustQuoteContainerHeight();
    }

    private void adjustQuoteContainerHeight() {
        if (tvQuote == null || llQuote == null) {
            return;
        }

        tvQuote.post(() -> {
            int lineCount = Math.max(tvQuote.getLineCount(), 1);
            int extraLines = Math.max(0, lineCount - 1);
            int baseHeight = dpToPx(72);
            int targetHeight = baseHeight + (extraLines * tvQuote.getLineHeight());

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) llQuote.getLayoutParams();
            if (layoutParams.height != LinearLayout.LayoutParams.WRAP_CONTENT) {
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                llQuote.setLayoutParams(layoutParams);
            }

            if (llQuote.getMinimumHeight() != targetHeight) {
                llQuote.setMinimumHeight(targetHeight);
                llQuote.requestLayout();
            }
        });
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void loadNotes() {
        new Thread(() -> {
            try {
                allNotes = noteDatabase.noteDao().getAllNotes();
            } catch (Exception e) {
                allNotes = new ArrayList<>();
            }
            runOnUiThread(() -> updateNoteRecyclerView(allNotes));
        }).start();
    }

    private void loadTodos() {
        new Thread(() -> {
            try {
                allTodos = noteDatabase.todoDao().getAllTodos();
            } catch (Exception e) {
                allTodos = new ArrayList<>();
            }
            runOnUiThread(() -> updateTodoRecyclerView(allTodos));
        }).start();
    }

    private void updateNoteRecyclerView(List<Note> notes) {
        if (rvNotes == null) {
            return;
        }
        if (notes == null) {
            notes = new ArrayList<>();
        }

        long latestTime = calculateLatestTime(allNotes);
        if (noteAdapter == null) {
            noteAdapter = new NoteAdapter(notes, this::onNoteClick, this::onNoteLongClick);
            noteAdapter.setLatestUpdateTime(latestTime);
            rvNotes.setAdapter(noteAdapter);
        } else {
            noteAdapter.updateNotes(notes);
            noteAdapter.setLatestUpdateTime(latestTime);
        }

        View tvEmpty = findViewById(R.id.tv_empty);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateTodoRecyclerView(List<Todo> todos) {
        if (rvTodos == null) {
            return;
        }
        if (todos == null) {
            todos = new ArrayList<>();
        }

        if (todoAdapter == null) {
            todoAdapter = new HorizontalTodoAdapter();
            todoAdapter.setOnTodoClickListener(new HorizontalTodoAdapter.OnTodoClickListener() {
                @Override
                public void onTodoClick(Todo todo) {
                    Intent intent = new Intent(MainActivity.this, EditTodoActivity.class);
                    intent.putExtra("todo_id", todo.getId());
                    startActivity(intent);
                }

                @Override
                public void onTodoCompleteClick(Todo todo, boolean completed) {
                    toggleTodoComplete(todo, completed);
                }
            });
            rvTodos.setAdapter(todoAdapter);
        }
        todoAdapter.setTodos(todos);
    }

    private void toggleTodoComplete(Todo todo, boolean completed) {
        new Thread(() -> {
            if (completed && TodoRepeatHelper.isRepeating(todo)) {
                TodoRepeatHelper.moveToNextOccurrence(todo);
                todo.setHasSentExpiredReminder(false);
                noteDatabase.todoDao().updateTodo(todo);
                NotificationHelper.notifyTodoReminderSet(MainActivity.this, todo.getId(), todo.getContent(), todo.getDeadline());
                ExpiredReminderScheduler.scheduleNextCheck(MainActivity.this);
                runOnUiThread(() -> {
                    loadTodos();
                    Toast.makeText(MainActivity.this, "重复待办已顺延至 " + TimeUtil.formatDateTime(todo.getDeadline()), Toast.LENGTH_SHORT).show();
                });
                return;
            }

            todo.setCompleted(completed);
            if (!completed) {
                todo.setHasSentExpiredReminder(false);
            }
            noteDatabase.todoDao().updateTodo(todo);
            ExpiredReminderScheduler.scheduleNextCheck(MainActivity.this);
            runOnUiThread(() -> {
                loadTodos();
                Toast.makeText(MainActivity.this, completed ? "已完成" : "已取消完成", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private long calculateLatestTime(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            return Long.MIN_VALUE;
        }
        long latestTime = Long.MIN_VALUE;
        for (Note note : notes) {
            if (note.getUpdateTime() > latestTime) {
                latestTime = note.getUpdateTime();
            }
        }
        return latestTime;
    }

    private void onNoteClick(Note note) {
        Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
        intent.putExtra("note_id", note.getId());
        startActivity(intent);
    }

    private void onNoteLongClick(Note note) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("确定删除这条笔记吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteNote(note))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteNote(Note note) {
        new Thread(() -> {
            noteDatabase.noteDao().deleteNote(note);
            MessageCenterRepository.deleteNoteMessages(this, note.getId());
            ExpiredReminderScheduler.scheduleNextCheck(this);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                loadNotes();
            });
        }).start();
    }

    private void clearAllNotes() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("确定要清空所有笔记吗？此操作不可恢复。")
                .setPositiveButton("确定", (dialog, which) -> new Thread(() -> {
                    noteDatabase.noteDao().deleteAllNotes();
                    MessageCenterRepository.deleteAllNoteMessages(this);
                    ExpiredReminderScheduler.scheduleNextCheck(this);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "已清空", Toast.LENGTH_SHORT).show();
                        loadNotes();
                    });
                }).start())
                .setNegativeButton("取消", null)
                .show();
    }

    private void clearAllTodos() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("确定要清空所有待办吗？此操作不可恢复。")
                .setPositiveButton("确定", (dialog, which) -> new Thread(() -> {
                    noteDatabase.todoDao().deleteAllTodos();
                    ExpiredReminderScheduler.scheduleNextCheck(this);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "已清空", Toast.LENGTH_SHORT).show();
                        loadTodos();
                    });
                }).start())
                .setNegativeButton("取消", null)
                .show();
    }

    private void search(String keyword) {
        if (keyword.isEmpty()) {
            updateNoteRecyclerView(allNotes);
            updateTodoRecyclerView(allTodos);
            return;
        }

        List<Note> filteredNotes = new ArrayList<>();
        for (Note note : allNotes) {
            String title = note.getTitle() != null ? note.getTitle() : "";
            String content = note.getContent() != null ? note.getContent() : "";
            if (title.toLowerCase().contains(keyword.toLowerCase()) || content.toLowerCase().contains(keyword.toLowerCase())) {
                filteredNotes.add(note);
            }
        }

        List<Todo> filteredTodos = new ArrayList<>();
        for (Todo todo : allTodos) {
            String content = todo.getContent() != null ? todo.getContent() : "";
            if (content.toLowerCase().contains(keyword.toLowerCase())) {
                filteredTodos.add(todo);
            }
        }

        updateNoteRecyclerView(filteredNotes);
        updateTodoRecyclerView(filteredTodos);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        loadTodos();
        updateMessageBadge();
        new Thread(() -> ExpiredReminderScheduler.scheduleNextCheck(MainActivity.this)).start();
        checkExpiredNotifications();
    }

    private void updateMessageBadge() {
        if (viewMessageBadge != null) {
            viewMessageBadge.setVisibility(MessageCenterRepository.getUnreadCount(this) > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void checkExpiredNotifications() {
        new Thread(() -> {
            NotificationHelper.notifyExpiredItemsIfNeeded(MainActivity.this);
            ExpiredReminderScheduler.scheduleNextCheck(MainActivity.this);
        }).start();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_CODE_POST_NOTIFICATIONS
        );
    }
}
