package com.app.tracker

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.Observer
import com.app.tracker.data.local.dao.TaskDao
import com.app.tracker.data.local.entity.TaskEntity
import com.app.tracker.data.repository.TaskRepository
import com.app.tracker.data.repository.TaskRepositoryImpl
import com.app.tracker.model.Task
import com.app.tracker.ui.viewmodel.TaskViewModel
import com.app.tracker.di.ServiceLocator
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

// --- JUnit 5 Extensions for Android Testing ---

class InstantTaskExecutorExtension : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })
    }

    override fun afterEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutinesTestExtension(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
    }
}

// --- Test Suite ---

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestExtension::class)
class TaskPlannerJUnit5MockedTest {

    // --- TaskRepositoryImpl Tests ---

    @Test
    fun repository_getAllTasks_delegatesToDaoAndMapsToDomain() = runTest {
        val mockDao: TaskDao = mock()
        val entities = listOf(
            TaskEntity(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12"),
            TaskEntity(2, "Task 2", "Desc 2", "Personal", "Low", true, "2026-06-12")
        )
        whenever(mockDao.getAllTasks()).thenReturn(flowOf(entities))

        val repository = TaskRepositoryImpl(mockDao)
        var resultList: List<Task>? = null
        repository.getAllTasks().collect {
            resultList = it
        }

        assertNotNull(resultList)
        assertEquals(2, resultList!!.size)
        assertEquals("Task 1", resultList!![0].title)
        assertEquals("Work", resultList!![0].category)
        assertEquals("High", resultList!![0].priority)
        assertEquals(false, resultList!![0].isCompleted)

        assertEquals("Task 2", resultList!![1].title)
        assertEquals("Personal", resultList!![1].category)
        assertEquals("Low", resultList!![1].priority)
        assertEquals(true, resultList!![1].isCompleted)
    }

    @Test
    fun repository_getTaskById_delegatesToDaoAndMapsToDomain() = runTest {
        val mockDao: TaskDao = mock()
        val entity = TaskEntity(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12")
        whenever(mockDao.getTaskById(1)).thenReturn(entity)
        whenever(mockDao.getTaskById(2)).thenReturn(null)

        val repository = TaskRepositoryImpl(mockDao)

        val task1 = repository.getTaskById(1)
        assertNotNull(task1)
        assertEquals("Task 1", task1!!.title)

        val task2 = repository.getTaskById(2)
        assertNull(task2)
    }

    @Test
    fun repository_insertTask_delegatesToDao() = runTest {
        val mockDao: TaskDao = mock()
        val task = Task(0, "New Task", "Desc", "Work", "High", false, "2026-06-12")
        whenever(mockDao.insertTask(any())).thenReturn(10L)

        val repository = TaskRepositoryImpl(mockDao)
        val id = repository.insertTask(task)

        assertEquals(10L, id)
        
        val captor = argumentCaptor<TaskEntity>()
        verify(mockDao).insertTask(captor.capture())
        assertEquals("New Task", captor.firstValue.title)
    }

    @Test
    fun repository_updateTask_delegatesToDao() = runTest {
        val mockDao: TaskDao = mock()
        val task = Task(5, "Updated Task", "Desc", "Work", "High", false, "2026-06-12")

        val repository = TaskRepositoryImpl(mockDao)
        repository.updateTask(task)

        val captor = argumentCaptor<TaskEntity>()
        verify(mockDao).updateTask(captor.capture())
        assertEquals(5, captor.firstValue.id)
        assertEquals("Updated Task", captor.firstValue.title)
    }

    @Test
    fun repository_deleteTask_delegatesToDao() = runTest {
        val mockDao: TaskDao = mock()
        val task = Task(5, "Delete Task", "Desc", "Work", "High", false, "2026-06-12")

        val repository = TaskRepositoryImpl(mockDao)
        repository.deleteTask(task)

        val captor = argumentCaptor<TaskEntity>()
        verify(mockDao).deleteTask(captor.capture())
        assertEquals(5, captor.firstValue.id)
    }

    @Test
    fun repository_toggleTaskCompleted_invertsCompletionStatusAndSaves() = runTest {
        val mockDao: TaskDao = mock()
        val task = Task(5, "Toggle Task", "Desc", "Work", "High", false, "2026-06-12")

        val repository = TaskRepositoryImpl(mockDao)
        repository.toggleTaskCompleted(task)

        val captor = argumentCaptor<TaskEntity>()
        verify(mockDao).updateTask(captor.capture())
        assertEquals(5, captor.firstValue.id)
        assertEquals(true, captor.firstValue.isCompleted) // Inverted from false
    }

    // --- TaskViewModel Tests ---

    @Test
    fun viewModel_initialization_collectsAndPostsAllTasks() = runTest {
        val mockRepo: TaskRepository = mock()
        val tasks = listOf(
            Task(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12"),
            Task(2, "Task 2", "Desc 2", "Personal", "Low", true, "2026-06-12")
        )
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(tasks))

        val viewModel = TaskViewModel(mockRepo)

        val observer: Observer<List<Task>> = mock()
        viewModel.allTasks.observeForever(observer)

        verify(observer).onChanged(tasks)
        assertEquals(tasks, viewModel.allTasks.value)
        viewModel.allTasks.removeObserver(observer)
    }

    @Test
    fun viewModel_insertTask_delegatesToRepository() = runTest {
        val mockRepo: TaskRepository = mock()
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(emptyList()))

        val viewModel = TaskViewModel(mockRepo)
        val task = Task(0, "New Task", "Desc", "Work", "High", false, "2026-06-12")
        viewModel.insertTask(task)

        verify(mockRepo).insertTask(task)
    }

    @Test
    fun viewModel_updateTask_delegatesToRepository() = runTest {
        val mockRepo: TaskRepository = mock()
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(emptyList()))

        val viewModel = TaskViewModel(mockRepo)
        val task = Task(1, "Updated Task", "Desc", "Work", "High", false, "2026-06-12")
        viewModel.updateTask(task)

        verify(mockRepo).updateTask(task)
    }

    @Test
    fun viewModel_deleteTask_delegatesToRepository() = runTest {
        val mockRepo: TaskRepository = mock()
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(emptyList()))

        val viewModel = TaskViewModel(mockRepo)
        val task = Task(1, "Delete Task", "Desc", "Work", "High", false, "2026-06-12")
        viewModel.deleteTask(task)

        verify(mockRepo).deleteTask(task)
    }

    @Test
    fun viewModel_toggleTaskCompleted_delegatesToRepository() = runTest {
        val mockRepo: TaskRepository = mock()
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(emptyList()))

        val viewModel = TaskViewModel(mockRepo)
        val task = Task(1, "Toggle Task", "Desc", "Work", "High", false, "2026-06-12")
        viewModel.toggleTaskCompleted(task)

        verify(mockRepo).toggleTaskCompleted(task)
    }

    @Test
    fun viewModel_getTaskById_callsCallbackWithResult() = runTest {
        val mockRepo: TaskRepository = mock()
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(emptyList()))
        val task = Task(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12")
        whenever(mockRepo.getTaskById(1)).thenReturn(task)

        val viewModel = TaskViewModel(mockRepo)

        var callbackResult: Task? = null
        viewModel.getTaskById(1) {
            callbackResult = it
        }

        assertEquals(task, callbackResult)
    }

    @Test
    fun viewModel_loadTasks_recollectsFromRepository() = runTest {
        val mockRepo: TaskRepository = mock()
        val tasks1 = listOf(Task(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12"))
        val tasks2 = listOf(
            Task(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12"),
            Task(2, "Task 2", "Desc 2", "Personal", "Low", true, "2026-06-12")
        )
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(tasks1), flowOf(tasks2))

        val viewModel = TaskViewModel(mockRepo) // triggers first collection in init

        assertEquals(tasks1, viewModel.allTasks.value)

        viewModel.loadTasks() // triggers second collection

        assertEquals(tasks2, viewModel.allTasks.value)
    }

    @Test
    fun viewModel_updateFilter_filtersTasksCorrectly() = runTest {
        val mockRepo: TaskRepository = mock()
        val tasks = listOf(
            Task(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12"),
            Task(2, "Task 2", "Desc 2", "Personal", "Low", true, "2026-06-12")
        )
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(tasks))

        val viewModel = TaskViewModel(mockRepo)

        // MediatorLiveData requires an active observer to propagate changes from its sources
        val observer: Observer<List<Task>> = mock()
        viewModel.filteredTasks.observeForever(observer)

        // Default filter should be "All", meaning all tasks are returned
        assertEquals(tasks, viewModel.filteredTasks.value)

        // Update filter to "Work"
        viewModel.updateFilter("Work")
        assertEquals(1, viewModel.filteredTasks.value?.size)
        assertEquals("Task 1", viewModel.filteredTasks.value?.get(0)?.title)

        // Update filter to "Personal"
        viewModel.updateFilter("Personal")
        assertEquals(1, viewModel.filteredTasks.value?.size)
        assertEquals("Task 2", viewModel.filteredTasks.value?.get(0)?.title)

        viewModel.filteredTasks.removeObserver(observer)
    }

    @Test
    fun viewModel_searchAndPriorityFilters_worksCorrectly() = runTest {
        val mockRepo: TaskRepository = mock()
        val tasks = listOf(
            Task(1, "Fix Layout", "Repair UI views", "Work", "High", false, "2026-06-12"),
            Task(2, "Buy Groceries", "Get bread and milk", "Personal", "Low", true, "2026-06-12"),
            Task(3, "Read Book", "Read chapter 5", "Personal", "High", false, "2026-06-12")
        )
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(tasks))

        val viewModel = TaskViewModel(mockRepo)

        val observer: Observer<List<Task>> = mock()
        viewModel.filteredTasks.observeForever(observer)

        // 1. Filter by category
        viewModel.updateFilter("Personal")
        assertEquals(2, viewModel.filteredTasks.value?.size)

        // 2. Filter by category AND priority
        viewModel.updatePriorityFilter("High")
        assertEquals(1, viewModel.filteredTasks.value?.size)
        assertEquals("Read Book", viewModel.filteredTasks.value?.get(0)?.title)

        // 3. Search query filter
        viewModel.updateFilter("All")
        viewModel.updatePriorityFilter("All")
        viewModel.updateSearchQuery("grocer")
        assertEquals(1, viewModel.filteredTasks.value?.size)
        assertEquals("Buy Groceries", viewModel.filteredTasks.value?.get(0)?.title)

        // 4. Search description filter
        viewModel.updateSearchQuery("views")
        assertEquals(1, viewModel.filteredTasks.value?.size)
        assertEquals("Fix Layout", viewModel.filteredTasks.value?.get(0)?.title)

        viewModel.filteredTasks.removeObserver(observer)
    }

    @Test
    fun viewModel_bulkOperations_loopsAndDelegatesToRepository() = runTest {
        val mockRepo: TaskRepository = mock()
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(emptyList()))

        val viewModel = TaskViewModel(mockRepo)
        val tasks = listOf(
            Task(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12"),
            Task(2, "Task 2", "Desc 2", "Personal", "Low", true, "2026-06-12")
        )

        // Bulk Delete
        viewModel.bulkDeleteTasks(tasks)
        verify(mockRepo).deleteTask(tasks[0])
        verify(mockRepo).deleteTask(tasks[1])

        // Bulk Toggle Complete
        viewModel.bulkToggleComplete(tasks)
        verify(mockRepo).toggleTaskCompleted(tasks[0])
        verify(mockRepo).toggleTaskCompleted(tasks[1])

        // Bulk Update Priority
        viewModel.bulkUpdatePriority(tasks, "Medium")
        verify(mockRepo).updateTask(tasks[0].copy(priority = "Medium"))
        verify(mockRepo).updateTask(tasks[1].copy(priority = "Medium"))
    }

    // --- TaskEntity Tests ---

    @Test
    fun taskEntity_propertiesAreCorrect() {
        val entity = TaskEntity(
            id = 1,
            title = "Title",
            description = "Desc",
            category = "Work",
            priority = "High",
            isCompleted = false,
            dueDate = "2026-06-12"
        )
        assertEquals(1, entity.id)
        assertEquals("Title", entity.title)
        assertEquals("Desc", entity.description)
        assertEquals("Work", entity.category)
        assertEquals("High", entity.priority)
        assertEquals(false, entity.isCompleted)
        assertEquals("2026-06-12", entity.dueDate)
    }

    // --- ServiceLocator Tests ---

    @Test
    fun serviceLocator_providesTaskRepository() {
        val mockRepo: TaskRepository = mock()
        ServiceLocator.taskRepository = mockRepo

        val context: Context = mock()
        val resolvedRepo = ServiceLocator.provideTaskRepository(context)

        assertEquals(mockRepo, resolvedRepo)
        ServiceLocator.taskRepository = null
    }

    // --- TaskViewModelFactory Tests ---

    @Test
    fun viewModelFactory_createsViewModelSuccessfully() {
        val mockRepo: TaskRepository = mock()
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(emptyList()))
        val factory = TaskViewModel.TaskViewModelFactory(mockRepo)

        val viewModel = factory.create(TaskViewModel::class.java)
        assertNotNull(viewModel)

        class InvalidViewModel : androidx.lifecycle.ViewModel()
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            factory.create(InvalidViewModel::class.java)
        }
    }

    // --- Unhappy Search & Filter Paths ---

    @Test
    fun viewModel_filter_nullTaskList_returnsEmptyList() = runTest {
        val mockRepo: TaskRepository = mock()
        val neverEmittingFlow = kotlinx.coroutines.flow.emptyFlow<List<Task>>()
        whenever(mockRepo.getAllTasks()).thenReturn(neverEmittingFlow)

        val viewModel = TaskViewModel(mockRepo)
        val observer: Observer<List<Task>> = mock()
        viewModel.filteredTasks.observeForever(observer)

        viewModel.updateFilter("Work")

        assertEquals(emptyList<Task>(), viewModel.filteredTasks.value)
        viewModel.filteredTasks.removeObserver(observer)
    }

    @Test
    fun viewModel_search_noMatches_returnsEmptyList() = runTest {
        val mockRepo: TaskRepository = mock()
        val tasks = listOf(
            Task(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12")
        )
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(tasks))

        val viewModel = TaskViewModel(mockRepo)
        val observer: Observer<List<Task>> = mock()
        viewModel.filteredTasks.observeForever(observer)

        viewModel.updateSearchQuery("Matches Nothing")
        assertEquals(emptyList<Task>(), viewModel.filteredTasks.value)

        viewModel.filteredTasks.removeObserver(observer)
    }

    @Test
    fun viewModel_filter_invalidCategoryOrPriority_returnsEmptyList() = runTest {
        val mockRepo: TaskRepository = mock()
        val tasks = listOf(
            Task(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12")
        )
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(tasks))

        val viewModel = TaskViewModel(mockRepo)
        val observer: Observer<List<Task>> = mock()
        viewModel.filteredTasks.observeForever(observer)

        // Invalid Category
        viewModel.updateFilter("InvalidCategory")
        assertEquals(emptyList<Task>(), viewModel.filteredTasks.value)

        // Reset and check Invalid Priority
        viewModel.updateFilter("All")
        viewModel.updatePriorityFilter("InvalidPriority")
        assertEquals(emptyList<Task>(), viewModel.filteredTasks.value)

        viewModel.filteredTasks.removeObserver(observer)
    }

    @Test
    fun viewModel_search_whitespaceQuery_matchesLiteralSpaces() = runTest {
        val mockRepo: TaskRepository = mock()
        val tasks = listOf(
            Task(1, "Task 1", "Desc 1", "Work", "High", false, "2026-06-12"),
            Task(2, "Task  2", "Desc  2", "Personal", "Low", true, "2026-06-12")
        )
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(tasks))

        val viewModel = TaskViewModel(mockRepo)
        val observer: Observer<List<Task>> = mock()
        viewModel.filteredTasks.observeForever(observer)

        // A search for double spaces "  " should match task 2
        viewModel.updateSearchQuery("  ")
        assertEquals(1, viewModel.filteredTasks.value?.size)
        assertEquals(2, viewModel.filteredTasks.value?.get(0)?.id)

        viewModel.filteredTasks.removeObserver(observer)
    }

    @Test
    fun viewModel_search_caseInsensitiveDescription_matches() = runTest {
        val mockRepo: TaskRepository = mock()
        val tasks = listOf(
            Task(1, "Fix Layout", "REPAIR UI VIEWS", "Work", "High", false, "2026-06-12")
        )
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(tasks))

        val viewModel = TaskViewModel(mockRepo)
        val observer: Observer<List<Task>> = mock()
        viewModel.filteredTasks.observeForever(observer)

        // Query in lowercase, description in uppercase
        viewModel.updateSearchQuery("repair")
        assertEquals(1, viewModel.filteredTasks.value?.size)
        assertEquals("Fix Layout", viewModel.filteredTasks.value?.get(0)?.title)

        viewModel.filteredTasks.removeObserver(observer)
    }

    // --- Repository Mapping Failures & Edge Cases ---

    @Test
    fun repository_getAllTasks_emptyEntityList_mapsToEmptyDomainList() = runTest {
        val mockDao: TaskDao = mock()
        whenever(mockDao.getAllTasks()).thenReturn(flowOf(emptyList()))

        val repository = TaskRepositoryImpl(mockDao)
        var resultList: List<Task>? = null
        repository.getAllTasks().collect {
            resultList = it
        }

        assertNotNull(resultList)
        assertEquals(0, resultList!!.size)
    }

    @Test
    fun repository_mapping_entityWithExtremeValues_mapsCorrectly() = runTest {
        val mockDao: TaskDao = mock()
        val entities = listOf(
            TaskEntity(
                id = -999, 
                title = "", 
                description = "~!@#$%^&*()_+{}|:\"<>?`-=[]\\;',./", 
                category = "   ", 
                priority = "", 
                isCompleted = false, 
                dueDate = "1970-01-01"
            )
        )
        whenever(mockDao.getAllTasks()).thenReturn(flowOf(entities))

        val repository = TaskRepositoryImpl(mockDao)
        var resultList: List<Task>? = null
        repository.getAllTasks().collect {
            resultList = it
        }

        assertNotNull(resultList)
        assertEquals(1, resultList!!.size)
        val mapped = resultList!![0]
        assertEquals(-999, mapped.id)
        assertEquals("", mapped.title)
        assertEquals("~!@#$%^&*()_+{}|:\"<>?`-=[]\\;',./", mapped.description)
        assertEquals("   ", mapped.category)
        assertEquals("", mapped.priority)
        assertEquals("1970-01-01", mapped.dueDate)
    }

    @Test
    fun repository_getAllTasks_flowThrowsException_propagates() = runTest {
        val mockDao: TaskDao = mock()
        val expectedException = RuntimeException("Database error")
        whenever(mockDao.getAllTasks()).thenReturn(kotlinx.coroutines.flow.flow {
            throw expectedException
        })

        val repository = TaskRepositoryImpl(mockDao)
        var exceptionThrown = false
        try {
            repository.getAllTasks().collect {}
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
            exceptionThrown = true
        }
        org.junit.jupiter.api.Assertions.assertTrue(exceptionThrown)
    }

    // --- Empty Lists in Bulk Operations ---

    @Test
    fun viewModel_bulkDeleteTasks_emptyList_noInteractionsWithRepository() = runTest {
        val mockRepo: TaskRepository = mock()
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(emptyList()))

        val viewModel = TaskViewModel(mockRepo)
        viewModel.bulkDeleteTasks(emptyList())

        verify(mockRepo, org.mockito.kotlin.never()).deleteTask(any())
    }

    @Test
    fun viewModel_bulkToggleComplete_emptyList_noInteractionsWithRepository() = runTest {
        val mockRepo: TaskRepository = mock()
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(emptyList()))

        val viewModel = TaskViewModel(mockRepo)
        viewModel.bulkToggleComplete(emptyList())

        verify(mockRepo, org.mockito.kotlin.never()).toggleTaskCompleted(any())
    }

    @Test
    fun viewModel_bulkUpdatePriority_emptyList_noInteractionsWithRepository() = runTest {
        val mockRepo: TaskRepository = mock()
        whenever(mockRepo.getAllTasks()).thenReturn(flowOf(emptyList()))

        val viewModel = TaskViewModel(mockRepo)
        viewModel.bulkUpdatePriority(emptyList(), "High")

        verify(mockRepo, org.mockito.kotlin.never()).updateTask(any())
    }
}

