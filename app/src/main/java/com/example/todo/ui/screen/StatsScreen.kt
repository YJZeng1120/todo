package com.example.todo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todo.ui.viewmodel.StatsViewModel
import com.example.todo.ui.viewmodel.TodoStats
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    bottomPadding: PaddingValues = PaddingValues()
) {
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("統計") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (stats.total == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = bottomPadding.calculateBottomPadding()),
                contentAlignment = Alignment.Center
            ) {
                Text("還沒有任何待辦，快去新增吧！", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .padding(bottom = 16.dp + bottomPadding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompletionRateCard(stats)
                SummaryRow(stats)
                PriorityBreakdownCard(stats)
                if (stats.overdue > 0) OverdueCard(stats.overdue)
            }
        }
    }
}

@Composable
private fun CompletionRateCard(stats: TodoStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("完成率", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LinearProgressIndicator(
                    progress = { stats.completionRate },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${(stats.completionRate * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "已完成 ${stats.completed} / 共 ${stats.total} 筆",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun SummaryRow(stats: TodoStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(label = "全部", value = stats.total, modifier = Modifier.weight(1f))
        SummaryCard(
            label = "已完成",
            value = stats.completed,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "未完成",
            value = stats.pending,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun PriorityBreakdownCard(stats: TodoStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("優先級分佈", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            PriorityRow(
                label = "高",
                count = stats.highPriority,
                total = stats.total,
                color = MaterialTheme.colorScheme.error
            )
            PriorityRow(
                label = "中",
                count = stats.mediumPriority,
                total = stats.total,
                color = MaterialTheme.colorScheme.tertiary
            )
            PriorityRow(
                label = "低",
                count = stats.lowPriority,
                total = stats.total,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun PriorityRow(
    label: String,
    count: Int,
    total: Int,
    color: androidx.compose.ui.graphics.Color
) {
    val fraction = if (total == 0) 0f else count.toFloat() / total
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(24.dp)
        )
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(24.dp)
        )
    }
}

@Composable
private fun OverdueCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "有 $count 筆待辦已逾期，記得處理！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
