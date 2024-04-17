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
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    DELETED("已删除");

    override fun toString(): String {
        return displayText
    }

    companion object {
        fun fromSubTasksDone(numSubTasks: Int, numSubTasksDone: Int): Status {
            return when {
                numSubTasks == numSubTasksDone -> COMPLETED
                numSubTasksDone > 0 && numSubTasksDone < numSubTasks -> IN_PROGRESS
                else -> PENDING
            }
        }
    }
}


/**
 *  活跃：无/低/中/高，共4种
 */
enum class Active(val displayIconResId: Int) {
    NONE(coil.base.R.drawable.abc_vector_test),
    LOW(coil.base.R.drawable.abc_vector_test),
    MEDIUM(coil.base.R.drawable.abc_vector_test),
    HIGH(coil.base.R.drawable.abc_vector_test);
}

/**
 *  待办数据模型
 *  使用Romm数据库存储数据
 *  使用Parcelize接口将class对象序列化和反序列化
 *  序列化可以让数据持久化、转化成字节流、在网络中传输
 *
 *  ID、标题、优先级、详情
 */


@Entity(
    tableName = "todo_data",
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

    // 基础信息
    val id: Long? = null,
    var title: String,
    var priority: Priority,
    var description: String?,

    // 4-15 日期
    var reminderTime: LocalTime? = null, // 新增字段：提醒时间
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var selectDateAt: LocalDateTime = LocalDateTime.now(),
    var lastModifiedAt: LocalDateTime = LocalDateTime.now(),

    // 4-19 待办
    var isChecked: Boolean = false,
    var status: Status = Status.PENDING,
    var subTaskCount: Int = 0,
    var subTasks: List<SubTask> = emptyList(), // 替换原有的布尔类型字段，用于存储包含描述和勾选状态的子任务列表
    var subTasksDoneCount: Int = 0,

    @ColumnInfo(index = true)
    val todo_box_id: Long? = null,
)

data class SubTask(val index: Int, val description: String, var isChecked: Boolean)
