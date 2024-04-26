package com.example.navhost.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "todo_box")
data class ToDoBox(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String,

    // 4-15新增日期字段
    val createdAt: LocalDateTime = LocalDateTime.now(), // 新增字段：创建时间，默认为当前时间
    var selectDateAt: LocalDateTime = LocalDateTime.now(),
    val lastModifiedAt: LocalDateTime = LocalDateTime.now(), // 新增字段：最后修改时间，默认为当前时间

)

data class ToDoBoxWithTodos(
    val box: ToDoBox,
    val todos: List<ToDoData>,
    val doneCount: Int,
)