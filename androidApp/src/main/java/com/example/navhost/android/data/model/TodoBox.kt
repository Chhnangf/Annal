package com.example.navhost.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_box")
data class ToDoBox(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String
)

