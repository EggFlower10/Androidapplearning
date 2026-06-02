package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {
    public static final String PREFERENCES_NAME = "note_settings";
    public static final String KEY_THEME_MODE = "theme_mode";

    public static final String MODE_SYSTEM = "system";
    public static final String MODE_LIGHT = "light";
    public static final String MODE_DARK = "dark";

    private ThemeManager() {
    }

    public static void applySavedTheme(Context context) {
        applyTheme(getThemeMode(context));
    }

    public static void applyTheme(String themeMode) {
        AppCompatDelegate.setDefaultNightMode(toNightMode(themeMode));
    }

    public static void saveThemeMode(Context context, String themeMode) {
        getPreferences(context).edit().putString(KEY_THEME_MODE, themeMode).apply();
    }

    public static String getThemeMode(Context context) {
        return getPreferences(context).getString(KEY_THEME_MODE, MODE_LIGHT);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private static int toNightMode(String themeMode) {
        if (MODE_DARK.equals(themeMode)) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        if (MODE_SYSTEM.equals(themeMode)) {
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        return AppCompatDelegate.MODE_NIGHT_NO;
    }
}
