package com.chhangf.annal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chhangf.annal.data.model.ToDoBox
import com.chhangf.annal.data.model.ToDoData


@Database(entities = [ToDoData::class, ToDoBox::class], version = 2, exportSchema = false)
@TypeConverters(Converter::class)
abstract class ToDoDatabase: RoomDatabase() {

    abstract fun toDoDao(): ToDoDao

    companion object {
        @Volatile
        private var INSTANCE: ToDoDatabase? = null

        val MIGRATION_1_TO_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
            ALTER TABLE todo_box
            ADD COLUMN last_modified_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        """.trimIndent())
            }
        }

        fun getDatabase(context: Context): ToDoDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoDatabase::class.java,
                    "todo_database"
                )
                    .addMigrations(MIGRATION_1_TO_2) // 添加迁移逻辑
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

}
