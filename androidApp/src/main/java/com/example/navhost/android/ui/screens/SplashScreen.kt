package com.example.navhost.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.navhost.android.SessionViewModel
import com.example.navhost.android.ui.navigation.BottomBarScreen
import com.example.navhost.android.ui.navigation.InitScreen
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(
    navHostController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(Unit) {
        delay(2000) // 延迟2秒

        if (isLoggedIn) {
            navHostController.navigate(BottomBarScreen.Home.route) {
                popUpTo(InitScreen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navHostController.navigate(InitScreen.Login.route) {
            //navHostController.navigate(BottomBarScreen.Home.route) {
                popUpTo(InitScreen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // 设置白色背景
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 假设您有一个名为 editor_icon 的图标资源
        Text(text = "SplashScreen")
    }
}


