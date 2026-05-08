package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.NoteDatabase;

public class SettingsActivity extends AppCompatActivity {
    private NoteDatabase noteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        noteDatabase = NoteDatabase.getInstance(this);

        initViews();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        Button btnClearCache = findViewById(R.id.btn_clear_cache);
        btnClearCache.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setMessage("确定清空所有笔记吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        clearCache();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        Button btnAbout = findViewById(R.id.btn_about);
        btnAbout.setOnClickListener(v -> {
            Toast.makeText(this, "备忘录 v1.0.0\n简约清新的笔记管理应用", Toast.LENGTH_SHORT).show();
        });
    }

    private void clearCache() {
        new Thread(() -> {
            noteDatabase.noteDao().deleteAllNotes();
            runOnUiThread(() -> {
                Toast.makeText(SettingsActivity.this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}