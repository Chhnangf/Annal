package com.example.navhost.android.data

import androidx.room.TypeConverter
import com.example.navhost.android.data.model.Priority

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
        return priority.name
    }

    /**
     *  toPriority(priority: String): Priority
     *  它接收一个字符串类型的参数，并将其转换回 Priority 枚举类型。
     *  当 Room 从数据库中读取数据并需要还原成 Priority 类型时，会调用这个方法。
     */
    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }

}

