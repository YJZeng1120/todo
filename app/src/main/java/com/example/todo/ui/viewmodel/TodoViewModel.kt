package com.example.todo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.model.Priority
import com.example.todo.data.model.TodoItem
import com.example.todo.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    val todos: StateFlow<List<TodoItem>> = repository.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTodo(title: String, description: String, priority: Priority, dueDate: Long?) {
        viewModelScope.launch {
            repository.insert(
                TodoItem(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate
                )
            )
        }
    }

    fun updateTodo(todo: TodoItem) {
        viewModelScope.launch { repository.update(todo) }
    }

    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch { repository.delete(todo) }
    }

    fun toggleComplete(todo: TodoItem) {
        viewModelScope.launch { repository.update(todo.copy(isCompleted = !todo.isCompleted)) }
    }

    suspend fun getTodoById(id: Int): TodoItem? = repository.getTodoById(id)
}
