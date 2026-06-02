package com.example.myapplication.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.entity.Todo;

import java.util.List;

@Dao
public interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY deadline ASC, createTime DESC")
    List<Todo> getAllTodos();

    @Query("SELECT * FROM todos WHERE completed = 0 ORDER BY deadline ASC, createTime DESC")
    List<Todo> getUncompletedTodos();

    @Query("SELECT * FROM todos WHERE completed = 1 ORDER BY updateTime DESC")
    List<Todo> getCompletedTodos();

    @Query("SELECT * FROM todos WHERE deadline > 0 AND deadline < :currentTime AND completed = 0 ORDER BY deadline ASC")
    List<Todo> getOverdueTodos(long currentTime);

    @Insert
    long insertTodo(Todo todo);

    @Update
    int updateTodo(Todo todo);

    @Delete
    int deleteTodo(Todo todo);

    @Query("DELETE FROM todos WHERE id = :id")
    int deleteTodoById(long id);

    @Query("DELETE FROM todos")
    void deleteAllTodos();

    @Query("SELECT * FROM todos WHERE id = :id")
    Todo getTodoById(long id);
}