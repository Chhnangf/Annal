package com.example.navhost.android.data.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.navhost.android.data.ToDoDao
import com.example.navhost.android.data.ToDoDatabase
import com.example.navhost.android.data.model.ToDoBox
import com.example.navhost.android.data.model.ToDoData
import com.example.navhost.android.data.repository.ToDoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    // 实例化ToDoDao、ToDoRepository
    private val toDoDao: ToDoDao
    private val repository: ToDoRepository

    private val sortedHighPriority: StateFlow<List<ToDoData>>
    private val sortedLowPriority: StateFlow<List<ToDoData>>

    init {
        val database = ToDoDatabase.getDatabase(application)
        toDoDao = database.toDoDao()

        // 创建并初始化ToDoRepository实例
        repository = ToDoRepository(toDoDao)

        sortedHighPriority = repository.sortByHighPriority.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )
        sortedLowPriority = repository.sortByLowPriority.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        // 初始化数据表数据
        viewModelScope.launch {
            fetchTodoBoxesWithTodos()
        }
    }

    // 添加获取单个待办事项的方法
    fun getTodoById(id: Long): Flow<ToDoData> {
        return repository.getTodoById(id)
    }


    fun insertOrUpdateData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            if (toDoData.id == null) {
                repository.insertData(toDoData)
            } else {
                repository.updateData(toDoData)
            }
            fetchTodoBoxesWithTodos()
        }
    }

    fun deleteItem(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(toDoData)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

    /**
     *  for checkbox
     */
    fun onTodoCheckedChange(todo: ToDoData, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTodo = todo.copy(isChecked = isChecked)
            repository.updateData(updatedTodo)
            fetchTodoBoxesWithTodos()
        }
    }

    /**
     *  for todoBoxs with todos
     */
    // 使用MutableStateFlow存储带有待办事项的收纳盒集合
    private val _boxesWithTodos  = MutableStateFlow<List<Pair<ToDoBox, List<ToDoData>>>>(emptyList())
    val todoBoxesWithTodos: StateFlow<List<Pair<ToDoBox, List<ToDoData>>>> get() = _boxesWithTodos

    // 异步获取带有待办事项的收纳盒数据
    private suspend fun fetchTodoBoxesWithTodos() {
        // 在这里确保调用了数据库或其他来源获取数据，并且在数据准备好之后，使用_emit_方法更新_stateFlow_
        val data = repository.getTodoBoxesWithTodos() // 假设repository中有获取数据的方法
        _boxesWithTodos.emit(data) // 确保_boxesWithTodos_已被初始化，不会为空
    }

    // 获取所有收纳盒及其待办事项
    suspend fun getTodoBoxesWithTodos(): List<Pair<ToDoBox, List<ToDoData>>> {
        return repository.getTodoBoxesWithTodos()
    }

    // 2024-4-8-3：24
    // 插入新的收纳盒
    fun insertBox(box: ToDoBox) = viewModelScope.launch(Dispatchers.IO) {
        val newBoxId = repository.insertBox(box)
        if (newBoxId > 0) {
            Log.d("ToDoBoxCreation", "New box created with ID: $newBoxId")
        } else {
            Log.w("ToDoBoxCreation", "Failed to create new box or get its ID.")
        }
        // 更新StateFlow中的数据
        fetchTodoBoxesWithTodos()
    }


    // 根据ID删除收纳盒
    fun deleteTodoBoxById(boxId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTodoBoxById(boxId)
            // 更新收纳盒与待办事项列表
            fetchTodoBoxesWithTodos()
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
    val filteredBoxesWithTodos: StateFlow<List<Pair<ToDoBox, List<ToDoData>>>> = _searchQuery.flatMapLatest { query ->
        when {
            // 搜索栏为空时显示所有盒子及其内容
            query.isEmpty() -> todoBoxesWithTodos
            // 搜索栏有内容时遍历titile是否匹配，返回其盒子及其内容
            else -> todoBoxesWithTodos.map { boxes ->
                boxes.map { (box, todos) ->
                    box to todos.filter { todo -> todo.title.contains(query, ignoreCase = true) }
                }.filter { (_, filteredTodos) -> filteredTodos.isNotEmpty() }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}