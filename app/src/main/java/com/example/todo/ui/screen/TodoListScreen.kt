package com.example.todo.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.todo.data.model.Priority
import com.example.todo.data.model.TodoItem
import com.example.todo.ui.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoViewModel,
    onAddClick: () -> Unit,
    onItemClick: (Int) -> Unit,
    bottomPadding: PaddingValues = PaddingValues()
) {
    val todos by viewModel.todos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My TODO") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增待辦")
            }
        }
    ) { paddingValues ->
        if (todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = bottomPadding.calculateBottomPadding()),
                contentAlignment = Alignment.Center
            ) {
                Text("還沒有待辦事項，點擊右下角新增！", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 8.dp + bottomPadding.calculateBottomPadding()
                )
            ) {
                items(todos, key = { it.id }) { todo ->
                    TodoListItem(
                        todo = todo,
                        onToggle = { viewModel.toggleComplete(todo) },
                        onDelete = { viewModel.deleteTodo(todo) },
                        onClick = { onItemClick(todo.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoListItem(
    todo: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
            }
            false // 永遠回傳 false，讓 card 自動彈回，由 dialog 決定是否刪除
        }
    )

    LaunchedEffect(showDeleteDialog) {
        if (showDeleteDialog) dismissState.reset()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("刪除待辦") },
            text = { Text("確定要刪除「${todo.title}」嗎？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("刪除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "刪除",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = todo.isCompleted, onCheckedChange = { onToggle() })
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (todo.isCompleted) MaterialTheme.colorScheme.outline
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (todo.description.isNotBlank()) {
                        Text(
                            text = todo.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        todo.dueDate?.let { epoch ->
                            val formatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                                .format(Date(epoch))
                            Text(
                                text = formatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        PriorityChip(priority = todo.priority)
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(priority: Priority) {
    val (label, color) = when (priority) {
        Priority.HIGH -> "高" to MaterialTheme.colorScheme.errorContainer
        Priority.MEDIUM -> "中" to MaterialTheme.colorScheme.tertiaryContainer
        Priority.LOW -> "低" to MaterialTheme.colorScheme.secondaryContainer
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
