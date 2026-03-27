package com.example.todo.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todo.ui.screen.StatsScreen
import com.example.todo.ui.screen.TodoDetailScreen
import com.example.todo.ui.screen.TodoListScreen
import com.example.todo.ui.viewmodel.StatsViewModel
import com.example.todo.ui.viewmodel.TodoViewModel

// 定義底部導覽列的頁籤
private enum class BottomTab(val route: String, val label: String) {
    TODO("list", "待辦"),
    STATS("stats", "統計")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    // TodoViewModel 在 NavGraph 層建立，讓 list 和 detail 共用同一個 instance
    val todoViewModel: TodoViewModel = hiltViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 只有在主頁面顯示底部導覽列，detail 頁不顯示
    val showBottomBar = currentRoute in BottomTab.entries.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    // 切換頁籤時，避免堆疊多個相同頁面
                                    popUpTo("list") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                when (tab) {
                                    BottomTab.TODO -> Icon(
                                        Icons.AutoMirrored.Filled.List,
                                        contentDescription = tab.label
                                    )
                                    BottomTab.STATS -> Icon(
                                        Icons.Default.Star,
                                        contentDescription = tab.label
                                    )
                                }
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // 將底部導覽列高度套用到 NavHost，讓內層每個頁面的 Scaffold
        // 都知道自己的可用空間終點在底部導覽列上方，FAB 才能正確定位
        NavHost(
            navController = navController,
            startDestination = "list",
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable("list") {
                TodoListScreen(
                    viewModel = todoViewModel,
                    onAddClick = { navController.navigate("detail/-1") },
                    onItemClick = { id -> navController.navigate("detail/$id") }
                )
            }
            composable(
                route = "detail/{todoId}",
                arguments = listOf(navArgument("todoId") { type = NavType.IntType })
            ) { backStackEntry ->
                val todoId = backStackEntry.arguments?.getInt("todoId") ?: -1
                TodoDetailScreen(
                    todoId = todoId,
                    viewModel = todoViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("stats") {
                val statsViewModel: StatsViewModel = hiltViewModel()
                StatsScreen(viewModel = statsViewModel)
            }
        }
    }
}
