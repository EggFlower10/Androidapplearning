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

import java.util.ArrayList;
import java.util.List;

public class HorizontalTodoAdapter extends RecyclerView.Adapter<HorizontalTodoAdapter.TodoViewHolder> {
    private List<Todo> todos = new ArrayList<>();
    private OnTodoClickListener onTodoClickListener;

    public interface OnTodoClickListener {
        void onTodoClick(Todo todo);
        void onTodoCompleteClick(Todo todo, boolean completed);
    }

    public void setOnTodoClickListener(OnTodoClickListener listener) {
        this.onTodoClickListener = listener;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo_horizontal, parent, false);
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
        private CheckBox checkBox;
        private TextView tvContent;
        private TextView tvTag;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxTodo);
            tvContent = itemView.findViewById(R.id.tvTodoContent);
            tvTag = itemView.findViewById(R.id.tvTodoTag);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onTodoClickListener != null) {
                    onTodoClickListener.onTodoClick(todos.get(position));
                }
            });
        }

        public void bind(Todo todo) {
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(todo.isCompleted());

            tvContent.setText(todo.getContent());
            if (todo.isCompleted()) {
                tvContent.setTextColor(tvContent.getContext().getResources().getColor(R.color.text_strikethrough));
                tvContent.setPaintFlags(tvContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTag.setText("已完成");
                tvTag.setBackgroundResource(R.drawable.tag_grey_bg);
                tvTag.setTextColor(tvTag.getContext().getResources().getColor(R.color.tag_grey));
            } else {
                tvContent.setTextColor(tvContent.getContext().getResources().getColor(R.color.text_primary));
                tvContent.setPaintFlags(tvContent.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                
                String priority = todo.getPriority();
                if ("high".equals(priority)) {
                    tvTag.setText("重要");
                    tvTag.setBackgroundResource(R.drawable.tag_pink_bg);
                    tvTag.setTextColor(tvTag.getContext().getResources().getColor(R.color.tag_pink_text));
                } else {
                    tvTag.setText("进行中");
                    tvTag.setBackgroundResource(R.drawable.tag_grey_bg);
                    tvTag.setTextColor(tvTag.getContext().getResources().getColor(R.color.tag_grey));
                }
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onTodoClickListener != null) {
                    onTodoClickListener.onTodoCompleteClick(todos.get(position), isChecked);
                }
            });
        }
    }
}