package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.NoteDatabase;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat swNotifySave;
    private SwitchCompat swNotifyExpired;
    private SwitchCompat swNotifyTodo;
    private Button btnTheme;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("note_settings", Context.MODE_PRIVATE);

        initViews();
        loadSettings();
    }

    private void initViews() {
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        btnTheme = findViewById(R.id.btn_theme);
        btnTheme.setOnClickListener(v -> showThemeSelector());

        swNotifySave = findViewById(R.id.sw_notify_save);
        swNotifyExpired = findViewById(R.id.sw_notify_expired);
        swNotifyTodo = findViewById(R.id.sw_notify_todo);

        swNotifySave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("notify_save", isChecked);
            Toast.makeText(SettingsActivity.this, 
                    isChecked ? "保存成功通知已开启" : "保存成功通知已关闭", 
                    Toast.LENGTH_SHORT).show();
        });

        swNotifyExpired.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("notify_expired", isChecked);
            Toast.makeText(SettingsActivity.this, 
                    isChecked ? "过期提醒通知已开启" : "过期提醒通知已关闭", 
                    Toast.LENGTH_SHORT).show();
        });

        swNotifyTodo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("notify_todo", isChecked);
            Toast.makeText(SettingsActivity.this, 
                    isChecked ? "待办提醒通知已开启" : "待办提醒通知已关闭", 
                    Toast.LENGTH_SHORT).show();
        });

        Button btnClearCache = findViewById(R.id.btn_clear_cache);
        btnClearCache.setOnClickListener(v -> clearCache());
    }

    private void loadSettings() {
        String themeMode = preferences.getString("theme_mode", "system");
        updateThemeButton(themeMode);

        swNotifySave.setChecked(preferences.getBoolean("notify_save", true));
        swNotifyExpired.setChecked(preferences.getBoolean("notify_expired", true));
        swNotifyTodo.setChecked(preferences.getBoolean("notify_todo", true));
    }

    private void saveSetting(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    private void saveSetting(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    private void showThemeSelector() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_theme_selector, null);
        
        Button btnSystem = dialogView.findViewById(R.id.btn_system);
        Button btnLight = dialogView.findViewById(R.id.btn_light);
        Button btnDark = dialogView.findViewById(R.id.btn_dark);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnSystem.setOnClickListener(v -> {
            saveSetting("theme_mode", "system");
            updateThemeButton("system");
            dialog.dismiss();
            Toast.makeText(this, "已设置为跟随系统", Toast.LENGTH_SHORT).show();
        });

        btnLight.setOnClickListener(v -> {
            saveSetting("theme_mode", "light");
            updateThemeButton("light");
            dialog.dismiss();
            Toast.makeText(this, "已设置为浅色模式", Toast.LENGTH_SHORT).show();
        });

        btnDark.setOnClickListener(v -> {
            saveSetting("theme_mode", "dark");
            updateThemeButton("dark");
            dialog.dismiss();
            Toast.makeText(this, "已设置为深色模式", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void updateThemeButton(String mode) {
        String[] modes = {"system", "light", "dark"};
        String[] labels = {"跟随系统", "浅色模式", "深色模式"};
        
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(mode)) {
                btnTheme.setText(labels[i]);
                break;
            }
        }
    }

    private void clearCache() {
        new AlertDialog.Builder(this)
                .setTitle("确认清理")
                .setMessage("确定要清理所有缓存数据吗？此操作不可恢复！")
                .setPositiveButton("确定", (dialog, which) -> {
                    new Thread(() -> {
                        NoteDatabase db = NoteDatabase.getInstance(SettingsActivity.this);
                        db.noteDao().deleteAllNotes();

                        runOnUiThread(() -> {
                            Toast.makeText(SettingsActivity.this, "缓存清理成功", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}