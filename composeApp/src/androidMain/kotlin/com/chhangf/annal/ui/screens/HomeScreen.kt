package com.chhangf.annal.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.chhangf.annal.SessionViewModel
import com.chhangf.annal.ui.navigation.BottomTopScreen

@Composable
fun HomeScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val shareScrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(shareScrollState)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /** 布局：
         *  顶部栏：menu菜单、textBottom文本按钮、search搜索按钮
         *  实现：Row、Box * 3
         *
         */
        BottomTop(navController)

    }
}

@Composable
fun BottomTop(navController: NavHostController) {
    val screens = listOf(
        BottomTopScreen.Fellow,
        BottomTopScreen.Nominate,
        BottomTopScreen.Search
    )

    val navStackBacEnter by navController.currentBackStackEntryAsState()
    val currentDestination = navStackBacEnter?.destination

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFFF3F3F3)),
    ) {
        // mean菜单
        Box(
            modifier = Modifier
                .fillMaxWidth(0.25f)
                .padding(4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = BottomTopScreen.Menu.icon,
                    contentDescription = "菜单"
                )
            }

        }

        // textBottom 文本
        Box(
            modifier = Modifier
                .fillMaxWidth(0.67f)
                .padding(2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                screens.forEach { screen ->
                    AddTopItems(
                        screen,
                        currentDestination,
                        navController
                    )
                }
            }

        }

        // search搜索
        Box(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(4.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                Icon(
                    imageVector = BottomTopScreen.Search.icon,
                    contentDescription = "菜单"
                )
            }
        }
    }

}


@Composable
fun RowScope.AddTopItems(
    screen: BottomTopScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
    val background =
        if (selected) Color.Black else Color(0xFFE0E0E0)
    val contentColor =
        if (selected) Color.White else Color.Black

    Box(
        modifier = Modifier
            //.clip(CircleShape)
            .background(background)
            //.border(width = 2.dp, color = Color.White)
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
                .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = screen.title,
                color = contentColor
            )
            AnimatedVisibility(visible = selected) {

            }
        }
    }
}