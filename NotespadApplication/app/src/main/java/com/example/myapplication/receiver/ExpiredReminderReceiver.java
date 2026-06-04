package com.example.myapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.myapplication.utils.ExpiredReminderScheduler;
import com.example.myapplication.utils.NotificationHelper;

public class ExpiredReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();
        new Thread(() -> {
            NotificationHelper.notifyExpiredItemsIfNeeded(appContext);
            ExpiredReminderScheduler.scheduleNextCheck(appContext);
        }, "expired-reminder-receiver").start();
    }
}
