package com.app.tracker.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.tracker.data.local.dao.TaskDao
import com.app.tracker.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_planner_db"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.taskDao())
                    }
                }
            }

            suspend fun populateDatabase(taskDao: TaskDao) {
                taskDao.insertTask(
                    TaskEntity(
                        title = "Design Project Mockups",
                        description = "Create modern UI/UX wireframes for the tracker application using Figma, focusing on layout and color harmony.",
                        category = "Work",
                        priority = "High",
                        isCompleted = false,
                        dueDate = "2026-06-10"
                    )
                )
                taskDao.insertTask(
                    TaskEntity(
                        title = "Buy Weekly Groceries",
                        description = "Pick up fresh vegetables, fruits, almond milk, whole wheat bread, and eggs from the local farmers market.",
                        category = "Personal",
                        priority = "Medium",
                        isCompleted = true,
                        dueDate = "2026-06-06"
                    )
                )
                taskDao.insertTask(
                    TaskEntity(
                        title = "Morning Cardio Workout",
                        description = "45 minutes outdoor running session at the park followed by stretching and core training.",
                        category = "Health",
                        priority = "High",
                        isCompleted = false,
                        dueDate = "2026-06-07"
                    )
                )
                taskDao.insertTask(
                    TaskEntity(
                        title = "Kotlin Design Patterns",
                        description = "Read chapter 4 and 5 of Kotlin concurrency and design patterns book.",
                        category = "Study",
                        priority = "Low",
                        isCompleted = true,
                        dueDate = "2026-06-08"
                    )
                )
                taskDao.insertTask(
                    TaskEntity(
                        title = "Team Sync-Up Meeting",
                        description = "Discuss sprint progress, resolve current blockers, and plan the release schedule with engineering team.",
                        category = "Work",
                        priority = "Medium",
                        isCompleted = false,
                        dueDate = "2026-06-09"
                    )
                )
            }
        }
    }
}
