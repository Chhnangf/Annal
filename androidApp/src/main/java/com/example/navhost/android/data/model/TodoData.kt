package com.example.navhost.android.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.LocalTime

/**
 *  优先级：高、中、低
 */
enum class Priority(val displayText: String) {
    HIGH("高"),
    MEDIUM("中"),
    LOW("低");
    override fun toString(): String {
        return displayText
    }
}

/**
 *  状态：待完成/已完成/删除
 */
enum class Status(val displayText: String) {
    PENDING("待完成"),
    COMPLETED("已完成"),
    DELETED("已删除");
    override fun toString(): String {
        return displayText
    }

    // 使用：
//    val status = TodoStatus.COMPLETED
//    println(status.toString())  // 输出：“已完成”
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
    var description: String?,
    var isChecked: Boolean = false,
    var reminderTime: LocalTime? = null, // 新增字段：提醒时间
    var status: Status = Status.PENDING,
    // 4-15 新增日期字段
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var selectDateAt: LocalDateTime = LocalDateTime.now(),
    var lastModifiedAt: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(index = true)
    val todo_box_id: Long? = null,
)
