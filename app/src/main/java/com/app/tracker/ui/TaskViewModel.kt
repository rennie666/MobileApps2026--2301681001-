package com.app.tracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.tracker.data.Task
import com.app.tracker.data.TaskDatabase
import com.app.tracker.data.TaskRepository
import kotlin.concurrent.thread

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val _allTasks = MutableLiveData<List<Task>>()
    val allTasks: LiveData<List<Task>> = _allTasks

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        refreshTasks()
    }

    fun refreshTasks() {
        // Използваме фонова нишка (thread), за да не блокираме екрана и да няма краш
        thread {
            val tasks = repository.getAllTasks()
            _allTasks.postValue(tasks) // postValue прехвърля безопасно данните към UI
        }
    }

    fun insertTask(task: Task) {
        thread {
            repository.insert(task)
            refreshTasks()
        }
    }
}