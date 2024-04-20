package com.example.navhost.android.data

import androidx.room.TypeConverter
import com.example.navhost.android.data.model.Priority
import com.example.navhost.android.data.model.Status
import com.example.navhost.android.data.model.SubTask
import com.google.gson.Gson
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 *  这段代码定义了一个名为 Converter 的类，其中包含两个方法，
 *  这两个方法是用来进行类型转换的，并使用了 Room 数据库库的 @TypeConverter 注解。
 *
 *  在 Room 数据库中，SQLite 本身并不支持自定义类型（如枚举类型 Priority）
 *  但是我们可以通过 @TypeConverter 注解来定义自定义类型的转换规则，
 *  将其转化为 SQLite 支持的基础类型（如字符串 String）进行存储。
 *
 *  通过这样的转换，Room 就能够在 SQLite 数据库中透明地存储和读取 Priority 枚举类型的数据了。
 */

class Converter {

    /**
     *  fromPriority(priority: Priority): String
     *  接收一个 Priority 枚举类型的参数，并将其名称转换为字符串类型。
     *  Room 需要把 Priority 类型的字段保存到数据库时，会调用这个方法。
     */
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.displayText
    }

    /**
     *  toPriority(priority: String): Priority
     *  它接收一个字符串类型的参数，并将其转换回 Priority 枚举类型。
     *  当 Room 从数据库中读取数据并需要还原成 Priority 类型时，会调用这个方法。
     */
    @TypeConverter
    fun toPriority(priorityText: String): Priority {
        return when (priorityText) {
            Priority.HIGH.displayText -> Priority.HIGH
            Priority.MEDIUM.displayText -> Priority.MEDIUM
            Priority.LOW.displayText -> Priority.LOW
            else -> throw IllegalArgumentException("Invalid priority text: $priorityText")
        }
    }

    @TypeConverter
    fun fromTodoStatus(status: Status): String {
        return status.displayText
    }

    @TypeConverter
    fun toTodoStatus(statusText: String): Status {
        return when (statusText) {
            Status.PENDING.displayText -> Status.PENDING
            Status.COMPLETED.displayText -> Status.COMPLETED
            Status.IN_PROGRESS.displayText -> Status.IN_PROGRESS
            Status.DELETED.displayText -> Status.DELETED
            else -> throw IllegalArgumentException("Invalid todo status text: $statusText")
        }
    }


    /**
     *  对LocalDateTime序列化和反序列化
     */
    @TypeConverter
    fun fromLocalTime(localTime: LocalTime?): String? {
        return localTime?.format(DateTimeFormatter.ISO_TIME)
    }

    @TypeConverter
    fun toLocalTime(databaseString: String?): LocalTime? {
        return databaseString?.let { LocalTime.parse(it, DateTimeFormatter.ISO_TIME) }
    }

    /**
     *  4-15 对数据库日期LocalDateTime格式的转换
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(localDateTime: LocalDateTime?): Long? {
        return localDateTime?.toInstant(ZoneOffset.UTC)?.epochSecond
    }

    // 4-20 for subTask
    @TypeConverter
    fun fromSubTasksList(subTasks: List<SubTask>): String {
        return Gson().toJson(subTasks)
    }

    @TypeConverter
    fun toSubTasksList(json: String): List<SubTask> {
        return Gson().fromJson(json, Array<SubTask>::class.java).toList()
    }
}

