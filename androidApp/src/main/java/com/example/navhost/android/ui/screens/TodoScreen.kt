package com.example.navhost.android.ui.screens


//import dev.jeziellago.compose.markdowntext.MarkdownText

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.navhost.android.data.model.Priority
import com.example.navhost.android.data.model.ToDoBox
import com.example.navhost.android.data.model.ToDoData
import com.example.navhost.android.data.viewmodel.ToDoViewModel
import com.example.navhost.android.markdown.MarkdownText
import kotlinx.coroutines.flow.collectLatest

/** 技术栈
 * Kotlin
 * ViewModel + LiveData + Room + AlarmManager + WorkerManager
 * navigation + DiaLog + 前台通知
 */


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TodosScreen(
    navHostController: NavHostController,
    todoViewModel: ToDoViewModel,
) {

    // 订阅收纳盒及其内容数据流 && 搜索栏相关
    var searchQuery by remember { mutableStateOf("") }
    val filteredBoxesWithTodos by todoViewModel.filteredBoxesWithTodos.collectAsState(initial = emptyList())


    LaunchedEffect(key1 = todoViewModel) {
        todoViewModel.searchQuery.collectLatest { query ->
            searchQuery = query
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    // 订阅收纳盒及其内容数据流
    val boxesWithTodosState by todoViewModel.todoBoxesWithTodos.collectAsState(emptyList())

    // 使用Scaffold创建包含顶部栏和底部栏的布局
    Scaffold(
        topBar = {
            // 自定义搜索栏，接受一个回调函数用于ui响应搜索文本的变化
            CustomSearchBar(onTextChanged = todoViewModel::setSearchQuery)
        },
        content = {

            // 主页 内容区，用padding手动设置顶部栏底部栏留白
            Column(
                //contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(state = rememberScrollState())
                    .padding(top = 60.dp, bottom = 44.dp)
            ) {
                filteredBoxesWithTodos.forEach { (todoBox, todoDatas) ->
                    TodoBox(
                        todoBoxId = todoBox.id ?: error("Missing todoBoxId"),
                        title = todoBox.title,
                        todos = todoDatas,
                        onEdit = { todoId, isNew ->
                            // 实现导航到编辑页面的逻辑
                            navHostController.navigate("todos/edit/${todoId}?isNew=false&todoBoxId=${todoBox.id}")
                        },
                        onAddButtonClick = {
                            // 实现添加待办事项的逻辑
                            navHostController.navigate("todos/edit/-1?isNew=true&todoBoxId=${todoBox.id}")
                        },
                        onTodoCheckedChange = { todo, isChecked ->
                            // 实现待办事项的勾选状态变化逻辑
                            todoViewModel.onTodoCheckedChange(todo, isChecked)
                        },
                        onDeleteBox = { todoBoxId ->
                            // 实现删除收纳盒的逻辑
                            todoViewModel.deleteTodoBoxById(todoBoxId)
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .padding(bottom = 50.dp),
                shape = RoundedCornerShape(50.dp),
                containerColor = Color(0xFFE49D8F), // 设置浅黄色背景色
                onClick = {
                    // 新建收纳盒
                    showDialog = true
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add TodoBox",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
    )

    var boxTitle by remember { mutableStateOf("") }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("新建收纳盒") },
            text = {

                TextField(
                    value = boxTitle,
                    onValueChange = { boxTitle = it },
                    label = { Text("收纳盒标题") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 调用todoViewModel的方法新建收纳盒并持久化存储
                        val newBox = ToDoBox(title = boxTitle)
                        todoViewModel.insertBox(newBox)

                        showDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}


// 收纳盒
@Composable
fun TodoBox(
    todoBoxId: Long,
    title: String,
    todos: List<ToDoData>,
    onEdit: (todoId: Long?, isNew: Boolean) -> Unit,
    onAddButtonClick: () -> Unit,
    onTodoCheckedChange: (ToDoData, Boolean) -> Unit,
    onDeleteBox: (Long) -> Unit, // 新增一个删除收纳盒的回调函数
) {
    var isExpanded by remember { mutableStateOf(true) }

    // 收纳盒卡片
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)

            .clip(RoundedCornerShape(8.dp)),
    ) {
        // 竖向容器：标题功能栏 + 内容栏 + 底部功能栏
        Column(modifier = Modifier.background(Color(0xffEEEEEE).copy(alpha = 1f))) {
            // 收纳盒标题栏及相关按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp)
                    .padding()
                    .background(Color(0xffFFD39B))
                    .clickable(onClick = { isExpanded = !isExpanded })
                    .padding(start = 8.dp)
                //.border(width = 1.dp, color = Color.Red)
            ) {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                // 删除盒子
                IconButton(onClick = { onDeleteBox(todoBoxId) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                }

                // 折叠/展开盒子
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Toggle expansion"
                    )
                }
            }

            // 折叠展开动画效果及列表内容
            AnimatedVisibility(visible = isExpanded, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    modifier = Modifier
                        .padding(top = 6.dp)
//                        .background(Color(0xFFFFFFFF).copy(alpha = 0.85f))
                    //.border(width = 1.dp, color = Color.Blue)
                ) {
                    todos.forEach() { todo ->
                        ToDosItem(
                            todo,
                            onEdit,
                            onCheckedChange = { todoData, isChecked ->
                                onTodoCheckedChange(
                                    todoData,
                                    isChecked
                                )
                            },
                        ) // 需要自行实现 ToDoItem
                    }
                }
            }

            // 底部添加按钮
            Button(
                onClick = onAddButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
//                    .background(Color(0xFFFFFFFF).copy(alpha = 0.85f))
                //.padding(bottom = 5.dp)
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(Color.Transparent),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加待办事项",
                        tint = Color.Black,
                    )
                    Text(
                        text = "添加待办事项",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


@Composable
fun ToDosItem(
    todo: ToDoData,
    onEdit: (todoId: Long?, isNew: Boolean) -> Unit,
    onCheckedChange: (ToDoData, Boolean) -> Unit,
) {
    val rowAlpha = if (todo.isChecked == true) ContentAlpha.disabled else ContentAlpha.high
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
        //.clickable(onClick = { onEdit(todo.id, false) })
    ) {

        //  复选框
        Column(
            modifier = Modifier
                //.border(width = 1.dp, color = Color.Blue)
                .fillMaxWidth(0.1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            todo.isChecked?.let {
                CustomCheckbox(
                    checked = it,
                    onCheckedChange = { newChecked ->
                        onCheckedChange(todo.copy(isChecked = newChecked), newChecked)
                    },
                    priority = todo.priority,
                )
            }
        }


        // 内容 标题、详情、优先级
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(start = 4.dp)
                // 编辑清单
                .clickable(onClick = { onEdit(todo.id, false) })
            //.border(width = 1.dp, color = Color.Blue)
        ) {
            Text(text = todo.title, style = MaterialTheme.typography.bodyMedium)
            //Text(text = todo.description, style = MaterialTheme.typography.bodySmall)
            MarkdownText(
                markdown = todo.description,
                style = MaterialTheme.typography.bodySmall,
                // 其他所需的参数...
            )
            Text(text = "", style = TextStyle(textDecoration = TextDecoration.LineThrough))
        }

        // 按钮操纵
        Column(
            modifier = Modifier
                .fillMaxWidth(1f),
            //.border(width = 1.dp, color = Color.Red)
            verticalArrangement = Arrangement.Center
        ) {
            Image(imageVector = Icons.Default.List, contentDescription = "清单操作")
        }

    }
}

// 自定义复选框样式和数据存储
@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    priority: Priority,
) {
    Surface(
        shape = CircleShape,
        contentColor = if (checked) MaterialTheme.colorScheme.onSurface else Color.Unspecified,
        border = BorderStroke(
            width = 2.dp,
            color = when (priority) {
                Priority.高 -> Color.Red
                // Color(0xFFFF8C00)
                Priority.中 -> Color(0xFF6495ED)
                Priority.低 -> Color(0xFF006400)
            }
        ),
        modifier = Modifier.size(22.dp),

        elevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .clickable(onClick = { onCheckedChange(!checked) }),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Checked",
                    tint = MaterialTheme.colorScheme.inverseSurface
                )
            }
        }
    }
}

@Composable
fun CustomSearchBar(
    onTextChanged: (String) -> Unit
) {

    var searchText by remember { mutableStateOf("") }

    Row (
        modifier = Modifier
            .padding(16.dp, 6.dp, 16.dp, 6.dp)
            .fillMaxWidth()
            .height(44.dp)
            //.border(1.dp, Color.Red)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFFFFEBCD)),
        verticalAlignment = Alignment.CenterVertically
    ){

        BasicTextField(
            value = searchText,
            onValueChange = { newText ->
                searchText = newText
                onTextChanged(newText)
            },
            modifier = Modifier
                .padding(start = 18.dp)
                //.border(1.dp, Color.Blue)
                .fillMaxWidth(0.85f),
            textStyle = TextStyle(fontSize = 14.sp)
        ) {
            if (searchText.isEmpty()) {
                Text(
                    text = "搜搜看",
                    color = Color(0xffb4b4b4),
                    fontSize = 15.sp
                )
            }
            it()
        }
        Box (
            modifier = Modifier
                //.border(1.dp, Color.Black)
                .padding(4.dp)

                .aspectRatio(1f)
                .clip(CircleShape)

                .background(Color(0XFFFA9E51))
        ){
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                tint = Color.White
            )
        }

    }


}

