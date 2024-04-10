package com.example.navhost.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class InitScreen (
    val route: String
) {
    object Splash: InitScreen( route = "splash")
    object Login: InitScreen(route = "login")
}

// for mainNav_barBottom
sealed class BottomBarScreen (
    val route: String,
    val title: String,
    val icon: ImageVector,
    val icon_focused: ImageVector, // 点击按钮替换图标的效果，暂未使用
) {
    // for home
    object Home: BottomBarScreen(
        route = "home",
        title = "首页",
        icon = Icons.Default.Home,
        icon_focused = Icons.Default.Home,
    )

    // for settings
    object Settings: BottomBarScreen(
        route = "settings",
        title = "设置",
        icon = Icons.Default.Settings,
        icon_focused = Icons.Default.Settings,
    )

    // for push
    object Push: BottomBarScreen(
        route = "push",
        title = "发布",
        icon = Icons.Default.Add,
        icon_focused = Icons.Default.Add,
    )

    // for todos
    object Todo: BottomBarScreen(
        route = "todo",
        title = "待办",
        icon = Icons.Default.Send,
        icon_focused = Icons.Default.Send,
    )
    object AddTodo: BottomBarScreen(
        route = "add_todo",
        title = "添加待办",
        icon = Icons.Default.Send,
        icon_focused = Icons.Default.Send,
    )
}

// for home_topBottom
sealed class BottomTopScreen (
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    // for menu
    object Menu : BottomTopScreen(
        route = "menu",
        title = "关注",
        icon = Icons.Default.List
    )

    // for fellow
    object Fellow : BottomTopScreen(
        route = "fellow",
        title = "关注",
        icon = Icons.Default.Face
    )

    // for nominate
    object Nominate : BottomTopScreen(
        route = "nominate",
        title = "发现",
        icon = Icons.Default.Face
    )

    // for fellow
    object Today : BottomTopScreen(
        route = "today",
        title = "今日",
        icon = Icons.Default.Face
    )

    // for search
    object Search : BottomTopScreen(
        route = "search",
        title = "搜索",
        icon = Icons.Default.Search
    )
}