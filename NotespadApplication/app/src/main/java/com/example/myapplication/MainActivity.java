package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;  // ✅ 正确
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.NoteAdapter;
import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Note;
import com.example.myapplication.network.QuoteService;
import com.example.myapplication.utils.ClipboardUtil;
import com.example.myapplication.utils.TimeUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvNotes;
    private NoteAdapter noteAdapter;
    private List<Note> allNotes = new ArrayList<>();
    private NoteDatabase noteDatabase;
    private QuoteService quoteService;

    private String currentCategory = "all";
    private String currentKeyword = "";
    private boolean sortByPriority = false;

    private TextView tvQuote;
    private TextView tvAuthor;
    private TextView tvExpiredCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteDatabase = NoteDatabase.getInstance(this);
        quoteService = new QuoteService();

        initViews();
        loadNotes();
        fetchQuote();
        checkExpiredNotes();
        checkCacheWarning();
    }

    private void initViews() {
        rvNotes = findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));

        tvQuote = findViewById(R.id.tv_quote);
        tvAuthor = findViewById(R.id.tv_author);
        tvExpiredCount = findViewById(R.id.tv_expired_count);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });

        ImageButton btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        View llQuote = findViewById(R.id.ll_quote);
        llQuote.setOnClickListener(v -> fetchQuote());
        llQuote.setOnLongClickListener(v -> {
            String quoteText = tvQuote.getText().toString() + " —— " + tvAuthor.getText().toString();
            ClipboardUtil.copyToClipboard(MainActivity.this, quoteText);
            Toast.makeText(MainActivity.this, R.string.copied_success, Toast.LENGTH_SHORT).show();
            return true;
        });

        SearchView svSearch = findViewById(R.id.sv_search);
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentKeyword = query;
                filterNotes();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentKeyword = newText;
                filterNotes();
                return false;
            }
        });

        Spinner spCategory = findViewById(R.id.sp_category);
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] categories = getResources().getStringArray(R.array.categories);
                currentCategory = categories[position];
                filterNotes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ImageButton btnExpired = findViewById(R.id.btn_expired);
        btnExpired.setOnClickListener(v -> {
            currentCategory = "expired";
            filterNotes();
        });

        ImageButton btnSort = findViewById(R.id.btn_sort);
        btnSort.setOnClickListener(v -> {
            sortByPriority = !sortByPriority;
            loadNotes();
            Toast.makeText(this, sortByPriority ? R.string.sort_priority : R.string.sort_time, Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchQuote() {
        quoteService.fetchQuote(new QuoteService.QuoteCallback() {
            @Override
            public void onSuccess(String quote, String author) {
                runOnUiThread(() -> {
                    tvQuote.setText(quote);
                    tvAuthor.setText("—— " + author);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    tvQuote.setText(R.string.daily_quote);
                    tvAuthor.setText("");
                });
            }
        });
    }

    private void loadNotes() {
        new Thread(() -> {
            if (sortByPriority) {
                allNotes = noteDatabase.noteDao().getAllNotes();
                allNotes.sort((n1, n2) -> {
                    int p1 = getPriorityOrder(n1.getPriority());
                    int p2 = getPriorityOrder(n2.getPriority());
                    return Integer.compare(p2, p1);
                });
            } else {
                allNotes = noteDatabase.noteDao().getAllNotes();
            }
            runOnUiThread(this::filterNotes);
        }).start();
    }

    private int getPriorityOrder(String priority) {
        switch (priority) {
            case "high":
                return 3;
            case "medium":
                return 2;
            default:
                return 1;
        }
    }

    private void filterNotes() {
        List<Note> filteredNotes = new ArrayList<>();

        for (Note note : allNotes) {
            boolean matchCategory = true;
            boolean matchKeyword = true;
            boolean matchExpired = false;

            if ("expired".equals(currentCategory)) {
                matchExpired = TimeUtil.isExpired(note.getReminderTime());
            } else if (!"all".equals(currentCategory)) {
                matchCategory = currentCategory.equals(note.getCategory());
            }

            if (!currentKeyword.isEmpty()) {
                String keyword = "%" + currentKeyword + "%";
                matchKeyword = (note.getTitle() != null && note.getTitle().toLowerCase().contains(currentKeyword.toLowerCase())) ||
                        (note.getContent() != null && note.getContent().toLowerCase().contains(currentKeyword.toLowerCase()));
            }

            if (matchCategory && matchKeyword && (!"expired".equals(currentCategory) || matchExpired)) {
                filteredNotes.add(note);
            }
        }

        updateRecyclerView(filteredNotes);
    }

    private void updateRecyclerView(List<Note> notes) {
        if (noteAdapter == null) {
            noteAdapter = new NoteAdapter(notes, this::onNoteClick, this::onNoteLongClick);
            rvNotes.setAdapter(noteAdapter);
        } else {
            noteAdapter.updateNotes(notes);
        }

        findViewById(R.id.tv_empty).setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        rvNotes.setVisibility(notes.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void onNoteClick(Note note) {
        Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
        intent.putExtra("note_id", note.getId());
        startActivity(intent);
    }

    private void onNoteLongClick(Note note) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    deleteNote(note);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteNote(Note note) {
        new Thread(() -> {
            noteDatabase.noteDao().deleteNote(note);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, R.string.deleted_success, Toast.LENGTH_SHORT).show();
                loadNotes();
            });
        }).start();
    }

    private void checkExpiredNotes() {
        new Thread(() -> {
            long currentTime = System.currentTimeMillis();
            List<Note> expiredNotes = noteDatabase.noteDao().getExpiredNotes(currentTime);
            runOnUiThread(() -> {
                if (!expiredNotes.isEmpty()) {
                    tvExpiredCount.setText(String.valueOf(expiredNotes.size()));
                    tvExpiredCount.setVisibility(View.VISIBLE);
                } else {
                    tvExpiredCount.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void checkCacheWarning() {
        new Thread(() -> {
            int count = noteDatabase.noteDao().getNoteCount();
            if (count > 20) {
                runOnUiThread(() -> {
                    new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                            .setMessage(R.string.cache_warning)
                            .setPositiveButton(R.string.cache_clear, (dialog, which) -> {
                                clearCache();
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                });
            }
        }).start();
    }

    private void clearCache() {
        new Thread(() -> {
            noteDatabase.noteDao().deleteAllNotes();
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
                loadNotes();
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        checkExpiredNotes();
    }
}