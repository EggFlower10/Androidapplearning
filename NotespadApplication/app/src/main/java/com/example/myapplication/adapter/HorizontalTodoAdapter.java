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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HorizontalTodoAdapter extends RecyclerView.Adapter<HorizontalTodoAdapter.TodoViewHolder> {
    private List<Todo> todos = new ArrayList<>();
    private OnTodoClickListener onTodoClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnTodoClickListener {
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
        private TextView tvDeadline;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxTodo);
            tvContent = itemView.findViewById(R.id.tvTodoContent);
            tvTag = itemView.findViewById(R.id.tvTodoTag);
            tvDeadline = itemView.findViewById(R.id.tvTodoDeadline);
        }

        public void bind(Todo todo) {
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(todo.isCompleted());

            tvContent.setText(todo.getContent());
            if (todo.isCompleted()) {
                tvContent.setTextColor(tvContent.getContext().getResources().getColor(R.color.text_strikethrough));
                tvContent.setPaintFlags(tvContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvContent.setTextColor(tvContent.getContext().getResources().getColor(R.color.text_primary));
                tvContent.setPaintFlags(tvContent.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            String tag = todo.getTag();
            if (tag == null || tag.isEmpty()) {
                tag = "默认";
            }
            tvTag.setText(tag);

            if (todo.getDeadline() > 0) {
                String deadline = dateFormat.format(new Date(todo.getDeadline()));
                tvDeadline.setText("截止 " + deadline);
            } else {
                tvDeadline.setText("无截止时间");
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