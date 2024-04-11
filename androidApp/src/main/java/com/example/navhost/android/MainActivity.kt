package com.example.navhost.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.example.navhost.android.data.viewmodel.ToDoViewModel
import com.example.navhost.android.ui.navigation.BottomNav
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SessionViewModel : ViewModel() {
    // 使用MutableStateFlow来存储登录状态
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    // 假设有一个登录方法，并在登录成功后更新登录状态
    private val _loginResult = MutableStateFlow<Boolean?>(null)
    val loginResult: StateFlow<Boolean?> get() = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val isSuccessfulLogin = authenticateUser(username, password)
            if (isSuccessfulLogin) {
                _isLoggedIn.value = true
            }
            _loginResult.value = isSuccessfulLogin
        }
    }

    // 可以有登出方法
    fun logout() {
        viewModelScope.launch {
            _isLoggedIn.value = false
            // 清除用户的登录信息等...
        }
    }

    // 模拟认证逻辑
    private suspend fun authenticateUser(username: String, password: String): Boolean {
        delay(1000) // 模拟网络延迟
        // return username == "example" && password == "password"
        return username == "" && password == ""
    }
}


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     *  实例化视图模型
     */
    private val sessionViewModel: SessionViewModel by viewModels()
    private val todoViewModel: ToDoViewModel by viewModels()
        {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ToDoViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ToDoViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

    /**
     *  viewModels接口内部实现
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                Surface {
                    BottomNav(navController, sessionViewModel, todoViewModel)
                }

            }

        }

    }
}
