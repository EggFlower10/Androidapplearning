package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.myapplication.utils.ThemeManager;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat swNotifySave;
    private SwitchCompat swNotifyExpired;
    private SwitchCompat swNotifyTodo;
    private TextView tvThemeValue;
    private SharedPreferences preferences;
    private boolean isLoadingSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(ThemeManager.PREFERENCES_NAME, Context.MODE_PRIVATE);

        initViews();
        loadSettings();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        ImageView ivThemeDropdown = findViewById(R.id.iv_theme_dropdown);
        ivThemeDropdown.setOnClickListener(v -> showThemeSelector());

        ImageView ivAboutArrow = findViewById(R.id.iv_about_arrow);
        ivAboutArrow.setOnClickListener(v -> showAboutDialog());

        tvThemeValue = findViewById(R.id.tv_theme_value);

        swNotifySave = findViewById(R.id.sw_notify_save);
        swNotifyExpired = findViewById(R.id.sw_notify_expired);
        swNotifyTodo = findViewById(R.id.sw_notify_todo);

        swNotifySave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isLoadingSettings) {
                return;
            }
            saveSetting("notify_save", isChecked);
            Toast.makeText(
                    SettingsActivity.this,
                    isChecked ? "保存成功通知已开启" : "保存成功通知已关闭",
                    Toast.LENGTH_SHORT
            ).show();
        });

        swNotifyExpired.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isLoadingSettings) {
                return;
            }
            saveSetting("notify_expired", isChecked);
            Toast.makeText(
                    SettingsActivity.this,
                    isChecked ? "过期提醒通知已开启" : "过期提醒通知已关闭",
                    Toast.LENGTH_SHORT
            ).show();
        });

        swNotifyTodo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isLoadingSettings) {
                return;
            }
            saveSetting("notify_todo", isChecked);
            Toast.makeText(
                    SettingsActivity.this,
                    isChecked ? "待办提醒通知已开启" : "待办提醒通知已关闭",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void loadSettings() {
        isLoadingSettings = true;
        String themeMode = ThemeManager.getThemeMode(this);
        updateThemeValue(themeMode);

        swNotifySave.setChecked(preferences.getBoolean("notify_save", false));
        swNotifyExpired.setChecked(preferences.getBoolean("notify_expired", true));
        swNotifyTodo.setChecked(preferences.getBoolean("notify_todo", true));
        isLoadingSettings = false;
    }

    private void saveSetting(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
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
            applyThemeSelection(ThemeManager.MODE_SYSTEM);
            dialog.dismiss();
            Toast.makeText(this, "已设置为跟随系统", Toast.LENGTH_SHORT).show();
        });

        btnLight.setOnClickListener(v -> {
            applyThemeSelection(ThemeManager.MODE_LIGHT);
            dialog.dismiss();
            Toast.makeText(this, "已设置为浅色模式", Toast.LENGTH_SHORT).show();
        });

        btnDark.setOnClickListener(v -> {
            applyThemeSelection(ThemeManager.MODE_DARK);
            dialog.dismiss();
            Toast.makeText(this, "已设置为深色模式", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void applyThemeSelection(String mode) {
        ThemeManager.saveThemeMode(this, mode);
        updateThemeValue(mode);
        ThemeManager.applyTheme(mode);
    }

    private void updateThemeValue(String mode) {
        String[] modes = {ThemeManager.MODE_SYSTEM, ThemeManager.MODE_LIGHT, ThemeManager.MODE_DARK};
        String[] labels = {"跟随系统", "浅色模式", "深色模式"};

        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(mode)) {
                if (tvThemeValue != null) {
                    tvThemeValue.setText(labels[i]);
                }
                break;
            }
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("关于")
                .setMessage("记事本 v1.0.0\n\n一款简洁优雅的笔记和待办管理应用。")
                .setPositiveButton("确定", null)
                .show();
    }
}
