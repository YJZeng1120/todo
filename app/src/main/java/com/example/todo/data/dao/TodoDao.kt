package com.example.todo.data.dao

import androidx.room.*
import com.example.todo.data.model.TodoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, priority DESC, dueDate ASC")
    fun getAllTodos(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Int): TodoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoItem)

    @Update
    suspend fun update(todo: TodoItem)

    @Delete
    suspend fun delete(todo: TodoItem)
}
