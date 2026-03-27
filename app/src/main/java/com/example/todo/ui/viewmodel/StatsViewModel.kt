package com.example.todo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.model.Priority
import com.example.todo.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TodoStats(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val highPriority: Int = 0,
    val mediumPriority: Int = 0,
    val lowPriority: Int = 0,
    val overdue: Int = 0
) {
    val completionRate: Float get() = if (total == 0) 0f else completed.toFloat() / total
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    val stats: StateFlow<TodoStats> = repository.getAllTodos()
        .map { todos ->
            val now = System.currentTimeMillis()
            TodoStats(
                total = todos.size,
                completed = todos.count { it.isCompleted },
                pending = todos.count { !it.isCompleted },
                highPriority = todos.count { it.priority == Priority.HIGH },
                mediumPriority = todos.count { it.priority == Priority.MEDIUM },
                lowPriority = todos.count { it.priority == Priority.LOW },
                overdue = todos.count { it.dueDate != null && it.dueDate < now && !it.isCompleted }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodoStats())
}
