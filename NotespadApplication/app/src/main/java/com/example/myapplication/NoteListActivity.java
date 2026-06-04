package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.myapplication.adapter.NoteAdapter;
import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Note;
import com.example.myapplication.utils.ExpiredReminderScheduler;
import com.example.myapplication.utils.MessageCenterRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteListActivity extends AppCompatActivity {

    private RecyclerView rvNotes;
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<>();
    private List<Note> filteredList = new ArrayList<>();
    private NoteDatabase noteDatabase;

    private EditText etSearch;
    private TextView tvEdit;
    private TextView tvSort;
    private TextView tvSave;
    private LinearLayout llSelectBar;
    private CheckBox cbSelectAll;
    private TextView tvDeleteSelected;
    private TextView tvCancelSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        initViews();
        initDatabase();
        loadNotes();
        setupListeners();
        setupDragAndDrop();
    }

    private void initViews() {
        rvNotes = findViewById(R.id.rv_notes);
        etSearch = findViewById(R.id.et_search);
        tvEdit = findViewById(R.id.tv_edit);
        tvSort = findViewById(R.id.tv_sort);
        tvSave = findViewById(R.id.tv_save);
        llSelectBar = findViewById(R.id.ll_select_bar);
        cbSelectAll = findViewById(R.id.cb_select_all);
        tvDeleteSelected = findViewById(R.id.tv_delete_selected);
        tvCancelSelect = findViewById(R.id.tv_cancel_select);

        adapter = new NoteAdapter(filteredList, this::onNoteClick, this::onNoteLongClick);
        adapter.setOnSelectChangeListener(this::onSelectChange);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setAdapter(adapter);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void initDatabase() {
        noteDatabase = NoteDatabase.getInstance(this);
    }

    private void loadNotes() {
        new Thread(() -> {
            noteList = noteDatabase.noteDao().getAllNotes();
            filteredList.clear();
            filteredList.addAll(noteList);
            long latestTime = calculateLatestTime(noteList);
            runOnUiThread(() -> {
                adapter.setLatestUpdateTime(latestTime);
                adapter.notifyDataSetChanged();
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

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvEdit.setOnClickListener(v -> {
            if (adapter.isSelectMode()) {
                exitSelectMode();
            } else {
                enterSelectMode();
            }
        });

        tvSort.setOnClickListener(v -> {
            showSortOptions();
        });

        tvSave.setOnClickListener(v -> {
            saveSortOrder();
        });

        findViewById(R.id.fab_add).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddNoteActivity.class);
            startActivity(intent);
        });

        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                adapter.selectAll();
            } else {
                adapter.deselectAll();
            }
        });

        tvDeleteSelected.setOnClickListener(v -> {
            deleteSelectedNotes();
        });

        tvCancelSelect.setOnClickListener(v -> {
            exitSelectMode();
        });
    }

    private void enterSelectMode() {
        adapter.setSelectMode(true);
        llSelectBar.setVisibility(View.VISIBLE);
        tvEdit.setText("取消");
        tvSave.setVisibility(View.GONE);
        tvSort.setVisibility(View.GONE);
        cbSelectAll.setChecked(false);
    }

    private void exitSelectMode() {
        adapter.setSelectMode(false);
        llSelectBar.setVisibility(View.GONE);
        tvEdit.setText("编辑");
        tvSave.setVisibility(View.VISIBLE);
        tvSort.setVisibility(View.VISIBLE);
        cbSelectAll.setChecked(false);
    }

    private void onSelectChange(int count) {
        cbSelectAll.setChecked(count > 0 && count == filteredList.size());
    }

    private void deleteSelectedNotes() {
        List<Note> selectedNotes = adapter.getSelectedNotes();
        if (selectedNotes.isEmpty()) {
            Toast.makeText(this, "请先选择要删除的笔记", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("确定删除选中的 " + selectedNotes.size() + " 条笔记吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    new Thread(() -> {
                        for (Note note : selectedNotes) {
                            noteDatabase.noteDao().deleteNote(note);
                            MessageCenterRepository.deleteNoteMessages(this, note.getId());
                        }
                        ExpiredReminderScheduler.scheduleNextCheck(NoteListActivity.this);
                        runOnUiThread(() -> {
                            loadNotes();
                            exitSelectMode();
                            Toast.makeText(NoteListActivity.this, "已删除", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void setupDragAndDrop() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                if (adapter.isSelectMode()) {
                    return false;
                }
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();

                if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) {
                    return false;
                }

                Collections.swap(filteredList, fromPos, toPos);
                adapter.notifyItemMoved(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // 不支持滑动删除
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return !adapter.isSelectMode();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvNotes);
    }

    private void saveSortOrder() {
        new Thread(() -> {
            for (int i = 0; i < filteredList.size(); i++) {
                Note note = filteredList.get(i);
                note.setSortOrder(i);
                noteDatabase.noteDao().updateNote(note);
            }

            for (Note note : noteList) {
                note.setSortOrder(filteredList.indexOf(note));
                noteDatabase.noteDao().updateNote(note);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "排序已保存", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void searchNotes(String keyword) {
        filteredList.clear();
        if (keyword.isEmpty()) {
            filteredList.addAll(noteList);
        } else {
            for (Note note : noteList) {
                if (note.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    note.getContent().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredList.add(note);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showSortOptions() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("排序方式");
        String[] options = {"按时间倒序", "按时间正序", "按标题升序", "按标题降序"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    sortByTimeDesc();
                    break;
                case 1:
                    sortByTimeAsc();
                    break;
                case 2:
                    sortByTitleAsc();
                    break;
                case 3:
                    sortByTitleDesc();
                    break;
            }
        });
        builder.show();
    }

    private void sortByTimeDesc() {
        Collections.sort(filteredList, (a, b) -> Long.compare(b.getUpdateTime(), a.getUpdateTime()));
        adapter.notifyDataSetChanged();
    }

    private void sortByTimeAsc() {
        Collections.sort(filteredList, (a, b) -> Long.compare(a.getUpdateTime(), b.getUpdateTime()));
        adapter.notifyDataSetChanged();
    }

    private void sortByTitleAsc() {
        Collections.sort(filteredList, (a, b) -> {
            String titleA = a.getTitle() != null ? a.getTitle() : "";
            String titleB = b.getTitle() != null ? b.getTitle() : "";
            return titleA.compareToIgnoreCase(titleB);
        });
        adapter.notifyDataSetChanged();
    }

    private void sortByTitleDesc() {
        Collections.sort(filteredList, (a, b) -> {
            String titleA = a.getTitle() != null ? a.getTitle() : "";
            String titleB = b.getTitle() != null ? b.getTitle() : "";
            return titleB.compareToIgnoreCase(titleA);
        });
        adapter.notifyDataSetChanged();
    }

    private void onNoteClick(Note note) {
        Intent intent = new Intent(this, AddNoteActivity.class);
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
            ExpiredReminderScheduler.scheduleNextCheck(NoteListActivity.this);
            runOnUiThread(() -> {
                loadNotes();
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }
}
