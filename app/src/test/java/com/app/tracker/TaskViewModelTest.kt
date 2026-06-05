package com.app.tracker

import com.app.tracker.data.repository.TaskRepository
import com.app.tracker.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeTaskRepository : TaskRepository {
    private val tasksList = mutableListOf<Task>()
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    override fun getAllTasks(): Flow<List<Task>> = tasksFlow

    override suspend fun getTaskById(id: Int): Task? {
        return tasksList.find { it.id == id }
    }

    override suspend fun insertTask(task: Task): Long {
        val newTask = if (task.id == 0) {
            task.copy(id = tasksList.size + 1)
        } else {
            task
        }
        tasksList.add(newTask)
        tasksFlow.value = tasksList.toList()
        return newTask.id.toLong()
    }

    override suspend fun updateTask(task: Task) {
        val index = tasksList.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasksList[index] = task
            tasksFlow.value = tasksList.toList()
        }
    }

    override suspend fun deleteTask(task: Task) {
        tasksList.removeIf { it.id == task.id }
        tasksFlow.value = tasksList.toList()
    }

    override suspend fun toggleTaskCompleted(task: Task) {
        val index = tasksList.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasksList[index] = task.copy(isCompleted = !task.isCompleted)
            tasksFlow.value = tasksList.toList()
        }
    }
}

class TaskRepositoryUnitTest {

    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        repository = FakeTaskRepository()
    }

    @Test
    fun insertTask_addsTaskToRepository() = runBlocking {
        val task = Task(
            id = 0,
            title = "Test Task",
            description = "Test Description",
            category = "Work",
            priority = "High",
            isCompleted = false,
            dueDate = "2026-06-12"
        )

        repository.insertTask(task)

        val allTasks = repository.getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Test Task", allTasks[0].title)
    }

    @Test
    fun deleteTask_removesTaskFromRepository() = runBlocking {
        val task = Task(
            id = 1,
            title = "Task to delete",
            description = "Details",
            category = "Personal",
            priority = "Low",
            isCompleted = false,
            dueDate = "2026-06-12"
        )
        repository.insertTask(task)

        repository.deleteTask(task)

        val allTasks = repository.getAllTasks().first()
        assertTrue(allTasks.isEmpty())
    }

    @Test
    fun toggleTaskCompleted_updatesCompletionStatus() = runBlocking {
        val task = Task(
            id = 1,
            title = "Uncompleted Task",
            description = "Details",
            category = "Health",
            priority = "Medium",
            isCompleted = false,
            dueDate = "2026-06-12"
        )
        repository.insertTask(task)

        repository.toggleTaskCompleted(task)

        val updatedTask = repository.getTaskById(1)
        assertNotNull(updatedTask)
        assertTrue(updatedTask!!.isCompleted)
    }
}
