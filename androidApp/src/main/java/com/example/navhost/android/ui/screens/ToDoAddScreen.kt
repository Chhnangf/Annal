package com.example.navhost.android.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.navhost.android.data.model.Priority
import com.example.navhost.android.data.model.ToDoData
import com.example.navhost.android.data.viewmodel.ToDoViewModel
import com.marosseleng.compose.material3.datetimepickers.time.domain.noSeconds
import com.marosseleng.compose.material3.datetimepickers.time.ui.dialog.TimePickerDialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ToDoAddScreen(
    nestedNavController: NavHostController,
    todoViewModel: ToDoViewModel,
    todoId: Long? = null,
    isNew: Boolean = true,
    todoBoxId: Long, // 新增 todoBoxId 参数
) {

    Log.d("AddToDoScreen", "todoId: $todoId, isNew: $isNew, todoBoxId: $todoBoxId")
    // 在AddToDoScreen函数顶部添加toDoData的定义
    var toDoData by remember { mutableStateOf<ToDoData?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.LOW) } // 优先级默认为低

    // 时间选择器
    var isDialogShown: Boolean by rememberSaveable { mutableStateOf(false) }
    var reminderTimeIsSet by remember { mutableStateOf(false) }
    var reminderText by rememberSaveable { mutableStateOf("设置提醒") }
    val (selectedTime, setSelectedTime) = rememberSaveable {
        mutableStateOf(
            LocalTime.now().noSeconds()
        )
    }
    val formatter = DateTimeFormatter.ofPattern("HH:mm") // 创建一个时间格式化器


    /**
     *  使用LaunchedEffect来监听todoId的变化，
     *  当todoId改变时，会在协程中启动一个新的作用域执行内部逻辑。
     */
    LaunchedEffect(key1 = todoId) {
        /**
         *  订阅todoViewModel.getTodoById(todoId)返回的Flow，并通过collect方法收集数据
         *  当查询到待办事项时，会更新title、description和selectedPriority这三个状态变量的值
         */
        todoId?.let {
            todoViewModel.getTodoById(it).collect { todo ->
                Log.d("AddToDoScreen", "Fetched todo: $todo")
                // 新建
                toDoData = todo
                // 编辑，根据isNew查询并填充表单数据
                if (!isNew) {
                    title = todo.title
                    description = todo.description.toString()
                    selectedPriority = todo.priority
                    reminderText = (if (todo.reminderTime != null) {
                        todo.reminderTime!!.format(formatter)
                    } else {
                        "设置提醒"
                    }).toString()
                }

            }
        }
    }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 输入标题
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
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

            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceAround
            ) {
                // 提交按钮
                Button(
                    onClick = {
                        val reminderTimeValue = if (reminderTimeIsSet) {
                            selectedTime
                        } else {
                            toDoData?.reminderTime
                        }
                        val newTodo = if (isNew) {
                            ToDoData(
                                todo_box_id = todoBoxId,
                                title = title,
                                priority = selectedPriority,
                                description = description,
                                reminderTime = reminderTimeValue,
                            )
                        } else {
                            todoId?.let { existingId ->
                                ToDoData(
                                    todo_box_id = todoBoxId,
                                    id = existingId,
                                    title = title,
                                    priority = selectedPriority,
                                    description = description,
                                    reminderTime = reminderTimeValue,
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
                                todoId?.let { ToDoData(it, title, selectedPriority, description) }
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

                // 设置提醒
                TextButton(
                    onClick = { isDialogShown = true },
                    Modifier
                    //.border(1.dp, Color.Red),
                ) {
                    Text(text = reminderText)
                }
            }
        }
    }

    // 提醒组件逻辑

    if (isDialogShown) {
        TimePickerDialog(
            onDismissRequest = { isDialogShown = false },
            initialTime = selectedTime,
            onTimeChange = {
                isDialogShown = false
                reminderTimeIsSet = true
                setSelectedTime(it)
                reminderText =
                    "提醒时间: ${it.format(DateTimeFormatter.ofPattern("HH:mm"))}" // 使用时间格式化器将LocalTime转为字符串
            },
            title = { Text(text = "选择提醒时间") }
        )
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


