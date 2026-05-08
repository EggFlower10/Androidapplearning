package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.entity.Note;
import com.example.myapplication.utils.TimeUtil;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Note note);
    }

    public NoteAdapter(List<Note> notes, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.notes = notes;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    public void updateNotes(List<Note> newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle() != null ? note.getTitle() : "无标题");
        holder.tvContent.setText(note.getContent() != null ? note.getContent() : "");
        holder.tvCategory.setText(note.getCategory() != null ? note.getCategory() : "未分类");
        holder.tvTime.setText(TimeUtil.formatDateTime(note.getUpdateTime()));

        holder.vPriority.setBackgroundColor(getPriorityColor(note.getPriority()));

        boolean isExpired = TimeUtil.isExpired(note.getReminderTime());
        holder.ivExpired.setVisibility(isExpired ? View.VISIBLE : View.GONE);
        holder.cvNote.setCardBackgroundColor(isExpired ? 
                holder.itemView.getContext().getResources().getColor(R.color.expired_bg) :
                holder.itemView.getContext().getResources().getColor(R.color.background_cream));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(note);
            }
            return true;
        });
    }

    private int getPriorityColor(String priority) {
        switch (priority) {
            case "high":
                return Color.parseColor("#FFE53935");
            case "medium":
                return Color.parseColor("#FFFDD835");
            case "low":
            default:
                return Color.parseColor("#FF9E9E9E");
        }
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        CardView cvNote;
        View vPriority;
        TextView tvTitle;
        TextView tvContent;
        TextView tvCategory;
        TextView tvTime;
        ImageView ivExpired;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cvNote = itemView.findViewById(R.id.cv_note);
            vPriority = itemView.findViewById(R.id.v_priority);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivExpired = itemView.findViewById(R.id.iv_expired);
        }
    }
}