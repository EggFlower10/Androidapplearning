package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.utils.ThemeManager;

public class NotesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySavedTheme(this);
    }
}
