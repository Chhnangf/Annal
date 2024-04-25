package com.chhangf.annal.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.chhangf.annal.data.model.ToDoBox
import com.chhangf.annal.data.model.ToDoData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ToDoDao {

    @Query("SELECT * FROM todo_data ORDER BY id ASC")
    fun getAllDataFlow(): Flow<List<ToDoData>>

    @Query("SELECT * FROM todo_data WHERE id = :id")
    fun getById(id: Long): Flow<ToDoData>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(toDoData: ToDoData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateData(toDoData: ToDoData)

    @Delete
    suspend fun deleteItem(toDoData: ToDoData)

    @Query("DELETE FROM todo_data")
    suspend fun deleteAll()

    @Query("SELECT * FROM todo_data WHERE title LIKE :searchQuery")
    fun searchDatabaseFlow(searchQuery: String): Flow<List<ToDoData>>

    @Query("SELECT * FROM todo_data ORDER BY CASE WHEN priority LIKE 'H%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 3 END")
    fun sortByHighPriorityFlow(): Flow<List<ToDoData>>

    @Query("SELECT * FROM todo_data ORDER BY CASE WHEN priority LIKE 'L%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'H%' THEN 3 END")
    fun sortByLowPriorityFlow(): Flow<List<ToDoData>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTodoBox(todoBox: ToDoBox)

    @Query("SELECT * FROM todo_box")
    suspend fun getAllBoxes(): List<ToDoBox>


    @Delete
    suspend fun deleteBox(todoBox: ToDoBox)

    @Query("SELECT * FROM todo_box WHERE id = :boxId")
    suspend fun getToDoBoxById(boxId: Long): ToDoBox?

    @Transaction
    @Query("SELECT * FROM todo_box")
    fun getTodoBoxesWithTodos(): List<ToDoBox> // 注意，这里提供了一个非Flow的查询方法作为示例

    // 如果要获取Flow，可以这样写
    @Transaction
    @Query("SELECT * FROM todo_box")
    fun getTodoBoxesWithTodosFlow(): Flow<List<ToDoBox>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertToDoBox(toDoBox: ToDoBox): Long

    @Insert
    fun insertToDoData(toDoData: ToDoData)

    // 删除收纳盒
    @Query("DELETE FROM todo_box WHERE id = :boxId")
    suspend fun deleteTodoBoxById(boxId: Long)

    // 4-15 对日期字段的数据库操作支持
    @Query("SELECT * FROM todo_box WHERE id = :boxId")
    suspend fun getBoxesByID(boxId: Long): List<ToDoBox>

    @Query("SELECT * FROM todo_box WHERE selectDateAt >= :startDate AND selectDateAt < :endDate")
    suspend fun getBoxesByDate(startDate: LocalDateTime, endDate: LocalDateTime): List<ToDoBox>

    @Query("SELECT * FROM todo_data WHERE todo_box_id = :boxId AND selectDateAt >= :startDate AND selectDateAt < :endDate AND status != 'DELETED'")
    suspend fun getTodosByBoxDate(boxId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<ToDoData>

    // 4-22 for todosDone
    // ToDoDao 中增加获取已完成任务数量的方法
    @Query("SELECT COUNT(*) FROM todo_data WHERE isChecked = 1")
    fun getTodoCount(): Int

    @Query("SELECT * FROM todo_data WHERE selectDateAt >= :startDate AND selectDateAt < :endDate")
    suspend fun getTodosByDate(startDate: LocalDateTime, endDate: LocalDateTime): List<ToDoData>

    // 4-29 新增对box的标题筛选
    @Query("SELECT * FROM todo_box WHERE color = :color")
    suspend fun getBoxesByColor(color: String): List<ToDoBox>

    @Query("SELECT * FROM todo_box WHERE id = :boxId AND selectDateAt >= :startDate AND selectDateAt < :endDate")
    fun getBoxByIdAndDate(boxId: Long, startDate: LocalDateTime, endDate: LocalDateTime): ToDoBox
}

