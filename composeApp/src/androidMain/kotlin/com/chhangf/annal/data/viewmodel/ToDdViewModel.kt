package com.chhangf.annal.data.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.chhangf.annal.data.ToDoDao
import com.chhangf.annal.data.ToDoDatabase
import com.chhangf.annal.data.model.Status
import com.chhangf.annal.data.model.SubTask
import com.chhangf.annal.data.model.ToDoBox
import com.chhangf.annal.data.model.ToDoBoxWithTodos
import com.chhangf.annal.data.model.ToDoData
import com.chhangf.annal.data.model.ToDoDataWithDate
import com.chhangf.annal.data.repository.ToDoRepository
import com.chhangf.annal.worker.ReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    // ToDoData with Box StateFlow
    private val _todoBoxesWithTodosByDate =
        MutableStateFlow<List<ToDoBoxWithTodos>>(emptyList())
    val todoBoxesWithTodosByDate: StateFlow<List<ToDoBoxWithTodos>>
        get() = _todoBoxesWithTodosByDate

    // ToDoData with Activity 无/低/中/高
    private val _todoWithActivity =
        MutableStateFlow<List<ToDoDataWithDate>>(emptyList())
    val todoWithActivity: StateFlow<List<ToDoDataWithDate>> get() = _todoWithActivity


    private val _selectedBoxId =
        MutableStateFlow<Long?>(null)
    val selectedBoxId: StateFlow<Long?> get() = _selectedBoxId

    private var _isAllSelected = MutableStateFlow(true) // 初始状态为 true，表示“全部”被选中
    val isAllSelected: StateFlow<Boolean> get() = _isAllSelected

    // Use _selectedDate save LocalDate, The default date is now
    // Use selectedDate get LocalDate the value
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> get() = _selectedDate

    private val _totalDoneTodosCount = MutableStateFlow(0)
    val totalDoneTodosCount: StateFlow<Int> = _totalDoneTodosCount

    // Use _todoBoxesList save List<ToDoBox>
    // Use todoBoxesList get List<ToDoBox> the value
    private val _todoBoxesList = MutableStateFlow<List<ToDoBox>>(emptyList())
    val todoBoxesList: StateFlow<List<ToDoBox>> get() = _todoBoxesList

    init {

        val database = ToDoDatabase.getDatabase(application)
        toDoDao = database.toDoDao()

        // 创建并初始化ToDoRepository实例
        repository = ToDoRepository(toDoDao)

        // 初始化数据表数据
        viewModelScope.launch {
            refreshStateFlowByDate(selectedDate.value)
        }

        viewModelScope.launch(Dispatchers.IO) {

            val doneCount = toDoDao.getTodoCount()
            withContext(Dispatchers.Main) {
                _totalDoneTodosCount.value = doneCount
            }
            refreshData()
            checkDefaultBoxes()
            val allBoxes = repository.getAllBoxes()
            _todoBoxesList.emit(allBoxes)
        }


    }

    // 刷新数据，根据日期和Box ID
    private suspend fun refreshData() {
        viewModelScope.launch {
            _todoBoxesWithTodosByDate.value = when (_selectedBoxId.value) {
                null -> repository.getboxesWithTodosByDate(selectedDate.value)
                else -> repository.getboxesWithTodosByDateAndID(
                    _selectedBoxId.value!!,
                    selectedDate.value
                )
            }

            _todoWithActivity.emit(repository.getWeeklyActivity())
        }
    }

    // ***数据库 增/删/改/查 都会调用这个方法 刷新stateflow数据 ****
    private suspend fun refreshStateFlowByDate(selectedDate: LocalDate) {

        // update _selectedDate is Calendar selected Date
        _selectedDate.value = selectedDate

        // update _todoWithActivity is the return value of getWeeklyActivity()
        val weeklyActivity = repository.getWeeklyActivity()
        _todoWithActivity.emit(weeklyActivity)

    }

    // 选中指定ID的Box，用于初始化或UI交互
    fun selectBox(boxId: Long?) = viewModelScope.launch(Dispatchers.IO) {
        _selectedBoxId.value = boxId
        if (boxId != null) {
            _isAllSelected.value = false
        } else {
            _isAllSelected.value = true
        }

        Log.d("ToDoViewModel", "_isAllSelected: ${_isAllSelected.value}")
        refreshData()
    }

    // expose to ui the api
    fun fetchCalendarDate(date: LocalDate) = viewModelScope.launch(Dispatchers.IO) {
        _selectedDate.value = date
        refreshData()
    }

    // 添加获取单个待办事项的方法
    suspend fun getTodoById(id: Long): Flow<ToDoData> {
        return repository.getTodoById(id)
    }

    fun deleteItem(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(toDoData)
            refreshStateFlowByDate(selectedDate.value)
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
                    "Inserting todo with : ${toDoData.id} ${toDoData.title} ${toDoData.description} ${toDoData.lastModifiedAt} ${toDoData.status} ${selectedDate.value} ${toDoData.subTasks}"
                )
                repository.insertData(toDoData)
            } else {
                Log.d(
                    "ToDoViewModel",
                    "Updating  todo with : ${toDoData.id} ${toDoData.title} ${toDoData.description} ${toDoData.lastModifiedAt} ${toDoData.status} ${selectedDate.value} ${toDoData.subTasks}"
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
                refreshData()
            }
        }
    }

    // 2024-4-8-3：24
    // 插入新的收纳盒
    fun insertBox(box: ToDoBox) = viewModelScope.launch(Dispatchers.IO) {

        val newBoxId = repository.insertBox(box)
        _todoBoxesList.emit(repository.getAllBoxes())

        if (newBoxId > 0) {
            Log.d("ToDoViewModel", "New box created with ID: $newBoxId")
        } else {
            Log.w("ToDoViewModel", "Failed to create new box or get its ID.")
        }
        // 更新StateFlow中的数据
        viewModelScope.launch(Dispatchers.Main) {
            refreshStateFlowByDate(selectedDate.value)
        }
    }

    // 根据ID删除收纳盒
    fun deleteTodoBoxById(boxId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTodoBoxById(boxId)
            viewModelScope.launch(Dispatchers.Main) {
                refreshStateFlowByDate(selectedDate.value)
            }
        }
    }

    fun deleteTodoById(todoId: Long, newStatus: Status) {
        viewModelScope.launch {

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
            refreshData()
        }
    }


    // 用户输入内容的接口，将数据存储到_searchQuery中供后续读取

    /**
     *  4-15新增对数据库日期字段操作的api
     *  订阅_todoBoxesWithTodosByDate 观察ToDoBox、ToDoData数据流
     *  暴露todoBoxesWithTodosByDate 给ui层和数据层，提供获取value的方法
     *  4-22修改 适配taskDone(update repository return list) and activity(add dataclass ToDoBoxWithTodos)
     */


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

            refreshData()
        }
    }

    // for SubTask
    fun refreshSubState(todo: ToDoData, subTaskIndex: Int, isChecked: Boolean) {
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


                refreshData()
            }

        }
    }

    // 加载一周的活跃度数据
    suspend fun refreshWeeklyActivity() {
        _todoWithActivity.value = repository.getWeeklyActivity()
        //Log.d("ToDoViewModel","_todoDataWithDate.value ${_todoWithActivity.value}")
    }


    private val sharedPreferences by lazy {
        application.getSharedPreferences(
            "${application.packageName}_preferences",
            Context.MODE_PRIVATE
        )
    }
    private val PREF_KEY_FIRST_RUN = "com.chhnangf.annal.name.PREF_KEY_FIRST_RUN"
    fun checkDefaultBoxes() {
        viewModelScope.launch(Dispatchers.IO) {
            val isFirstRun = sharedPreferences.getBoolean(PREF_KEY_FIRST_RUN, true)
            Log.d("ToDoViewModel", "checkDefaultBoxes: $isFirstRun")
            if (isFirstRun) {
                val defaultBoxes = listOf(
                    ToDoBox(title = "日常", color = "0x12345678"),
                    ToDoBox(title = "工作", color = "0x78945612"),
                    ToDoBox(title = "学习", color = "0x96385274")
                )

                defaultBoxes.forEach() {
                    repository.insertBox(it)
                }
                refreshStateFlowByDate(selectedDate.value)
                //defaultBoxes.firstOrNull()?.id?.let { onBoxSelected(selectedDate.value, it) }
                // 更新标志表示已经不是首次启动
                with(sharedPreferences.edit()) {
                    putBoolean(PREF_KEY_FIRST_RUN, false)
                    apply()
                }
            }
        }
    }

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

