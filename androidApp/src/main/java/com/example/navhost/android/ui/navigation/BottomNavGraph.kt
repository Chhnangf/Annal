package com.example.navhost.android.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.navhost.android.SessionViewModel
import com.example.navhost.android.data.viewmodel.ToDoViewModel
import com.example.navhost.android.ui.screens.HomesScreen
import com.example.navhost.android.ui.screens.LoginsScreen
import com.example.navhost.android.ui.screens.PublishsScreen
import com.example.navhost.android.ui.screens.SettingsScreen
import com.example.navhost.android.ui.screens.SplashsScreen
import com.example.navhost.android.ui.screens.ToDoAddScreen
import com.example.navhost.android.ui.screens.TodosScreen

@Composable
fun BottomNavGraph (
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    todoViewModel: ToDoViewModel
) {
    NavHost(navController = navController,
        startDestination = BottomBarScreen.Todo.route ,
    ) {
        /**
         * for Init
         */
        composable(InitScreen.Splash.route) {
            SplashsScreen(navController,sessionViewModel)
        }
        composable(InitScreen.Login.route) {
            LoginsScreen(navController,sessionViewModel)
        }

        /**
         * for BottomBarScreen
         */
        composable(BottomBarScreen.Home.route) {
            HomesScreen(navController,sessionViewModel)
        }

        /**
         *  for todos
         */
        composable(BottomBarScreen.Todo.route) {
            TodosScreen(navController,todoViewModel)
        }
        composable("todos/edit/{todoId}?isNew={isNew}&todoBoxId={todoBoxId}") { backStackEntry ->
            val todoId = backStackEntry.arguments?.getString("todoId")?.toLongOrNull() ?: return@composable
            // 正确从 arguments 中获取 isNew 参数
            val isNewStr = backStackEntry.arguments?.getString("isNew") // 获取是新建还是编辑
            val isNew = isNewStr?.toBoolean() ?: true // 获取用户新建还是编辑，默认值为true表示新建
            val todoBoxIdFromArgs = backStackEntry.arguments?.getString("todoBoxId")?.toLongOrNull() // 获取boxid，不能为空

            Log.d("NavGraph", "NavGraph_Todo ID: $todoId, NavGraph_isNew: $isNew, todoBoxIdFromArgs: $todoBoxIdFromArgs")
            todoBoxIdFromArgs?.let {
                ToDoAddScreen(
                    nestedNavController = navController,
                    todoViewModel = todoViewModel,
                    todoId = todoId,
                    isNew = isNew,
                    todoBoxId = it
                )
            }
        }

        composable(BottomBarScreen.Push.route) {
            PublishsScreen(navController,sessionViewModel)
        }
        composable(BottomBarScreen.Settings.route) {
            SettingsScreen(navController,todoViewModel)
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