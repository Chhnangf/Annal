import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TODO_TABLE = """
    CREATE TABLE IF NOT EXISTS todo_table (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        description TEXT,
        priority INTEGER,
        due_date TEXT,
        completed BOOLEAN DEFAULT 0
    );
"""
        db.execSQL(CREATE_TODO_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 在此处实现数据库升级逻辑
    }

    fun checkDatabaseAccess() {
        try {
            writableDatabase.use { db ->
                // 执行一个简单的查询，比如检查表是否存在
                val query = "SELECT COUNT(*) FROM todo_table"
                val cursor = db.rawQuery(query, null)
                if (cursor != null) {
                    cursor.moveToFirst()
                    // 如果查询结果大于0，则表明至少有一条记录，说明数据库可以访问
                    if (cursor.getInt(0) > 0) {
                        Log.d("DatabaseHelper", "Database is accessible and has data.")
                    } else {
                        Log.d("DatabaseHelper", "Database is accessible but has no data.")
                    }
                    cursor.close()
                } else {
                    Log.e("DatabaseHelper", "Failed to execute query, database might not be accessible.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DatabaseHelper", "Error while accessing the database.", e)
        }
    }

    companion object {
        // 数据库名称和版本
        private const val DATABASE_NAME = "todo_database.db"
        private const val DATABASE_VERSION = 1
    }
}