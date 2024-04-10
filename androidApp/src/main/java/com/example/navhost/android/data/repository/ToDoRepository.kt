package com.example.navhost.android.data.repository

import com.example.navhost.android.data.ToDoDao
import com.example.navhost.android.data.model.ToDoBox
import com.example.navhost.android.data.model.ToDoData
import kotlinx.coroutines.flow.Flow
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
            Pair(box, todos)
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

}