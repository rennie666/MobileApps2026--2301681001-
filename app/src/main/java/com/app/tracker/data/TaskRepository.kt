package com.app.tracker.data

import android.content.Context
import android.content.SharedPreferences
import com.app.tracker.model.Task
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class TaskRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("task_planner_prefs", Context.MODE_PRIVATE)

    fun getTasks(): List<Task> {
        val jsonString = prefs.getString("tasks_list", null)
        if (jsonString == null) {
            val defaultTasks = createDefaultTasks()
            saveTasks(defaultTasks)
            return defaultTasks
        }
        return try {
            val tasks = mutableListOf<Task>()
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                tasks.add(
                    Task(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        category = obj.getString("category"),
                        priority = obj.getString("priority"),
                        isCompleted = obj.getBoolean("isCompleted"),
                        dueDate = obj.getString("dueDate")
                    )
                )
            }
            tasks
        } catch (e: Exception) {
            e.printStackTrace()
            createDefaultTasks()
        }
    }

    fun getTaskById(id: String): Task? {
        return getTasks().find { it.id == id }
    }

    fun saveTask(task: Task) {
        val tasks = getTasks().toMutableList()
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
        } else {
            tasks.add(task)
        }
        saveTasks(tasks)
    }

    fun deleteTask(id: String) {
        val tasks = getTasks().filter { it.id != id }
        saveTasks(tasks)
    }

    fun toggleTaskCompleted(id: String) {
        val tasks = getTasks().toMutableList()
        val index = tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            val task = tasks[index]
            tasks[index] = task.copy(isCompleted = !task.isCompleted)
            saveTasks(tasks)
        }
    }

    private fun saveTasks(tasks: List<Task>) {
        val jsonArray = JSONArray()
        for (task in tasks) {
            val obj = JSONObject()
            obj.put("id", task.id)
            obj.put("title", task.title)
            obj.put("description", task.description)
            obj.put("category", task.category)
            obj.put("priority", task.priority)
            obj.put("isCompleted", task.isCompleted)
            obj.put("dueDate", task.dueDate)
            jsonArray.put(obj)
        }
        prefs.edit().putString("tasks_list", jsonArray.toString()).apply()
    }

    private fun createDefaultTasks(): List<Task> {
        return listOf(
            Task(
                id = UUID.randomUUID().toString(),
                title = "Design Project Mockups",
                description = "Create modern UI/UX wireframes for the tracker application using Figma, focusing on layout and color harmony.",
                category = "Work",
                priority = "High",
                isCompleted = false,
                dueDate = "2026-06-10"
            ),
            Task(
                id = UUID.randomUUID().toString(),
                title = "Buy Weekly Groceries",
                description = "Pick up fresh vegetables, fruits, almond milk, whole wheat bread, and eggs from the local farmers market.",
                category = "Personal",
                priority = "Medium",
                isCompleted = true,
                dueDate = "2026-06-06"
            ),
            Task(
                id = UUID.randomUUID().toString(),
                title = "Morning Cardio Workout",
                description = "45 minutes outdoor running session at the park followed by stretching and core training.",
                category = "Health",
                priority = "High",
                isCompleted = false,
                dueDate = "2026-06-07"
            ),
            Task(
                id = UUID.randomUUID().toString(),
                title = "Kotlin Design Patterns",
                description = "Read chapter 4 and 5 of Kotlin concurrency and design patterns book.",
                category = "Study",
                priority = "Low",
                isCompleted = true,
                dueDate = "2026-06-08"
            ),
            Task(
                id = UUID.randomUUID().toString(),
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
