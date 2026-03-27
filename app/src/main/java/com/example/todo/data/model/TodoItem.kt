package com.example.todo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority { LOW, MEDIUM, HIGH }

@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
