package com.chhangf.annal.ui.screens


//import dev.jeziellago.compose.markdowntext.MarkdownText

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.chhangf.annal.data.model.Activity
import com.chhangf.annal.data.model.Priority
import com.chhangf.annal.data.model.ToDoBox
import com.chhangf.annal.data.model.ToDoData
import com.chhangf.annal.data.viewmodel.ToDoViewModel
import com.marosseleng.compose.material3.datetimepickers.time.domain.noSeconds
import com.marosseleng.compose.material3.datetimepickers.time.ui.dialog.TimePickerDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

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

    // 4-15新增以日期为主导的数据流订阅
    val boxesWithTodos by todoViewModel.todoBoxesWithTodosByDate.collectAsState()

    val selectDate by todoViewModel.selectedDate.collectAsState()
    val selectedDateString = selectDate.toString()
    LaunchedEffect(key1 = todoViewModel) {
        todoViewModel.searchQuery.collectLatest { query ->
            searchQuery = query
        }
    }

    // 订阅收纳盒及其内容数据流
    // val boxesWithTodosState by todoViewModel.todoBoxesWithTodos.collectAsState(emptyList())

    // 底部动作条
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    // 使用Scaffold创建包含顶部栏和底部栏的布局
    Scaffold(

        topBar = {
            // 自定义搜索栏，接受一个回调函数用于ui响应搜索文本的变化
            Card(
                modifier = Modifier,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Color(0xff548383).copy(alpha = 0.8f)) //绿色ff548383 蓝色ff7da0ca
                ) {
                    // 搜索框
                    CustomSearchBar(onTextChanged = todoViewModel::setSearchQuery)

                    // 主题（诗文 + 按钮）
                    CustomTitle()

                    CustomCalendar(
                        todoViewModel = todoViewModel,
                        onDateSelected = { selectedDate ->
                            Log.d(
                                "ToDoScreen",
                                "selectedDate -> CustomCalendar: $selectedDate"
                            )
                            todoViewModel.fetchCalendarDate(selectedDate)
                        },
                        selectedDate = selectDate,
                    )

                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "下拉"
                        )
                    }
                }
            }
        },

        content = {

            // 主页 内容区，用padding手动设置顶部栏底部栏留白
            Column(
                //contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(state = rememberScrollState())
                    .padding(top = 240.dp, bottom = 44.dp)
            ) {
                BoxSelectionBar(todoViewModel)
                // *** 遍历数据表显示内容 *** //
                // 判断boxesWithTodos中所有的todos是否都为空
                val allTodosEmpty = boxesWithTodos.all { it.todos.isEmpty() }
                //Log.d("ToDoScreen", "allTodosEmpty -> $allTodosEmpty")
                if (allTodosEmpty) {
                    CreateTaskPrompt()
                } else {
                    boxesWithTodos.forEach { (todoBox, todos, doneCount) ->
                        TodoBox(
                            todoViewModel = todoViewModel,
                            todoBoxId = todoBox.id ?: error("Missing todoBoxId"),
                            title = todoBox.title,
                            todos = todos,
                            todoDone = doneCount,
                            onEdit = { todoId, _ ->
                                // 实现导航到编辑页面的逻辑
                                navHostController.navigate("todos/edit/${todoId}?isNew=false&todoBoxId=${todoBox.id}&selectDateAt=${selectedDateString}")
                            },
                            onAddButtonClick = {
                                // 实现添加待办事项的逻辑
                                navHostController.navigate("todos/edit/-1?isNew=true&todoBoxId=${todoBox.id}&selectDateAt=${selectedDateString}")
                            },
                            onTodoCheckedChange = { todos, isChecked ->
                                // 实现待办事项的勾选状态变化逻辑
                                todoViewModel.updateTodoState(todos, isChecked)
                            },
                            onDeleteBox = { todoBoxId ->
                                // 实现删除收纳盒的逻辑
                                todoViewModel.deleteTodoBoxById(todoBoxId)
                            },
                            onSubTaskCheckedChange = { todos, subTaskIndex, isChecked ->
                                todoViewModel.refreshSubState(todos, subTaskIndex, isChecked)
                            },
                        )
                    }
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
                    showBottomSheet = true
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

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            EditTodoForm(
                todoViewModel = todoViewModel,
                onDismissRequest = {
                    showBottomSheet = false
                },
                onUpdateToDoData = { todoDate ->
                    todoViewModel.insertOrUpdateData(todoDate)
                },
                onDeleteToDoData = { todoDate ->
                    todoViewModel.deleteItem(todoDate)
                }
            )
        }
    }
}


// 收纳盒
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TodoBox(
    todoViewModel: ToDoViewModel,
    todoBoxId: Long,
    title: String,
    todos: List<ToDoData>,
    todoDone: Int,
    onEdit: (todoId: Long?, isNew: Boolean) -> Unit,
    onAddButtonClick: () -> Unit,
    onTodoCheckedChange: (ToDoData, Boolean) -> Unit,
    onDeleteBox: (Long) -> Unit, // 新增一个删除收纳盒的回调函数
    onSubTaskCheckedChange: (ToDoData, Int, Boolean) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(true) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    if (!todos.isEmpty()) {
        // 收纳盒卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)

                .clip(RoundedCornerShape(8.dp)),
        ) {
            // 竖向容器：标题功能栏 + 内容栏 + 底部功能栏
            Column(modifier = Modifier.background(Color(0x77EEEEEE).copy(alpha = 1f))) {
                // 收纳盒标题栏及相关按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                        .padding()
                        //.background(Color(0x9FB3B8A5).copy(alpha = 0.5f))
                        .clickable(onClick = { isExpanded = !isExpanded })
                        .padding(start = 8.dp)
                    //.border(width = 1.dp, color = Color.Red)
                ) {
                    // 标题
                    Text(
                        text = title,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                        color = Color(0xFF35303C),
                        modifier = Modifier.weight(1f)
                    )
                    // 删除盒子
                    IconButton(onClick = { onDeleteBox(todoBoxId) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete"
                        )
                    }


                    Text(text = " $todoDone / ${todos.size}")
                    // 折叠/展开盒子
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
                                    onTodoCheckedChange(todoData, isChecked)
                                },
                                onSubTaskCheckedChange = { ToDoData, Int, Boolean ->
                                    onSubTaskCheckedChange(ToDoData, Int, Boolean)
                                }
                            ) // 需要自行实现 ToDoItem
                        }
                    }
                }

                // 底部添加按钮
                Button(
                    onClick = {
                        showBottomSheet = true
                    }, //onAddButtonClick
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
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            EditTodoForm(
                todoViewModel = todoViewModel,
                onDismissRequest = {
                    showBottomSheet = false
                },
                onUpdateToDoData = { todoDate ->
                    todoViewModel.insertOrUpdateData(todoDate)
                },
                onDeleteToDoData = { }
            )
        }
    }
}


@Composable
fun ToDosItem(
    todo: ToDoData,
    onEdit: (todoId: Long?, isNew: Boolean) -> Unit,
    onCheckedChange: (ToDoData, Boolean) -> Unit,
    onSubTaskCheckedChange: (ToDoData, Int, Boolean) -> Unit,
) {
    val rowAlpha = if (todo.isChecked == true) ContentAlpha.disabled else ContentAlpha.high
    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth()
            .alpha(rowAlpha)
        //.clickable(onClick = { onEdit(todo.id, false) })
    ) {

        //  母复选框
        Column(
            modifier = Modifier
                //.border(width = 1.dp, color = Color.Blue)
                .fillMaxWidth(0.1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            todo.isChecked.let {
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

            /**
             *     text = todo.reminderTime.toLocalDate().toString() +
             *            " " +
             *            todo.reminderTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
             *     您会得到类似于"2023-03-15 14:30"的格式
             *     使用正则表达式省略时间前方多余的0
             */
            val formatter = DateTimeFormatter.ofPattern("H：mm")
            val dateWithoutLeadingZero = todo.reminderTime?.format(formatter)
                ?.replaceFirst("^0+(?!$)", "")
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp, end = 8.dp)
                //.border(1.dp, Color.Red)
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (dateWithoutLeadingZero != null) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "提醒",
                        Modifier.size(16.dp)
                    )
                    dateWithoutLeadingZero.let {
                        Text(text = it, style = TextStyle(fontSize = 13.sp, color = Color.Blue))
                    }
                }
            }
            if (todo.description != "") {
                // 遍历并展示子任务
                todo.subTasks.forEachIndexed { index, subTask ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            //.border(1.dp, Color.Red)
                            .padding(vertical = 2.dp)
                    ) {
                        // 添加针对每个子任务的复选框
                        CustomCheckbox(
                            checked = subTask.isChecked,
                            onCheckedChange = { newChecked ->
                                // 调用新回调函数，传入当前子任务实例及其新的勾选状态
                                onSubTaskCheckedChange(todo, index, newChecked)
                            },
                            priority = todo.priority,
                            isSubCheckbox = true
                        )

                        // 子任务描述及其他UI元素
                        Text(text = subTask.description)

//                MarkdownText(
//                    markdown = todo.description!!,
//                    style = MaterialTheme.typography.bodySmall,
//                    // 其他所需的参数...
//                )


                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(1.5.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color.LightGray)
            )
        }

        // 按钮操纵
        Column(
            modifier = Modifier
                .fillMaxWidth(1f),
            //.border(width = 1.dp, color = Color.Red)
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "清单操作"
            )
        }
    }
}

// 自定义复选框样式和数据存储
@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    priority: Priority,
    isSubCheckbox: Boolean = false, // 新增的布尔型参数，用于区分母复选框与子复选框
) {
    val shape = if (isSubCheckbox) CircleShape else CircleShape // RectangleShape 方形
    val borderWidth = if (isSubCheckbox) 1.dp else 2.dp
    val borderColor = if (isSubCheckbox) Color.Black else when (priority) {
        Priority.HIGH -> Color.Red
        Priority.MEDIUM -> Color(0xFF6495ED)
        Priority.LOW -> Color(0xFF007900)
    }
    val boxSize = if (isSubCheckbox) 18.dp else 22.dp
    val contentColor = if (checked) MaterialTheme.colorScheme.onSurface else Color.Unspecified
    val boxPadding = 2.dp
    val elevation = if (isSubCheckbox) 1.dp else 3.dp

    Surface(
        shape = shape,
        contentColor = contentColor,
        border = BorderStroke(
            width = borderWidth,
            color = borderColor
        ),
        modifier = Modifier.size(boxSize),

        elevation = elevation
    ) {
        Box(
            modifier = Modifier
                .padding(boxPadding)
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

    // 搜索框输入状态和动画
    var searchText by remember { mutableStateOf("") }
    var isSearchBoxVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(28.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // menu
        Icon(
            modifier = Modifier
                .fillMaxWidth(0.14f),
            imageVector = Icons.Default.Menu,
            contentDescription = "侧边栏菜单"
        )

        // search
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .height(30.dp)
                // 设置背景颜色，并指定同样大小的圆角
                .fillMaxWidth(0.86f)
                .background(
                    if (isSearchBoxVisible) Color.White else Color.Transparent,
                ),

            ) {
            Row(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    modifier = Modifier,
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = Color.Black
                )
                BasicTextField(
                    value = searchText,
                    onValueChange = { newText ->
                        searchText = newText
                        onTextChanged(newText)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 5.dp)
                        .fillMaxWidth(0.8f),
                    textStyle = TextStyle(fontSize = 14.sp),

                    ) {

                    if (searchText.isEmpty()) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(0.9f),
                            text = "搜索",
                            color = Color(0xFF615959),
                            fontSize = 12.sp
                        )
                    }

                    it()
                }
                // 关闭搜索
                IconButton(
                    onClick = { isSearchBoxVisible = false },
                    modifier = Modifier
                        .height(20.dp)
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth(1f),
                )
                {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清除搜索内容",
                        tint = Color.Black,
                    )
                }

            }
        }

        // other
        Icon(
            modifier = Modifier
                .fillMaxWidth(1f),
            imageVector = Icons.Default.DateRange,
            contentDescription = "其他功能"
        )
    }


}

// 自定义滑块
@Composable
fun SwitchWithIconExample() {
    var checked by remember { mutableStateOf(true) }

    Switch(
        modifier = Modifier,
        checked = checked,
        onCheckedChange = {
            checked = it
        },
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        }
    )
}

@Composable
fun CustomCalendar(
    todoViewModel: ToDoViewModel,
    onDateSelected: (LocalDate) -> Unit, // 修改这里的参数类型
    selectedDate: LocalDate
) {

    // 日历
    Box(
        modifier = Modifier
            .padding(6.dp, 0.dp, 6.dp, 0.dp)
            .clip(RoundedCornerShape(10.dp))

    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    //.border(1.dp, Color.Red)
                    .padding(10.dp, 10.dp, 10.dp, 0.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentYearMonth by remember {
                    mutableStateOf(
                        YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-M"))
                    )
                }
                Text(text = currentYearMonth, fontSize = 24.sp)

                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "活跃低", fontSize = 12.sp)
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "活跃低"
                    )
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "活跃中"
                    )
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "活跃高"
                    )
                    Text(text = "高", fontSize = 12.sp)
                }


            }

            Column(
                modifier = Modifier.padding(6.dp, 0.dp, 6.dp, 10.dp)
            ) {

                DisplayCurrentWeekDates(
                    todoViewModel = todoViewModel,
                    onDateSelected = { selected ->
                        onDateSelected(selected) // 调用传入的 onDateSelected 回调，通知 selectedDate 的变化
                        todoViewModel.fetchCalendarDate(selected)
                        // 在这里添加更多日期被点击后的处理逻辑，例如打印日期
                        Log.d("ToDoScreen", "CustomCalendar: selected = $selected")
                    },
                    selectedDate = selectedDate,
                    today = LocalDate.now()
                )
                Log.d(
                    "ToDoScreen",
                    "CustomCalendar: selectedDate=$selectedDate today=${LocalDate.now()}"
                )
            }

        }
    }
}

val weekdayChineseMap = mapOf(
    "MON" to "一",
    "TUE" to "二",
    "WED" to "三",
    "THU" to "四",
    "FRI" to "五",
    "SAT" to "六",
    "SUN" to "日"
)


// 在使用此 Composable 时，传入一个处理日期选择的回调函数
@Composable
fun DisplayCurrentWeekDates(
    todoViewModel: ToDoViewModel,
    onDateSelected: (LocalDate) -> Unit,
    selectedDate: LocalDate?,
    today: LocalDate
) {

    // 订阅 todoViewModel 的 todoDataWithDate
    val todoDataWithDate by todoViewModel.todoWithActivity.collectAsState()
    LaunchedEffect(Unit) {
        todoViewModel.refreshWeeklyActivity()
    }

    val firstDayOfWeek =
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DayOfWeek.entries.forEach { day ->
            val shortWeekday = day.toString().substring(0, 3).uppercase(Locale.ENGLISH)
            val chineseWeekday = weekdayChineseMap[shortWeekday]
            Text(
                modifier = Modifier,
                text = chineseWeekday ?: shortWeekday,
                fontSize = 16.sp
            )
        }

    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..6) {
            val weekDate = firstDayOfWeek.plusDays(i.toLong())

            // 从 todoDataWithDate 中找到对应日期的活动度信息
            val weekDateWithDate = todoDataWithDate.find { it.selectedDate == weekDate }

            val selectDateWithDate = todoDataWithDate.find { it.selectedDate == selectedDate }

            val activity = weekDateWithDate?.activity ?: Activity.NONE
            val todoCount = weekDateWithDate?.totalTodos ?: 0
            val todoDone = weekDateWithDate?.doneTodos ?: 0
            val selectDate = selectDateWithDate?.selectedDate

            //Log.d("ToDoScreen","DisplayCurrentWeekDates:\nactivity=$activity todoCount=$todoCount todoDone=$todoDone")
            val backgroundColor = when (activity) {
                Activity.NONE -> Color.Transparent
                Activity.LOW -> Color.Green.copy(alpha = 0.15f)
                Activity.MEDIUM -> Color.Green.copy(alpha = 0.4f)
                Activity.HIGH -> Color.Green.copy(alpha = 0.65f)
            }

            CustomBottomBackgroundTextButton(
                weekDate,
                isSelectedDate = selectDate == weekDate, //selectedDate?.isEqual(weekDate) == true,
                isToday = weekDate == today,
                onClick = {
                    Log.d(
                        "DisplayCurrentWeekDates",
                        "Clicked date: $weekDate, setting as selected."
                    )
                    onDateSelected(weekDate)
                },
                doneCount = todoDone,
                totalCount = todoCount,
                backgroundColor = backgroundColor, // Color(0x12345678)
                selectedBorderColor = Color.Blue,

                ) {

            }
        }
    }
}


// 自定义日期选项样式
@Composable
fun CustomBottomBackgroundTextButton(
    recursionDate: LocalDate,
    isSelectedDate: Boolean = false, // 表示当前日期是否被选中
    isToday: Boolean = false, // 判断是否为今天
    onClick: (LocalDate) -> Unit, // 修改这里，使onClick接收LocalDate作为参数
    doneCount: Int,
    totalCount: Int,
    selectedBorderColor: Color = Color.Blue,
    todayBorderColor: Color = Color.Red,
    backgroundColor: Color = Color(0x88888888),
    content: @Composable () -> Unit
) {
    var currentProgress by remember { mutableStateOf(0f) } // 初始化进度为0
    val targetProgress by remember(doneCount, totalCount) {
        derivedStateOf {
            doneCount.toFloat() / totalCount.toFloat().coerceAtLeast(1f)
        }
    }
    LaunchedEffect(key1 = targetProgress) {
        delay(500L) // 延迟500毫秒
        currentProgress = targetProgress // 直接赋值，Compose会处理动画
    }
    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(durationMillis = 1000) // 动画持续时间1秒
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(backgroundColor)
            .then(Modifier.clickable(onClick = { onClick(recursionDate) })),
        contentAlignment = Alignment.Center
    ) {
        // 浅灰色Todo指示进度条
        if (totalCount > 0) {
            CircularProgressIndicator(
                progress = 1f, // 使用todo完成比例
                strokeWidth = 2.dp,
                color = Color.Gray.copy(alpha = 0.3f), // 浅灰色，透明度调整以保持视觉轻盈
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
            )
        }
        CircularProgressIndicator(
            progress = animatedProgress,
            strokeWidth = 2.dp,
            color = Color.Blue,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.Center)
        )
        Text(
            text = recursionDate.dayOfMonth.toString(),
            color = Color.Black,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                // 调整透明度使得文本在进度条颜色下可见（可选）
                .alpha(0.8f)
        )
        if (isToday) {
            DateRoundedRect(
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            )
        }
        if (!isToday && isSelectedDate) {
            DateRoundedRect(
                color = Color.Blue,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun DateRoundedRect(
    color: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 4.dp, // 圆角半径
    size: Dp = 9.dp, // 矩形的总尺寸，假设为正方形处理
    lineWidth: Dp = 2.dp // 线宽，实际上作为矩形的“高”
) {
    Canvas(modifier = modifier.then(Modifier.size(size, lineWidth))) {
        val rectCornerSize = CornerRadius(cornerRadius.toPx())
        drawRoundRect(
            color = color,
            size = Size(size.toPx(), lineWidth.toPx()),
            cornerRadius = rectCornerSize,
            style = Fill
        )
    }
}

// 自定义标题栏 + 切换按钮样式
@Composable
fun CustomTitle() {
    Row {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 4.dp)
        ) {
            Text(text = "代办", fontSize = 26.sp)
            Text(text = "每日计划都在这里", fontSize = 12.sp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(top = 4.dp, end = 8.dp)
            //.border(1.dp, Color.LightGray)
            ,
            horizontalArrangement = Arrangement.End
        ) {
            //SwitchWithIconExample()
        }
    }
}

@Composable
fun BoxSelectionBar(todoViewModel: ToDoViewModel) {

    val todoBox by todoViewModel.todoBoxesList.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var boxTitle by remember { mutableStateOf("") }

    var selectedBoxId by remember { mutableStateOf<Long?>(null) }
    var isAllSelected by remember { mutableStateOf(false) }

    Log.d("ToDoScreen", "BoxSelectionBar: $todoBox")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth().padding(10.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        item {
            // 全部按钮
            BoxSelectionButton(
                title = "全部",
                isSelected = isAllSelected,
                onClick = {
                    isAllSelected = true
                    selectedBoxId = null
                    todoViewModel.selectBox(null)
                },
                icon = Icons.Default.Home,
            )
        }
        items(todoBox) { box ->

            BoxSelectionButton(
                title = box.title,
                isSelected = selectedBoxId == box.id,
                onClick = {
                    selectedBoxId = box.id
                    isAllSelected = false
                    todoViewModel.selectBox(box.id)
                },
            )
        }

        item {
            // 创建按钮
            BoxSelectionButton(
                title = "创建",
                onClick = {
                    showDialog = true
                },
                icon = Icons.Default.Add
            )
        }


    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("创建标题") },
            text = {
                TextField(
                    value = boxTitle,
                    onValueChange = { boxTitle = it },
                    label = { Text("标题名称") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newBox = ToDoBox(
                            title = boxTitle,
                        )
                        newBox.let { todoViewModel.insertBox(it) }
                        todoViewModel.fetchCalendarDate(LocalDate.now())
                        // 重置 boxTitle 和 showDialog
                        boxTitle = ""
                        showDialog = false
                    }
                ) {
                    Text("创建")
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

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun BoxSelectionButton(
    title: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val backgroundColor =
        if (isSelected) Color.Black.copy(0.7f) else Color.Gray.copy(alpha = 0.3f) // 点击时背景变为深色，未点击时为白色
    val contentColor =
        if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Black // 点击时内容变为白色，未点击时为黑色
    val interactionSource = remember { MutableInteractionSource() }
    val fontSystem =
        if (isSelected) FontWeight.Bold else FontWeight.Normal
    Box(
        modifier = Modifier
            .width(70.dp)
            .height(45.dp)
            .padding(4.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 可选，为点击添加涟漪效果
                onClick = onClick

            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = contentColor, // 使用状态控制文本颜色
            modifier = Modifier
                .padding(4.dp),
            fontWeight =  fontSystem

        )
    }
}

@Composable
fun CreateTaskPrompt() {
    // 实现创建任务的引导页面，例如一个带有提示和添加按钮的简单布局
    val customTextStyle = TextStyle(
        color = Color.Gray.copy(alpha = 0.5f), // 设置颜色为浅灰色
        fontSize = 19.sp, // 设置字体大小为20sp
        fontWeight = FontWeight.Bold, // 如果需要加粗，可设置字体粗细
    )
    Box(
        modifier = Modifier.fillMaxSize(), // 让Box充满整个屏幕
        contentAlignment = Alignment.Center // 居中对齐内容
    ) {
        Column(
            modifier = Modifier
                .height((LocalConfiguration.current.screenHeightDp * 0.4f).dp)
                .padding(horizontal = 16.dp) // 可选，添加内边距
                .align(Alignment.Center), // 在垂直方向上居中
            verticalArrangement = Arrangement.Center, // 内部元素垂直居中
            horizontalAlignment = Alignment.CenterHorizontally // 内部元素水平居中
        ) {
            Text(text = "还没有任务快去创建一个吧！", style = customTextStyle)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoForm(
    todoViewModel: ToDoViewModel,
    initialTodoData: ToDoData? = null, // 添加可空参数
    onDismissRequest: () -> Unit,
    onUpdateToDoData: (ToDoData) -> Unit,
    onDeleteToDoData: (ToDoData) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        val todoBoxes = todoViewModel.todoBoxesList.collectAsState()
        val selectDate = todoViewModel.selectedDate.collectAsState()
        var selectedBoxId by remember { mutableStateOf(todoBoxes.value.firstOrNull()?.id) }

        // Box 选择器的实现（示例使用DropdownMenu）
        var expanded by remember { mutableStateOf(false) }
        val boxTitle = todoBoxes.value.find { it.id == selectedBoxId }?.title ?: "请选择Box"

        // 根据initialTodoData是否为null初始化表单状态
        var toDoDataState by remember { mutableStateOf(initialTodoData) }
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

        // 提交按钮之前添加Box选择UI
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            todoBoxes.value.forEach { box ->
                DropdownMenuItem(
                    onClick = {
                        selectedBoxId = box.id
                        expanded = false
                    },
                    text = { Text(box.title) }
                )
            }
        }


        LaunchedEffect(key1 = initialTodoData) {
            /**
             *  当查询到待办事项时，会更新title、description和selectedPriority这三个状态变量的值
             */
            title = initialTodoData?.title ?: ""
            description = initialTodoData?.description ?: ""
            selectedPriority = initialTodoData?.priority ?: Priority.LOW
            reminderText = (if (initialTodoData?.reminderTime != null) {
                initialTodoData.reminderTime!!.format(formatter)
            } else {
                "设置提醒"
            }).toString()
        }


        // 提交按钮的点击逻辑
        fun onSubmit() {
            // 构建新的ToDoData对象
            toDoDataState = ToDoData(
                title = title,
                description = description,
                priority = selectedPriority,
                reminderTime = if (reminderTimeIsSet) selectedTime else null,
                // 其他必要字段，例如创建日期或收纳盒ID，可能需要从外部传入
                todo_box_id = selectedBoxId,
                selectDateAt = selectDate.value.atStartOfDay(),
            )

            // 调用onUpdateToDoData函数
            toDoDataState?.let { onUpdateToDoData(it) }
            // 关闭表单
            onDismissRequest()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // 选择box
            Button(onClick = { expanded = !expanded }) {
                Text(text = boxTitle)
            }
            // 设置提醒
            Button(
                onClick = { isDialogShown = true },
                Modifier
                //.border(1.dp, Color.Red),
            ) {
                Text(text = reminderText)
            }

            // 优先级选择
            PrioritySelect(
                onPrioritySelected = { selectedPriority = it }
            )
        }
        // 标题输入
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("主题") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 描述输入
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("正文") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = Int.MAX_VALUE
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // 删除按钮
            Button(
                onClick = {

                },
                modifier = Modifier
            ) {
                Text(text = "删除")
            }

            // 提交按钮
            Button(
                onClick = {
                    val reminderTimeValue = if (reminderTimeIsSet) {
                        selectedTime
                    } else {
                        toDoDataState?.reminderTime
                    }
                    if (title != "") {
                        onSubmit()
                    }
                },
                modifier = Modifier
            ) {
                Text(text = "保存")
            }
        }

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
}

@Composable
fun PrioritySelect(
    onPrioritySelected: (Priority) -> Unit,
    initiallySelected: Priority = Priority.LOW
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(initiallySelected) }

    Box(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val label = selectedPriority.toString()
        Button(onClick = { expanded = true }) {
            Text(text = label)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }, // 这里修正了 expanded 的引用
            modifier = Modifier.width(80.dp)
        ) {
            Priority.entries.forEach { priority ->
                DropdownMenuItem(
                    onClick = {
                        selectedPriority = priority
                        onPrioritySelected(priority)
                        expanded = false
                    },
                    text = { Text(priority.toString()) }
                )
            }
        }
    }
}
