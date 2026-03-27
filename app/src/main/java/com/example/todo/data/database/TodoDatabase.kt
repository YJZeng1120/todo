package com.example.todo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todo.data.dao.TodoDao
import com.example.todo.data.model.Priority
import com.example.todo.data.model.TodoItem

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)
}

@Database(entities = [TodoItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        fun getInstance(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                )
                    .addCallback(SeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private class SeedCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()
                val day = 24 * 60 * 60 * 1000L

                // title, description, isCompleted (0/1), priority, dueDate (null or epoch), createdAt
                val rows = listOf(
                    listOf("回覆工作信件",   "主管寄的 Q3 進度確認信",  0, "HIGH",   now - day,       now - day * 5),
                    listOf("完成期末報告",   "軟工課程，需附上 UML 圖", 0, "HIGH",   now + day,       now - day * 3),
                    listOf("準備技術面試",   "複習 DS、演算法、系設",   0, "HIGH",   now + day * 3,   now - day * 2),
                    listOf("繳網路費",       "",                         0, "MEDIUM", now + day * 2,   now - day * 4),
                    listOf("健身 30 分鐘",   "跑步 + 核心訓練",         1, "MEDIUM", null,            now - day * 6),
                    listOf("整理桌面",       "",                         1, "LOW",    null,            now - day * 7),
                    listOf("看書 20 頁",     "《Clean Code》第三章",    0, "LOW",    now + day * 7,   now - day),
                    listOf("買菜",           "牛奶、雞蛋、青菜",         0, "LOW",    null,            now),
                )

                rows.forEach { r ->
                    val dueDate = if (r[4] == null) "NULL" else r[4]
                    db.execSQL(
                        """INSERT INTO todos (title, description, isCompleted, priority, dueDate, createdAt)
                           VALUES ('${r[0]}', '${r[1]}', ${r[2]}, '${r[3]}', $dueDate, ${r[5]})"""
                    )
                }
            }
        }
    }
}
