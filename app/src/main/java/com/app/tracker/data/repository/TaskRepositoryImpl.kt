package com.app.tracker.data.repository

import com.app.tracker.data.local.dao.TaskDao
import com.app.tracker.data.local.entity.TaskEntity
import com.app.tracker.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(private val taskDao: TaskDao) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)?.toDomain()
    }

    override suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }

    override suspend fun toggleTaskCompleted(task: Task) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(updated.toEntity())
    }

    private fun TaskEntity.toDomain(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            category = category,
            priority = priority,
            isCompleted = isCompleted,
            dueDate = dueDate
        )
    }

    private fun Task.toEntity(): TaskEntity {
        return TaskEntity(
            id = id,
            title = title,
            description = description,
            category = category,
            priority = priority,
            isCompleted = isCompleted,
            dueDate = dueDate
        )
    }
}
