package com.chhangf.annal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.chhangf.annal.SessionViewModel
import com.chhangf.annal.ui.navigation.BottomBarScreen
import com.chhangf.annal.ui.navigation.InitScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    navHostController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }


    LaunchedEffect(sessionViewModel.loginResult) {
        sessionViewModel.loginResult.collectLatest { loginResult ->
            if (loginResult == true) {
                navHostController.navigate(BottomBarScreen.Home.route) {
                    popUpTo(InitScreen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }

            } else if (loginResult == false) {
                // 显示错误提示或其他操作
            }
        }
    }



    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                sessionViewModel.login(username.text, password.text)
            }) {
                Text(text = "登录")
            }


        }
    }
}