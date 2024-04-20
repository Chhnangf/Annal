package com.example.navhost.android.data.repository

import android.util.Log
import com.example.navhost.android.data.ToDoDao
import com.example.navhost.android.data.model.Activity
import com.example.navhost.android.data.model.Status
import com.example.navhost.android.data.model.ToDoBox
import com.example.navhost.android.data.model.ToDoBoxWithTodos
import com.example.navhost.android.data.model.ToDoData
import com.example.navhost.android.data.model.ToDoDataWithDate
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
    // 获取List<ToDoBox> List<ToDoData> Int 存放到ToDoBoxWithTodos数据类
    suspend fun getTodoBoxesWithTodosByDate(selectedDate: LocalDate): List<ToDoBoxWithTodos> {

        val dateStart = selectedDate.atStartOfDay()
        val dateEnd = selectedDate.atTime(LocalTime.MAX)

        val boxes = toDoDao.getBoxesByDate(dateStart, dateEnd)

        return boxes.map { box ->
            //Log.d("ToDoRepository", "boxs: ${box.title}, dateStart: $dateStart, dateEnd: $dateEnd")
            val todoWithBox = toDoDao.getTodosByBoxIDate(box.id!!, dateStart, dateEnd)
            // 过滤掉状态为deleted的todoData
            val todosFilter = todoWithBox.filter { it.status != Status.DELETED }
            val todosDone = todosFilter.count { it.isChecked }

            // 每个盒子的完成数量
            ToDoBoxWithTodos(box, todosFilter, todosDone)

        }
    }

    // 新增一个函数用于获取选定日期的整体活跃度
    suspend fun getActivityOnDate(selectedDate: LocalDate): ToDoDataWithDate {
        val dateStart = selectedDate.atStartOfDay()
        val dateEnd = selectedDate.atTime(LocalTime.MAX)

        val allTodos = toDoDao.getTodosByDate(dateStart, dateEnd)


        val filteredTodos = allTodos.filter { it.status != Status.DELETED }
        val TodosCount = filteredTodos.size
        val TodosDone = filteredTodos.count { it.isChecked }


        // 根据总的 Todos 数量计算活跃度
        val activity: Activity = when {
            TodosCount == 0 -> Activity.NONE
            TodosCount <= 1 -> Activity.LOW
            TodosCount <= 2 -> Activity.MEDIUM
            else -> Activity.HIGH
        }
        Log.d("ToDoDataRepository", "getActivityOnDate TodosCount: $TodosCount TodosDone: $TodosDone activity: $activity")
        return ToDoDataWithDate(TodosCount, TodosDone, activity)
    }

}