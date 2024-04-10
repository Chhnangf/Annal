package com.example.navhost.android.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 *  优先级：高、中、低
 */
enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}


/**
 *  待办数据模型
 *  使用Romm数据库存储数据
 *  使用Parcelize接口将class对象序列化和反序列化
 *  序列化可以让数据持久化、转化成字节流、在网络中传输
 *
 *  ID、标题、优先级、详情
 */


@Entity(tableName = "todo_data",
    foreignKeys = [
        ForeignKey(
            entity = ToDoBox::class,
            parentColumns = ["id"],
            childColumns = ["todo_box_id"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class ToDoData(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    var title: String,
    var priority: Priority,
    var description: String,
    var isChecked: Boolean = false,
    @ColumnInfo(index = true)
    val todo_box_id: Long? = null,
)
