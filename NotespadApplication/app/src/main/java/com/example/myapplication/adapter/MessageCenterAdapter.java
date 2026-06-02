package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.entity.MessageItem;

import java.util.ArrayList;
import java.util.List;

public class MessageCenterAdapter extends RecyclerView.Adapter<MessageCenterAdapter.MessageViewHolder> {
    private final List<MessageItem> items = new ArrayList<>();
    private OnMessageClickListener onMessageClickListener;

    public interface OnMessageClickListener {
        void onMessageClick(MessageItem item);
    }

    public void setItems(List<MessageItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.onMessageClickListener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_center, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvSummary.setText(item.getSummary());
        holder.tvTime.setText(item.getTime());
        holder.tvCategory.setText(item.getCategory());
        holder.tvUnread.setVisibility(item.isUnread() ? View.VISIBLE : View.GONE);
        holder.tvHighlight.setVisibility(item.isHighlighted() ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            if (onMessageClickListener != null) {
                onMessageClickListener.onMessageClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvSummary;
        final TextView tvTime;
        final TextView tvCategory;
        final TextView tvUnread;
        final TextView tvHighlight;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_message_title);
            tvSummary = itemView.findViewById(R.id.tv_message_summary);
            tvTime = itemView.findViewById(R.id.tv_message_time);
            tvCategory = itemView.findViewById(R.id.tv_message_category);
            tvUnread = itemView.findViewById(R.id.tv_message_unread);
            tvHighlight = itemView.findViewById(R.id.tv_message_highlight);
        }
    }
}
