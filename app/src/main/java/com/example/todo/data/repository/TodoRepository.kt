package com.example.todo.data.repository

import com.example.todo.data.dao.TodoDao
import com.example.todo.data.model.TodoItem
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val dao: TodoDao) {

    fun getAllTodos(): Flow<List<TodoItem>> = dao.getAllTodos()

    suspend fun getTodoById(id: Int): TodoItem? = dao.getTodoById(id)

    suspend fun insert(todo: TodoItem) = dao.insert(todo)

    suspend fun update(todo: TodoItem) = dao.update(todo)

    suspend fun delete(todo: TodoItem) = dao.delete(todo)
}
