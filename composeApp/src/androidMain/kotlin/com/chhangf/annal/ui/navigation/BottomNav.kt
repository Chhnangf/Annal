package com.chhangf.annal.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.chhangf.annal.SessionViewModel
import com.chhangf.annal.data.viewmodel.todo.ToDoViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BottomNav(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    todoViewModel: ToDoViewModel,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Scaffold(
        bottomBar = if (shouldShowBottomBar(currentRoute)) {
            { BottomBar(navController) }
        } else {
            // 当不需要底部栏时，提供一个空视图或者高度为0的Spacer
            { Spacer(modifier = Modifier.height(0.dp)) }
        },
    ) {
        Column(
            modifier = Modifier
        ) {
            BottomNavGraph(navController, sessionViewModel, todoViewModel)
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Todo,
        BottomBarScreen.Push,
        BottomBarScreen.Settings,
    )

    val navStackBacEnter by navController.currentBackStackEntryAsState()
    val currentDestination = navStackBacEnter?.destination

    Row(
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        screens.forEach { screen ->
            AddBarItems(
                screen,
                currentDestination,
                navController
            )
        }
    }
}

@Composable
fun RowScope.AddBarItems(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
    val background =
        if (selected) Color.Black else Color.Transparent
    val contentColor =
        if (selected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp)
            .height(35.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                }
            )
    ) {
        Row(
            modifier = Modifier
                .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                screen.icon,
                contentDescription = "icon",
                tint = contentColor
            )
            AnimatedVisibility(visible = selected) {

                Text(
                    text = screen.title,
                    color = contentColor
                )
            }
        }
    }
}

// 定义一个函数来判断是否应该显示底部栏
fun shouldShowBottomBar(route: String?) = route != InitScreen.Splash.route && route != InitScreen.Login.route && route != "todos/edit/{todoId}?isNew={isNew}&todoBoxId={todoBoxId}"

@Composable
fun CustomTopBar(navController: NavHostController, sessionViewModel: SessionViewModel) {
    Surface(
        modifier = Modifier
            .height(44.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            IconButton(onClick = { /* 处理左侧导航按钮点击事件 */ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
                Text("Title 1", style = MaterialTheme.typography.bodyLarge)
                Text("Title 2", style = MaterialTheme.typography.bodyLarge)
                Text("Title 3", style = MaterialTheme.typography.bodyLarge)

            IconButton(onClick = { /* 处理搜索按钮点击事件 */ }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }
    }
}