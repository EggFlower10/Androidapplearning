package com.example.myapplication.adapter;

import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteViewHolder> {

    private Context context;
    private List<Note> noteList;
    private OnNoteClickListener listener;
    private boolean isEditMode = false;
    private List<Long> selectedIds;

    public NoteListAdapter(Context context, List<Note> noteList, OnNoteClickListener listener) {
        this.context = context;
        this.noteList = noteList;
        this.listener = listener;
    }

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    public void setSelectedIds(List<Long> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public void clearSelections() {
        if (selectedIds != null) {
            selectedIds.clear();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note_list, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());
        
        String content = note.getContent();
        if (content.length() > 50) {
            holder.tvContent.setText(content.substring(0, 50) + "...");
        } else {
            holder.tvContent.setText(content);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.tvTime.setText("🍂 " + sdf.format(note.getUpdateTime()));

        if (position == 0) {
            holder.tvLatest.setVisibility(View.VISIBLE);
        } else {
            holder.tvLatest.setVisibility(View.GONE);
        }

        if (isEditMode) {
            holder.cbSelect.setVisibility(View.VISIBLE);
            boolean isSelected = selectedIds != null && selectedIds.contains(note.getId());
            holder.cbSelect.setChecked(isSelected);
        } else {
            holder.cbSelect.setVisibility(View.GONE);
            holder.cbSelect.setChecked(false);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isEditMode) {
                holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
                listener.onNoteLongClick(note);
            } else {
                listener.onNoteClick(note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onNoteLongClick(note);
            return true;
        });

        holder.ivEdit.setOnClickListener(v -> {
            listener.onNoteClick(note);
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener instanceof OnNoteDeleteListener) {
                ((OnNoteDeleteListener) listener).onNoteDelete(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public interface OnNoteDeleteListener {
        void onNoteDelete(Note note);
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        TextView tvLatest;
        ImageView ivEdit;
        ImageView ivDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_select);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvLatest = itemView.findViewById(R.id.tv_latest);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}