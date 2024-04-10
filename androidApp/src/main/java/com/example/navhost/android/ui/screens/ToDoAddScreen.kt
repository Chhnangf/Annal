package com.example.navhost.android.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.navhost.android.data.model.Priority
import com.example.navhost.android.data.model.ToDoData
import com.example.navhost.android.data.viewmodel.ToDoViewModel
import java.time.LocalDate

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ToDoAddScreen(
    nestedNavController: NavHostController,
    todoViewModel: ToDoViewModel,
    todoId: Long? = null,
    isNew: Boolean = true,
    todoBoxId: Long, // 新增 todoBoxId 参数
    currentDate: LocalDate = LocalDate.now(), // 当前日期，用于初始化日期选择器
) {

    Log.d("AddToDoScreen", "todoId: $todoId, isNew: $isNew, todoBoxId: $todoBoxId")
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.LOW) } // 优先级默认为低

    // // 编辑模式，根据isNew查询并填充表单数据
    if (!isNew && todoId != null) {
        /**
         *  使用LaunchedEffect来监听todoId的变化，
         *  当todoId改变时，会在协程中启动一个新的作用域执行内部逻辑。
         */
        LaunchedEffect(key1 = todoId) {
                /**
                 *  订阅todoViewModel.getTodoById(todoId)返回的Flow，并通过collect方法收集数据
                 *  当查询到待办事项时，会更新title、description和selectedPriority这三个状态变量的值
                 */
                todoViewModel.getTodoById(todoId).collect { todo ->
                    Log.d("AddToDoScreen", "Fetched todo: $todo")
                    title = todo.title
                    description = todo.description
                    selectedPriority = todo.priority
                }
        }
    }


    Box(modifier = Modifier.fillMaxSize(),contentAlignment = Alignment.Center) {
        Column (
            modifier = Modifier
                .fillMaxSize()
        ){
            // 输入标题
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 输入内容
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 优先级选择器 RadioGroup or DropdownMenu
            PrioritySelector(
                onPrioritySelected = { selectedPriority = it }
            )

            // 提交按钮
            Button(
                onClick = {
                    val newTodo = if (isNew) {
                        ToDoData(
                            todo_box_id = todoBoxId,
                            title = title,
                            priority = selectedPriority,
                            description = description,
                        )
                    } else {
                        todoId?.let { existingId ->
                            ToDoData(
                                todo_box_id = todoBoxId,
                                id = existingId,
                                title = title,
                                priority = selectedPriority,
                                description = description,
                            )
                        }
                    }

                    newTodo?.let { todoViewModel.insertOrUpdateData(it) } // 在 ViewModel 中提供一个可以同时处理插入和更新的方法
                    nestedNavController.popBackStack()
                },
                modifier = Modifier
            ) {
                Text(text = "Submit")
            }
            // 删除按钮
            Button(
                onClick = {
                    if (!title.isNullOrEmpty()) {
                        val deleteTodo =
                            todoId?.let { ToDoData(it,title, selectedPriority, description) }
                        deleteTodo?.let { todoViewModel.deleteItem(it) }
                        // 返回到上一个目的地，即TodoScreen
                        nestedNavController.popBackStack()
                    } else {
                        // 显示错误提示，输入不能为空
                        // ...
                    }
                },
                modifier = Modifier
            ) {
                Text(text = "Delete")
            }
        }
    }
}

/**
 *  优先级下拉列表 由Box垂直布局，DropdownMenu组件实现
 */
@Composable
fun PrioritySelector(
    onPrioritySelected: (Priority) -> Unit,
    initiallySelected: Priority = Priority.LOW
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(initiallySelected) }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val label = selectedPriority.name
        Button(onClick = { expanded = true }) {
            Text(text = label)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }, // 这里修正了 expanded 的引用
            modifier = Modifier.width(70.dp)
        ) {
            Priority.entries.forEach { priority ->
                DropdownMenuItem(
                    onClick = {
                        selectedPriority = priority
                        onPrioritySelected(priority)
                        expanded = false
                    },
                    text = { Text(priority.name) }
                )
            }
        }
    }
}


