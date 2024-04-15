package com.example.navhost.android.data.repository

import android.util.Log
import com.example.navhost.android.data.ToDoDao
import com.example.navhost.android.data.model.ToDoBox
import com.example.navhost.android.data.model.ToDoData
import com.example.navhost.android.data.model.TodoStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject


/**
 *  仓储API，封装对ToDoDao操作的调用接口
 */
class ToDoRepository @Inject constructor (private val toDoDao: ToDoDao) {

    val sortByHighPriority: Flow<List<ToDoData>> = toDoDao.sortByHighPriorityFlow()
    val sortByLowPriority: Flow<List<ToDoData>> = toDoDao.sortByLowPriorityFlow()

    // 添加获取单个待办事项的方法
    fun getTodoById(id: Long): Flow<ToDoData> {
        return toDoDao.getById(id)
    }

    suspend fun insertOrUpdateData(toDoData: ToDoData) {
        if (toDoData.id == null) {
            val entity = toDoData.copy() // 直接复制整个对象，Room 会自动应用 TypeConverter
            toDoDao.insertData(entity)
        } else {
            val entity = toDoData.copy() // 直接复制整个对象，Room 会自动应用 TypeConverter
            toDoDao.updateData(entity)
        }
    }

    suspend fun insertData(toDoData: ToDoData) {
        toDoDao.insertData(toDoData)
    }

    suspend fun updateData(toDoData: ToDoData) {
        toDoDao.updateData(toDoData)
    }

    suspend fun deleteItem(toDoData: ToDoData) {
        toDoDao.deleteItem(toDoData)
    }

    suspend fun deleteAll() {
        toDoDao.deleteAll()
    }

    fun searchDatabase(searchQuery: String): Flow<List<ToDoData>> {
        return toDoDao.searchDatabaseFlow(searchQuery)
    }


    /**
     *  for TodoBox
     */

// 获取所有收纳盒及其待办事项
    suspend fun getTodoBoxesWithTodos(): List<Pair<ToDoBox, List<ToDoData>>> {
        val boxes = toDoDao.getAllTodoBoxes()
        return boxes.map { box ->
            val todos = toDoDao.getTodosByBoxId(box.id!!)
            val todoStates = todos.filter { it.status != TodoStatus.DELETED }

            // Log
            Log.d("ToDoRepository", "Fetched todo box: ${box.title}, containing ${todos.size} todos:")
            todos.forEachIndexed { index, todo ->
                Log.d("ToDoRepository", "  - Todo #${index + 1}: Title: ${todo.title}, Status: ${todo.status.displayText}")
            }

            Pair(box, todoStates)
        }
    }

    // 插入新的收纳盒
    suspend fun insertBox(box: ToDoBox): Long = toDoDao.insertToDoBox(box)

    // 插入新的待办事项
    suspend fun insertTodo(todo: ToDoData) {
        toDoDao.insertToDoData(todo)
    }

    // 根据ID删除收纳盒
    suspend fun deleteTodoBoxById(boxId: Long) {
        toDoDao.deleteTodoBoxById(boxId)
    }

    // 4-15新增对数据库日期字段操作的api
    // 获取指定日期关联的待办盒子及其待办事项
    suspend fun getTodoBoxesWithTodosByModifiedDate(selectedDate: LocalDate): List<Pair<ToDoBox, List<ToDoData>>> {
        val dateStart = selectedDate.atStartOfDay()
        //val dateEnd = dateStart.plusDays(1) // 包含整天的待办事项
        val dateEnd = selectedDate.atTime(LocalTime.MAX)

        val boxes = toDoDao.getBoxesByModifiedDate(dateStart, dateEnd)
        return boxes.map { box ->
            Log.d("ToDoRepository", "todo by date: ${box.title}, dateStart: $dateStart, dateEnd: $dateEnd")
            val todos = toDoDao.getTodosByBoxIdAndModifiedDate(box.id!!, dateStart, dateEnd)
            val todoStates = todos.filter { it.status != TodoStatus.DELETED }
            Pair(box, todoStates)
        }
    }

}