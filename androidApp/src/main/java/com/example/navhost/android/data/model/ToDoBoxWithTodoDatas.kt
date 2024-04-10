package com.example.navhost.android.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ToDoBoxWithTodoDatas(
    @Embedded
    val toDoBox: ToDoBox,
    @Relation(
        parentColumn = "id",
        entityColumn = "todo_box_id"
    )
    val todoDatas: List<ToDoData>
)