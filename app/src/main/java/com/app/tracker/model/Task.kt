package com.app.tracker.model

data class Task(
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // e.g. "Work", "Personal", "Health", "Study"
    val priority: String, // e.g. "High", "Medium", "Low"
    var isCompleted: Boolean,
    val dueDate: String
)
