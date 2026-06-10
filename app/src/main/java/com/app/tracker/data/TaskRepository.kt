package com.app.tracker.data

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): List<Task> = taskDao.getAllTasks()

    fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    fun update(task: Task) {
        taskDao.updateTask(task)
    }

    fun delete(task: Task) {
        taskDao.deleteTask(task)
    }
}