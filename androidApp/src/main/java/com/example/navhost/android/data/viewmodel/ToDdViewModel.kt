package com.example.navhost.android.data.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.navhost.android.data.ToDoDao
import com.example.navhost.android.data.ToDoDatabase
import com.example.navhost.android.data.model.Status
import com.example.navhost.android.data.model.SubTask
import com.example.navhost.android.data.model.ToDoBox
import com.example.navhost.android.data.model.ToDoData
import com.example.navhost.android.data.repository.ToDoRepository
import com.example.navhost.android.worker.ReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    // 实例化ToDoDao、ToDoRepository
    private val toDoDao: ToDoDao
    private val repository: ToDoRepository

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> get() = _selectedDate

    private val _totalDoneTodosCount = MutableStateFlow(0)
    val totalDoneTodosCount: StateFlow<Int> = _totalDoneTodosCount



    init {

        val database = ToDoDatabase.getDatabase(application)
        toDoDao = database.toDoDao()

        // 创建并初始化ToDoRepository实例
        repository = ToDoRepository(toDoDao)

        // 初始化数据表数据
        viewModelScope.launch {
            fetchTodoBoxesWithTodosByModifiedDate(selectedDate.value)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val doneCount = toDoDao.getTodoCount()
            withContext(Dispatchers.Main) {
                _totalDoneTodosCount.value = doneCount
            }
        }


    }



    // 添加获取单个待办事项的方法
    suspend fun getTodoById(id: Long): Flow<ToDoData> {
        return repository.getTodoById(id)
    }

    fun deleteItem(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(toDoData)
            fetchTodoBoxesWithTodosByModifiedDate(selectedDate.value)
        }
    }


    fun insertOrUpdateData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            val subTasks = toDoData.description?.let { processDescriptionIntoSubTasks(it) }
            if (subTasks != null) {
                toDoData.subTasks = subTasks
            }

            val existingTodo = toDoData.id?.let { repository.getTodoById(it) }
            if (existingTodo == null) {
                Log.d(
                    "ToDoViewModel",
                    "Inserting todo with ID: ${toDoData.id} ${toDoData.title} ${toDoData.description} ${toDoData.lastModifiedAt} ${toDoData.status} ${selectedDate.value} ${toDoData.subTasks}"
                )
                repository.insertData(toDoData)
            } else {
                Log.d(
                    "ToDoViewModel",
                    "Updating  todo with ID: ${toDoData.id} ${toDoData.title} ${toDoData.description} ${toDoData.lastModifiedAt} ${toDoData.status} ${selectedDate.value} ${toDoData.subTasks}"
                )
                repository.updateData(toDoData)
            }

            // 添加以下代码，判断是否存在提醒时间并安排通知
            val delay = calculateReminderDelay(toDoData)
            Log.d(
                "ToDoViewModel",
                "insertOrUpdateData -> reminderTime: $selectedDate.value with delay of $delay ms for todo: ${toDoData.title}"
            )

            if (toDoData.reminderTime != null) {
                if (delay > 0) {
                    val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)

                        .setInputData(
                            workDataOf(
                                "TODO_ID" to toDoData.id,
                                "TITLE" to toDoData.title,
                                // 不再传递提醒时间字符串，因为通知触发时不再需要解析时间
                            )
                        )
                        .build()

                    WorkManager.getInstance(getApplication()).enqueue(workRequest)
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                fetchTodoBoxesWithTodosByModifiedDate(selectedDate.value)
            }
        }
    }


    // 2024-4-8-3：24
    // 插入新的收纳盒
    fun insertBox(box: ToDoBox) = viewModelScope.launch(Dispatchers.IO) {
        val newBoxId = repository.insertBox(box)
        if (newBoxId > 0) {
            Log.d("ToDoViewModel", "New box created with ID: $newBoxId")
        } else {
            Log.w("ToDoViewModel", "Failed to create new box or get its ID.")
        }
        // 更新StateFlow中的数据
        viewModelScope.launch(Dispatchers.Main) {
            fetchTodoBoxesWithTodosByModifiedDate(selectedDate.value)
        }
    }

    // 根据ID删除收纳盒
    fun deleteTodoBoxById(boxId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTodoBoxById(boxId)
            fetchTodoBoxesWithTodosByModifiedDate(selectedDate.value)
        }
    }

    fun updateTodoStatus(todoId: Long, newStatus: Status) {
        viewModelScope.launch {

            // 根据todoId获取该ToDoData数据
            val existingTodo = repository.getTodoById(todoId)
            // 订阅getTodoById返回的Flow，并使用transform修改数据
            repository.getTodoById(todoId).transform { todoData ->
                // 修改状态字段
                todoData.status = newStatus
                // 发送修改后的数据
                emit(todoData)
            }.firstOrNull()?.let { updatedTodo ->
                // 将修改后的数据保存回数据库
                repository.updateData(updatedTodo)
            }

        }
    }

    /**
     *  for search
     */
    // 搜索查询 StateFlow
    private val _searchQuery = MutableStateFlow("")

    // 用户输入内容的接口，将数据存储到_searchQuery中供后续读取
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val searchQuery: StateFlow<String> get() = _searchQuery

    // 针对todoBoxesWithTodos进行搜索过滤
    // 2024-4-8-3：24
    // 搜索栏显示包含todo title的box
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredBoxesWithTodos: StateFlow<List<Serializable>> =
        _searchQuery.flatMapLatest { query ->
            when {
                // 搜索栏为空时显示所有盒子及其内容
                query.isEmpty() -> todoBoxesWithTodosByDate
                // 搜索栏有内容时遍历titile是否匹配，返回其盒子及其内容
                else -> todoBoxesWithTodosByDate.map { boxes ->
                    boxes.map { (box, todos) ->
                        box to todos.filter { todo ->
                            todo.status != Status.DELETED && todo.title.contains(
                                query,
                                ignoreCase = true
                            )
                        }
                    }.filter { (_, filteredTodos) -> filteredTodos.isNotEmpty() }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    /**
     *  4-15新增对数据库日期字段操作的api
     *  订阅_todoBoxesWithTodosByDate 观察ToDoBox、ToDoData数据流
     *  暴露todoBoxesWithTodosByDate 给ui层和数据层，提供获取value的方法
     */
    private val _todoBoxesWithTodosByDate =
        MutableStateFlow<List<Triple<ToDoBox, List<ToDoData>, Int>>>(emptyList())
    val todoBoxesWithTodosByDate: StateFlow<List<Triple<ToDoBox, List<ToDoData>, Int>>> get() = _todoBoxesWithTodosByDate


    // getTodoBoxesWithTodosByModifiedDate获取ToDoBox、ToDoData表内容
    // 更新_todoBoxesWithTodosByDate、_selectedDate
    private suspend fun fetchTodoBoxesWithTodosByModifiedDate(selectedDate: LocalDate) {

        val result = repository.getTodoBoxesWithTodosByModifiedDate(selectedDate)
        Log.d("ToDoViewModel", "fetchTodoBoxesWithTodosByModifiedDate result: $result")
        _todoBoxesWithTodosByDate.emit(result)
        _selectedDate.value = selectedDate

    }

    fun fetchTodoBoxesBySelectedDate(selected: LocalDate) = viewModelScope.launch(Dispatchers.IO) {
        val result = repository.getTodoBoxesWithTodosByModifiedDate(selected)
        _todoBoxesWithTodosByDate.emit(result)
        fetchTodoBoxesWithTodosByModifiedDate(selected)
    }

    // todo 待修改逻辑有bug
    fun processDescriptionIntoSubTasks(description: String): List<SubTask> {
        return description.split("\n").filter { it.isNotBlank() }.mapIndexed { index, line ->
            SubTask(index = index, description = line.trim(), isChecked = false)
        }
    }

    // 4-20 for Tsk CheckBox State
    // for Todos

    fun updateTodoState(todo: ToDoData, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            var updatedTodo = todo.copy(isChecked = isChecked)
            // 当母任务被勾选时，同步更新所有子任务的状态为已勾选
            updatedTodo = if (isChecked) {
                val allSubTasksChecked = updatedTodo.subTasks.map { it.copy(isChecked = true) }
                updatedTodo.copy(subTasks = allSubTasksChecked, status = Status.COMPLETED)
            } else {
                val allSubTasksChecked = updatedTodo.subTasks.map { it.copy(isChecked = false) }
                updatedTodo.copy(subTasks = allSubTasksChecked, status = Status.PENDING)
            }

            repository.updateData(updatedTodo)


            fetchTodoBoxesWithTodosByModifiedDate(selectedDate.value)
        }
    }
    // for SubTask
    fun updateSubTaskState(todo: ToDoData, subTaskIndex: Int, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedSubTask = todo.subTasks.getOrNull(subTaskIndex)?.copy(isChecked = isChecked)
            if (updatedSubTask != null) {
                val updatedTodoSubTasks = todo.subTasks.map {
                    if (it.index == subTaskIndex) updatedSubTask else it
                }
                val areAllSubTasksChecked = updatedTodoSubTasks.all { it.isChecked }

                val updatedTodo = todo.copy(
                    subTasks = updatedTodoSubTasks,
                    status = if (areAllSubTasksChecked) Status.COMPLETED else Status.IN_PROGRESS,
                    isChecked = areAllSubTasksChecked
                )

                // 先更新数据库
                repository.updateData(updatedTodo)


                fetchTodoBoxesWithTodosByModifiedDate(selectedDate.value)
            }

        }
    }

    // 4-22 for todo计数

}


fun calculateReminderDelay(toDoData: ToDoData): Long {

    val currentDate = LocalDate.now(ZoneId.systemDefault())
    // 检查reminderTime是否为null，并提供一个默认值（例如当前时间）
    val reminderTime = toDoData.reminderTime ?: LocalTime.now(ZoneId.systemDefault())


    val reminderDateTime = LocalDateTime.of(currentDate, reminderTime)
    // 将提醒时间转换为Instant对象
    val reminderInstant = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant()

    val currentTime = Instant.now()
    // 计算延迟时间（以毫秒为单位）
    val delay = ChronoUnit.MILLIS.between(currentTime, reminderInstant)

    return delay
}