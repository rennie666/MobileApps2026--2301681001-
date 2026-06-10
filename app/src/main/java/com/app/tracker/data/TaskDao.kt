package com.app.tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {

    // Read
    @Query("SELECT * FROM tasks_table")
    fun getAllTasks(): List<Task>

    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task)

    // Update
    @Update
    fun updateTask(task: Task)

    // Delete
    @Delete
    fun deleteTask(task: Task)
}