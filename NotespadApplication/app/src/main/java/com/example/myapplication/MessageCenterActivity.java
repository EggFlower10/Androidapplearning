package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.MessageCenterAdapter;
import com.example.myapplication.entity.MessageItem;
import com.example.myapplication.utils.MessageCenterRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageCenterActivity extends AppCompatActivity {
    private final List<MessageItem> allMessages = new ArrayList<>();
    private final List<MessageItem> visibleMessages = new ArrayList<>();

    private MessageCenterAdapter adapter;
    private TextView tvFilterAll;
    private TextView tvFilterUnread;
    private TextView tvFilterReminder;
    private TextView tvFilterSystem;
    private TextView tvFilterTodo;
    private TextView tvFilterNote;
    private TextView tvEmpty;
    private TextView tvUnreadSummary;
    private TextView tvUnreadBadge;

    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);

        initViews();
        setupRecyclerView();
        setupListeners();
        refreshMessages();
    }

    private void initViews() {
        tvFilterAll = findViewById(R.id.tv_filter_all);
        tvFilterUnread = findViewById(R.id.tv_filter_unread);
        tvFilterReminder = findViewById(R.id.tv_filter_reminder);
        tvFilterSystem = findViewById(R.id.tv_filter_system);
        tvFilterTodo = findViewById(R.id.tv_filter_todo);
        tvFilterNote = findViewById(R.id.tv_filter_note);
        tvEmpty = findViewById(R.id.tv_empty);
        tvUnreadSummary = findViewById(R.id.tv_unread_summary);
        tvUnreadBadge = findViewById(R.id.tv_unread_badge);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.tv_mark_all_read).setOnClickListener(v -> markAllAsRead());
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);
        adapter = new MessageCenterAdapter();
        adapter.setOnMessageClickListener(this::markMessageAsRead);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        tvFilterAll.setOnClickListener(v -> setFilter("all"));
        tvFilterUnread.setOnClickListener(v -> setFilter("unread"));
        tvFilterReminder.setOnClickListener(v -> setFilter("reminder"));
        tvFilterSystem.setOnClickListener(v -> setFilter("system"));
        tvFilterTodo.setOnClickListener(v -> setFilter("todo"));
        tvFilterNote.setOnClickListener(v -> setFilter("note"));
    }

    private void refreshMessages() {
        allMessages.clear();
        allMessages.addAll(MessageCenterRepository.getMessages(this));
        updateSummary();
        applyFilter();
    }

    private void updateSummary() {
        int unreadCount = MessageCenterRepository.getUnreadCount(this);
        tvUnreadSummary.setText(unreadCount + " 条未读消息");
        tvUnreadBadge.setText(String.format(Locale.CHINA, "%02d", unreadCount));
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateFilterStyles();
        applyFilter();
    }

    private void applyFilter() {
        visibleMessages.clear();
        for (MessageItem item : allMessages) {
            if ("all".equals(currentFilter)
                    || ("unread".equals(currentFilter) && item.isUnread())
                    || ("reminder".equals(currentFilter) && isReminderCategory(item.getCategory()))
                    || ("system".equals(currentFilter) && "系统消息".equals(item.getCategory()))
                    || ("todo".equals(currentFilter) && MessageCenterRepository.RELATED_TYPE_TODO.equals(item.getRelatedType()))
                    || ("note".equals(currentFilter) && MessageCenterRepository.RELATED_TYPE_NOTE.equals(item.getRelatedType()))) {
                visibleMessages.add(item);
            }
        }

        adapter.setItems(visibleMessages);
        tvEmpty.setVisibility(visibleMessages.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean isReminderCategory(String category) {
        return "待办提醒".equals(category) || "过期提醒".equals(category);
    }

    private void updateFilterStyles() {
        bindFilterState(tvFilterAll, "all".equals(currentFilter));
        bindFilterState(tvFilterUnread, "unread".equals(currentFilter));
        bindFilterState(tvFilterReminder, "reminder".equals(currentFilter));
        bindFilterState(tvFilterSystem, "system".equals(currentFilter));
        bindFilterState(tvFilterTodo, "todo".equals(currentFilter));
        bindFilterState(tvFilterNote, "note".equals(currentFilter));
    }

    private void bindFilterState(TextView textView, boolean selected) {
        textView.setBackgroundResource(selected ? R.drawable.message_filter_selected_bg : R.drawable.message_filter_bg);
        textView.setTextColor(getResources().getColor(selected ? R.color.white : R.color.text_secondary));
    }

    private void markAllAsRead() {
        MessageCenterRepository.markAllAsRead(this);
        refreshMessages();
    }

    private void markMessageAsRead(MessageItem item) {
        if (!item.isUnread()) {
            return;
        }
        MessageCenterRepository.markAsRead(this, item.getId());
        refreshMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMessages();
    }
}
