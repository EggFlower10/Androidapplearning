package com.example.myapplication.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.entity.Todo;
import com.example.myapplication.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.TodoViewHolder> {
    private List<Todo> todos = new ArrayList<>();
    private List<Todo> selectedTodos = new ArrayList<>();
    private boolean isSelectMode = false;
    private OnTodoClickListener listener;
    private OnSelectChangeListener selectChangeListener;

    public interface OnTodoClickListener {
        void onTodoClick(Todo todo);
        void onTodoCompleteClick(Todo todo, boolean completed);
    }

    public interface OnSelectChangeListener {
        void onSelectChange(int count);
    }

    public void setOnSelectChangeListener(OnSelectChangeListener listener) {
        this.selectChangeListener = listener;
    }

    public TodoListAdapter(List<Todo> todos, OnTodoClickListener listener) {
        this.todos = todos;
        this.listener = listener;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
        selectedTodos.clear();
        notifyDataSetChanged();
    }

    public void setSelectMode(boolean selectMode) {
        this.isSelectMode = selectMode;
        if (!selectMode) {
            selectedTodos.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectMode() {
        return isSelectMode;
    }

    public void selectAll() {
        selectedTodos.clear();
        selectedTodos.addAll(todos);
        notifyDataSetChanged();
        if (selectChangeListener != null) {
            selectChangeListener.onSelectChange(selectedTodos.size());
        }
    }

    public void deselectAll() {
        selectedTodos.clear();
        notifyDataSetChanged();
        if (selectChangeListener != null) {
            selectChangeListener.onSelectChange(0);
        }
    }

    public List<Todo> getSelectedTodos() {
        return selectedTodos;
    }

    private void toggleSelect(Todo todo) {
        if (selectedTodos.contains(todo)) {
            selectedTodos.remove(todo);
        } else {
            selectedTodos.add(todo);
        }
        notifyDataSetChanged();
        if (selectChangeListener != null) {
            selectChangeListener.onSelectChange(selectedTodos.size());
        }
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo_list, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todos.get(position);
        holder.bind(todo);
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbSelect;
        private CheckBox cbComplete;
        private TextView tvContent;
        private TextView tvDeadline;
        private TextView tvTag;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_select);
            cbComplete = itemView.findViewById(R.id.cb_complete);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvDeadline = itemView.findViewById(R.id.tv_deadline);
            tvTag = itemView.findViewById(R.id.tv_tag);
        }

        public void bind(Todo todo) {
            cbSelect.setVisibility(isSelectMode ? View.VISIBLE : View.GONE);
            cbSelect.setChecked(selectedTodos.contains(todo));

            cbComplete.setOnCheckedChangeListener(null);
            cbComplete.setChecked(todo.isCompleted());

            tvContent.setText(todo.getContent());
            if (todo.isCompleted()) {
                tvContent.setTextColor(tvContent.getContext().getResources().getColor(R.color.text_strikethrough));
                tvContent.setPaintFlags(tvContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvDeadline.setTextColor(tvDeadline.getContext().getResources().getColor(R.color.text_strikethrough));
            } else {
                tvContent.setTextColor(tvContent.getContext().getResources().getColor(R.color.text_primary));
                tvContent.setPaintFlags(tvContent.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvDeadline.setTextColor(tvDeadline.getContext().getResources().getColor(R.color.text_hint));
            }

            if (todo.getDeadline() > 0) {
                tvDeadline.setText("截止: " + TimeUtil.formatDate(todo.getDeadline()));
            } else {
                tvDeadline.setText("");
            }

            String priority = todo.getPriority();
            if (todo.isCompleted()) {
                tvTag.setText("已完成");
                tvTag.setBackgroundResource(R.drawable.tag_grey_bg);
                tvTag.setTextColor(tvTag.getContext().getResources().getColor(R.color.tag_grey));
            } else if ("high".equals(priority)) {
                tvTag.setText("重要");
                tvTag.setBackgroundResource(R.drawable.tag_pink_bg);
                tvTag.setTextColor(tvTag.getContext().getResources().getColor(R.color.tag_pink_text));
            } else {
                tvTag.setText("进行中");
                tvTag.setBackgroundResource(R.drawable.tag_grey_bg);
                tvTag.setTextColor(tvTag.getContext().getResources().getColor(R.color.tag_grey));
            }

            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked != selectedTodos.contains(todo)) {
                    toggleSelect(todo);
                }
            });

            cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTodoCompleteClick(todo, isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (isSelectMode) {
                    toggleSelect(todo);
                } else if (listener != null) {
                    listener.onTodoClick(todo);
                }
            });
        }
    }
}