package com.example.myapplication.utils;

import com.example.myapplication.entity.Todo;

import java.util.Calendar;

public final class TodoRepeatHelper {
    private TodoRepeatHelper() {
    }

    public static boolean isRepeating(Todo todo) {
        return todo != null
                && todo.getRepeatType() != null
                && !"none".equals(todo.getRepeatType());
    }

    public static void moveToNextOccurrence(Todo todo) {
        if (!isRepeating(todo)) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        long baseTime = todo.getDeadline() > 0 ? todo.getDeadline() : System.currentTimeMillis();
        calendar.setTimeInMillis(baseTime);

        switch (todo.getRepeatType()) {
            case "daily":
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case "weekly":
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "monthly":
                calendar.add(Calendar.MONTH, 1);
                break;
            default:
                return;
        }

        todo.setDeadline(calendar.getTimeInMillis());
        todo.setCompleted(false);
        todo.setUpdateTime(System.currentTimeMillis());
    }
}
