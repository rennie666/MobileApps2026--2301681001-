package com.app.tracker.di

import android.content.Context
import com.app.tracker.data.local.db.AppDatabase
import com.app.tracker.data.repository.TaskRepository
import com.app.tracker.data.repository.TaskRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

object ServiceLocator {
    private val scope = CoroutineScope(SupervisorJob())
    private var database: AppDatabase? = null
    @Volatile
    var taskRepository: TaskRepository? = null

    fun provideTaskRepository(context: Context): TaskRepository {
        synchronized(this) {
            return taskRepository ?: createRepository(context)
        }
    }

    private fun createRepository(context: Context): TaskRepository {
        val db = database ?: AppDatabase.getInstance(context.applicationContext, scope)
        database = db
        val newRepo = TaskRepositoryImpl(db.taskDao())
        taskRepository = newRepo
        return newRepo
    }
}
