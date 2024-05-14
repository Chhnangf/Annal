package com.chhangf.annal.data.viewmodel.todo

import android.util.Log
import com.chhangf.annal.data.model.todo.ToDoDao
import com.chhangf.annal.data.core.todo.Activity
import com.chhangf.annal.data.core.todo.Status
import com.chhangf.annal.data.core.todo.ToDoBox
import com.chhangf.annal.data.core.todo.ToDoBoxWithTodos
import com.chhangf.annal.data.core.todo.ToDoData
import com.chhangf.annal.data.core.todo.ToDoDataWithDate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
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
    suspend fun getboxesWithTodosByDateAndID(boxId: Long, selectedDate: LocalDate): List<ToDoBoxWithTodos> {

        val dateStart = selectedDate.atStartOfDay()
        val dateEnd = selectedDate.atTime(LocalTime.MAX)

        val boxesWithTodos = toDoDao.getBoxesByID(boxId).map { box ->
            val todos = toDoDao.getTodosByBoxDate(box.id!!, dateStart, dateEnd)
            val filteredTodos = todos.filter { it.status != Status.DELETED }
            val doneCount = filteredTodos.count { it.isChecked }
            ToDoBoxWithTodos(box, filteredTodos, doneCount)
        }
        val logMessage = boxesWithTodos.joinToString(separator = "\n\n") { it.toString().replace("\n", "\n  ") }
        Log.d("ToDoRepository", "getboxesWithTodosByDateAndID: selectedDate $selectedDate\n logMessage: \n$logMessage")

        return boxesWithTodos
    }

    suspend fun getboxesWithTodosByDate(selectedDate: LocalDate): List<ToDoBoxWithTodos> {

        val dateStart = selectedDate.atStartOfDay()
        val dateEnd = selectedDate.atTime(LocalTime.MAX)

        val boxesWithTodos = toDoDao.getAllBoxes().map { box ->
            val todos = toDoDao.getTodosByBoxDate(box.id!!, dateStart, dateEnd)
            val filteredTodos = todos.filter { it.status != Status.DELETED }
            val doneCount = filteredTodos.count { it.isChecked }
            ToDoBoxWithTodos(box, filteredTodos, doneCount)
        }
        val logMessage = boxesWithTodos.joinToString(separator = "\n\n") { it.toString().replace("\n", "\n  ") }
        Log.d("ToDoRepository", "getboxesWithTodosByDate: selectedDate $selectedDate\n logMessage: \n$logMessage")

        return boxesWithTodos
    }

    // 根据所有的box查询日期范围内的tododata
    suspend fun getTodoBoxesWithTodos(selectedDate: LocalDate): List<ToDoBoxWithTodos> {
        val dateStart = selectedDate.atStartOfDay()
        val dateEnd = selectedDate.atTime(LocalTime.MAX)

        val boxWithTodos = toDoDao.getAllBoxes().map { box ->
            val todos = toDoDao.getTodosByBoxDate(box.id!!, dateStart, dateEnd)
            val filteredTodos = todos.filter { it.status != Status.DELETED }
            val doneCount = filteredTodos.count { it.isChecked }
            ToDoBoxWithTodos(box, filteredTodos, doneCount)
        }

        return boxWithTodos
    }

    // 新增一个函数用于获取选定日期的整体活跃度
    suspend fun getActivityOnDate(selectedDate: LocalDate): ToDoDataWithDate {
        // 开始日期 -> 结束日期，目前只实装了当天
        val dateStart = selectedDate.atStartOfDay()
        val dateEnd = selectedDate.atTime(LocalTime.MAX)

        // 根据时间段获取所有tododata
        val allTodos = toDoDao.getTodosByDate(dateStart, dateEnd)

        // 获取现存tododata的数量
        val filteredTodos = allTodos.filter { it.status != Status.DELETED }
        val todosCount = filteredTodos.size
        val todosDone = filteredTodos.count { it.isChecked }


        // 根据tododata计算活跃度
        val activity: Activity = when {
            todosCount == 0 -> Activity.NONE
            todosCount <= 1 -> Activity.LOW
            todosCount <= 2 -> Activity.MEDIUM
            else -> Activity.HIGH
        }
        //Log.d("ToDoDataRepository", "getActivityOnDate TodosCount: $todosCount TodosDone: $todosDone activity: $activity")
        return ToDoDataWithDate(selectedDate, todosCount, todosDone, activity)
    }

    suspend fun getWeeklyActivity(): List<ToDoDataWithDate> {

        // 根据日期范围获取每天的活跃度数据
        val dailyActivities = mutableListOf<ToDoDataWithDate>()
        // 计算本周的周一和周日日期
//        var startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
//        val endOfWeek = startOfWeek.plusDays(6L)
//        while (startOfWeek <= endOfWeek) {
//            val activity = getActivityOnDate(startOfWeek)
//            dailyActivities.add(activity)
//            startOfWeek = startOfWeek.plusDays(1)
//        }

        // 获取本月的第一天
        var startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())

        // 获取本月的最后一天
        val endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
        while (startOfMonth <= endOfMonth) {
            val activity = getActivityOnDate(startOfMonth)
            dailyActivities.add(activity)
            startOfMonth = startOfMonth.plusDays(1)
        }

        val logMessage = dailyActivities.joinToString(separator = "\n") { it.toString() }
        //Log.d("ToDoRepository", "getWeeklyActivity:\n$logMessage")
        Log.d("getWeeklyActivity", "调用次数")
        return dailyActivities
    }

    // 4-29 新增对box的筛选
    suspend fun getAllBoxes(): List<ToDoBox> = toDoDao.getAllBoxes()


}