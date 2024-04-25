package com.chhangf.annal.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chhangf.annal.SessionViewModel
import com.chhangf.annal.data.viewmodel.ToDoViewModel
import com.chhangf.annal.ui.screens.HomeScreen
import com.chhangf.annal.ui.screens.LoginScreen
import com.chhangf.annal.ui.screens.PublishScreen
import com.chhangf.annal.ui.screens.SettingScreen
import com.chhangf.annal.ui.screens.SplashScreen
import com.chhangf.annal.ui.screens.ToDoAddScreen
import com.chhangf.annal.ui.screens.TodosScreen
import java.time.LocalDate

@Composable
fun BottomNavGraph (
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    todoViewModel: ToDoViewModel
) {
    NavHost(navController = navController,
        startDestination = BottomBarScreen.Todo.route ,
    ) {
        val LocalToDoViewModel = compositionLocalOf<ToDoViewModel> { error("No ToDoViewModel provided!") }
        /**
         * for Init
         */
        composable(InitScreen.Splash.route) {
            SplashScreen(navController,sessionViewModel)
        }
        composable(InitScreen.Login.route) {
            LoginScreen(navController,sessionViewModel)
        }

        /**
         * for BottomBarScreen
         */
        composable(BottomBarScreen.Home.route) {
            HomeScreen(navController,sessionViewModel)
        }

        /**
         *  for todos
         */
        composable(BottomBarScreen.Todo.route) {
            TodosScreen(navController,todoViewModel)
        }
        composable("todos/edit/{todoId}?isNew={isNew}&todoBoxId={todoBoxId}&selectDateAt={selectDateAt}") { backStackEntry ->
            val todoId = backStackEntry.arguments?.getString("todoId")?.toLongOrNull() ?: return@composable
            // 正确从 arguments 中获取 isNew 参数
            val isNewStr = backStackEntry.arguments?.getString("isNew") // 获取是新建还是编辑
            val isNew = isNewStr?.toBoolean() ?: true // 获取用户新建还是编辑，默认值为true表示新建
            val todoBoxIdFromArgs = backStackEntry.arguments?.getString("todoBoxId")?.toLongOrNull() // 获取boxid，不能为空
            val selectDateAtStr = backStackEntry.arguments?.getString("selectDateAt")
            val selectedDate: LocalDate? = selectDateAtStr?.let {
                // 假设您的字符串是按照ISO 8601格式编码的日期
                LocalDate.parse(it)
            }

            Log.d("NavGraph", "NavGraph_Todo ID: $todoId, NavGraph_isNew: $isNew, todoBoxIdFromArgs: $todoBoxIdFromArgs")
            todoBoxIdFromArgs?.let {
                selectedDate?.let { it1 ->
                    ToDoAddScreen(
                        nestedNavController = navController,
                        todoViewModel = todoViewModel,
                        todoId = todoId,
                        isNew = isNew,
                        todoBoxId = it,
                        selectedDate = it1
                    )
                }
            }
        }

        composable(BottomBarScreen.Push.route) {
            PublishScreen(navController,sessionViewModel)
        }
        composable(BottomBarScreen.Settings.route) {
            SettingScreen(navController,todoViewModel)
        }

        /**
         * for BottomTopScreen
         */
        composable(BottomTopScreen.Menu.route) {
            //MeunScreen(navController,sessionViewModel)
        }
        composable(BottomTopScreen.Fellow.route) {
            //FellowScreen(navController,sessionViewModel)
        }
        composable(BottomTopScreen.Nominate.route) {
            //NominateScreen(navController,sessionViewModel)
        }
        composable(BottomTopScreen.Today.route) {
            //TodayScreen(navController,sessionViewModel)
        }
        composable(BottomTopScreen.Search.route) {
            //SearchScreen(navController,sessionViewModel)
        }

    }
}