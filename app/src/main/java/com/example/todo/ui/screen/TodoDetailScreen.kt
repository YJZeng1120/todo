package com.example.todo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todo.data.model.Priority
import com.example.todo.data.model.TodoItem
import com.example.todo.ui.viewmodel.TodoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    todoId: Int,
    viewModel: TodoViewModel,
    onBack: () -> Unit
) {
    val isEditMode = todoId != -1
    val scope = rememberCoroutineScope()

    var existingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var titleError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(todoId) {
        if (isEditMode) {
            viewModel.getTodoById(todoId)?.let { todo ->
                existingTodo = todo
                title = todo.title
                description = todo.description
                priority = todo.priority
                dueDate = todo.dueDate
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "編輯待辦" else "新增待辦") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("標題 *") },
                isError = titleError,
                supportingText = { if (titleError) Text("標題不能為空") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述（選填）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Text("優先級", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Priority.entries.forEachIndexed { index, p ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index, Priority.entries.size),
                        onClick = { priority = p },
                        selected = priority == p,
                        label = {
                            Text(
                                when (p) {
                                    Priority.LOW -> "低"
                                    Priority.MEDIUM -> "中"
                                    Priority.HIGH -> "高"
                                }
                            )
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        dueDate?.let {
                            SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(it))
                        } ?: "選擇到期日"
                    )
                }
                if (dueDate != null) {
                    TextButton(onClick = { dueDate = null }) {
                        Text("清除")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    scope.launch {
                        if (isEditMode && existingTodo != null) {
                            viewModel.updateTodo(
                                existingTodo!!.copy(
                                    title = title.trim(),
                                    description = description.trim(),
                                    priority = priority,
                                    dueDate = dueDate
                                )
                            )
                        } else {
                            viewModel.addTodo(
                                title = title.trim(),
                                description = description.trim(),
                                priority = priority,
                                dueDate = dueDate
                            )
                        }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("儲存")
            }

            if (isEditMode) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("刪除此待辦")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("確定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("刪除待辦") },
            text = { Text("確定要刪除「${existingTodo?.title}」嗎？") },
            confirmButton = {
                TextButton(onClick = {
                    existingTodo?.let { viewModel.deleteTodo(it) }
                    showDeleteDialog = false
                    onBack()
                }) { Text("刪除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}
