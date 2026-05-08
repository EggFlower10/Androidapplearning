package com.example.myapplication.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

public class NotificationManager {
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "note_reminder_channel";

    private final android.app.NotificationManager notificationManager;
    private final Context context;

    public NotificationManager(Context context) {
        this.context = context;
        this.notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    CHANNEL_ID,
                    "笔记提醒",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("备忘录提醒通知");
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String title, String content) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.primary_green));

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void showReminderNotification(String noteTitle) {
        showNotification("笔记提醒", "您有一条笔记即将到期：" + noteTitle);
    }

    public void cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
}