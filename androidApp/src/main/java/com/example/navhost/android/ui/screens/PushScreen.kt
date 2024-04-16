package com.example.navhost.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.navhost.android.SessionViewModel

@Composable
fun PublishScreen(
    navHostController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 在此处放置HomeScreen的具体内容，而不是简单地显示一个Text
    }
}

