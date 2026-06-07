package com.app.tracker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.tracker.data.repository.TaskRepository
import com.app.tracker.model.Task
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _allTasks = MutableLiveData<List<Task>>()
    val allTasks: LiveData<List<Task>> = _allTasks

    private val _selectedCategory = MutableLiveData<String>("All")
    val selectedCategory: LiveData<String> = _selectedCategory

    private val _selectedPriority = MutableLiveData<String>("All")
    val selectedPriority: LiveData<String> = _selectedPriority

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _filteredTasks = MediatorLiveData<List<Task>>()
    val filteredTasks: LiveData<List<Task>> = _filteredTasks

    init {
        _filteredTasks.addSource(_allTasks) { tasks ->
            combineTasksFilterAndSearch(tasks, _selectedCategory.value, _selectedPriority.value, _searchQuery.value)
        }
        _filteredTasks.addSource(_selectedCategory) { category ->
            combineTasksFilterAndSearch(_allTasks.value, category, _selectedPriority.value, _searchQuery.value)
        }
        _filteredTasks.addSource(_selectedPriority) { priority ->
            combineTasksFilterAndSearch(_allTasks.value, _selectedCategory.value, priority, _searchQuery.value)
        }
        _filteredTasks.addSource(_searchQuery) { query ->
            combineTasksFilterAndSearch(_allTasks.value, _selectedCategory.value, _selectedPriority.value, query)
        }
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            repository.getAllTasks().collect { tasks ->
                _allTasks.postValue(tasks)
            }
        }
    }

    fun updateFilter(category: String) {
        _selectedCategory.value = category
    }

    fun updatePriorityFilter(priority: String) {
        _selectedPriority.value = priority
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun combineTasksFilterAndSearch(
        tasks: List<Task>?,
        category: String?,
        priority: String?,
        query: String?
    ) {
        if (tasks == null) {
            _filteredTasks.value = emptyList()
            return
        }
        val catFilter = category ?: "All"
        val priFilter = priority ?: "All"
        val search = query ?: ""

        var result = tasks

        // 1. Filter by category
        if (!catFilter.equals("All", ignoreCase = true)) {
            result = result.filter { it.category.equals(catFilter, ignoreCase = true) }
        }

        // 2. Filter by priority
        if (!priFilter.equals("All", ignoreCase = true)) {
            result = result.filter { it.priority.equals(priFilter, ignoreCase = true) }
        }

        // 3. Search filter loop
        if (search.isNotEmpty()) {
            result = result.filter {
                it.title.contains(search, ignoreCase = true) ||
                it.description.contains(search, ignoreCase = true)
            }
        }

        _filteredTasks.value = result
    }

    // --- Mass Updates & Loops ---

    fun bulkDeleteTasks(tasks: List<Task>) {
        viewModelScope.launch {
            for (task in tasks) {
                repository.deleteTask(task)
            }
        }
    }

    fun bulkToggleComplete(tasks: List<Task>) {
        viewModelScope.launch {
            for (task in tasks) {
                repository.toggleTaskCompleted(task)
            }
        }
    }

    fun bulkUpdatePriority(tasks: List<Task>, priority: String) {
        viewModelScope.launch {
            for (task in tasks) {
                val updated = task.copy(priority = priority)
                repository.updateTask(updated)
            }
        }
    }

    fun insertTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun getTaskById(id: Int, callback: (Task?) -> Unit) {
        viewModelScope.launch {
            val task = repository.getTaskById(id)
            callback(task)
        }
    }

    class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
