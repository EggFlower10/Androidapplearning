package com.example.myapplication.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.myapplication.database.NoteDatabase;
import com.example.myapplication.entity.Note;
import com.example.myapplication.entity.Todo;
import com.example.myapplication.receiver.ExpiredReminderReceiver;

public final class ExpiredReminderScheduler {
    private static final int REQUEST_CODE = 3001;

    private ExpiredReminderScheduler() {
    }

    public static void scheduleNextCheck(Context context) {
        Context appContext = context.getApplicationContext();
        NoteDatabase database = NoteDatabase.getInstance(appContext);
        long now = System.currentTimeMillis();

        Note expiredNote = database.noteDao().getFirstExpiredNote(now);
        Todo expiredTodo = database.todoDao().getFirstOverdueTodo(now);

        long triggerAtMillis;
        if (expiredNote != null || expiredTodo != null) {
            triggerAtMillis = now + 1_000L;
        } else {
            Note nextNote = database.noteDao().getNextPendingReminder(now);
            Todo nextTodo = database.todoDao().getNextPendingDeadline(now);
            triggerAtMillis = minPositive(
                    nextNote != null ? nextNote.getReminderTime() : 0L,
                    nextTodo != null ? nextTodo.getDeadline() : 0L
            );
        }

        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = createPendingIntent(appContext);
        alarmManager.cancel(pendingIntent);

        if (triggerAtMillis <= 0L) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    public static void cancel(Context context) {
        Context appContext = context.getApplicationContext();
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        alarmManager.cancel(createPendingIntent(appContext));
    }

    private static PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, ExpiredReminderReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static long minPositive(long first, long second) {
        if (first <= 0L) {
            return second;
        }
        if (second <= 0L) {
            return first;
        }
        return Math.min(first, second);
    }
}
