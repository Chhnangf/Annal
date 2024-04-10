package com.example.navhost.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.navhost.android.SessionViewModel

@Composable
fun FellowScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
    ) {
         Text(text = "FellowScreen")
    }
}