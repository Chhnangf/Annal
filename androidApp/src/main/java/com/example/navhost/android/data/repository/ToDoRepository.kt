package com.example.navhost.android.data.repository

import android.util.Log
import com.example.navhost.android.data.ToDoDao
import com.example.navhost.android.data.model.Status
import com.example.navhost.android.data.model.ToDoBox
import com.example.navhost.android.data.model.ToDoData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject


/**
 *  仓储API，封装对ToDoDao操作的调用接口
 */
class ToDoRepository @Inject constructor (private val toDoDao: ToDoDao) {

    /**
     *  for todos
     */
    fun getTodoById(id: Long): Flow<ToDoData> {
        return toDoDao.getById(id)
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


    /**
     *  for TodoBox
     */
    // 插入新的收纳盒
    fun insertBox(box: ToDoBox): Long = toDoDao.insertToDoBox(box)

    // 根据ID删除收纳盒
    suspend fun deleteTodoBoxById(boxId: Long) {
        toDoDao.deleteTodoBoxById(boxId)
    }

    // 4-15新增对数据库日期字段操作的api
    // 获取指定日期关联的待办盒子及其待办事项
    suspend fun getTodoBoxesWithTodosByModifiedDate(selectedDate: LocalDate): List<Pair<ToDoBox, List<ToDoData>>> {

        val dateStart = selectedDate.atStartOfDay()
        val dateEnd = selectedDate.atTime(LocalTime.MAX)

        val boxes = toDoDao.getBoxesByModifiedDate(dateStart, dateEnd)
        return boxes.map { box ->
            Log.d("ToDoRepository", "boxs: ${box.title}, dateStart: $dateStart, dateEnd: $dateEnd")
            val todos = toDoDao.getTodosByBoxIdAndModifiedDate(box.id!!, dateStart, dateEnd)
            Log.d("ToDoRepository", "todos: $todos")
            val todoStates = todos.filter { it.status != Status.DELETED }
            Pair(box, todoStates)
        }
    }

}