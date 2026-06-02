package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.entity.Note;
import com.example.myapplication.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;
    private List<Note> selectedNotes = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private long latestUpdateTime = Long.MIN_VALUE;
    private boolean isSelectMode = false;

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Note note);
    }

    public interface OnSelectChangeListener {
        void onSelectChange(int count);
    }

    private OnSelectChangeListener selectChangeListener;

    public void setOnSelectChangeListener(OnSelectChangeListener listener) {
        this.selectChangeListener = listener;
    }

    public NoteAdapter(List<Note> notes, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.notes = notes;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    public void updateNotes(List<Note> newNotes) {
        this.notes = newNotes;
        selectedNotes.clear();
        notifyDataSetChanged();
    }

    public void setLatestUpdateTime(long latestTime) {
        this.latestUpdateTime = latestTime;
        notifyDataSetChanged();
    }

    public void setSelectMode(boolean selectMode) {
        this.isSelectMode = selectMode;
        if (!selectMode) {
            selectedNotes.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectMode() {
        return isSelectMode;
    }

    public void selectAll() {
        selectedNotes.clear();
        selectedNotes.addAll(notes);
        notifyDataSetChanged();
        if (selectChangeListener != null) {
            selectChangeListener.onSelectChange(selectedNotes.size());
        }
    }

    public void deselectAll() {
        selectedNotes.clear();
        notifyDataSetChanged();
        if (selectChangeListener != null) {
            selectChangeListener.onSelectChange(0);
        }
    }

    public List<Note> getSelectedNotes() {
        return selectedNotes;
    }

    private void toggleSelect(Note note) {
        if (selectedNotes.contains(note)) {
            selectedNotes.remove(note);
        } else {
            selectedNotes.add(note);
        }
        notifyDataSetChanged();
        if (selectChangeListener != null) {
            selectChangeListener.onSelectChange(selectedNotes.size());
        }
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
        holder.tvTime.setText("🍂 " + TimeUtil.formatDateTime(note.getUpdateTime()));

        holder.tvLatest.setVisibility(isLatestNote(note) ? View.VISIBLE : View.GONE);

        holder.cbSelect.setVisibility(isSelectMode ? View.VISIBLE : View.GONE);
        holder.cbSelect.setChecked(selectedNotes.contains(note));

        if (isSelectMode) {
            holder.itemView.setOnClickListener(v -> {
                toggleSelect(note);
            });
        } else {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(note);
                }
            });
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectMode && longClickListener != null) {
                longClickListener.onItemLongClick(note);
            }
            return true;
        });

        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(note);
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(note);
            }
        });

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != selectedNotes.contains(note)) {
                toggleSelect(note);
            }
        });
    }

    private boolean isLatestNote(Note note) {
        if (latestUpdateTime == Long.MIN_VALUE) {
            return false;
        }
        return note.getUpdateTime() == latestUpdateTime;
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        TextView tvLatest;
        ImageView ivEdit;
        ImageView ivDelete;
        CheckBox cbSelect;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvLatest = itemView.findViewById(R.id.tv_latest);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            cbSelect = itemView.findViewById(R.id.cb_select);
        }
    }
}