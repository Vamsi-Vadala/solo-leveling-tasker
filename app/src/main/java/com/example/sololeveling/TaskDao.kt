package com.example.sololeveling.database

import androidx.room.*
import com.example.sololeveling.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE day = :day")
    suspend fun getTasksForDay(day: String): List<Task> // ✅ Correct return type

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task> // ✅ Correct return type

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long // ✅ Return ID of inserted task

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>): List<Long> // ✅ Return list of inserted IDs

    @Update
    suspend fun updateTask(task: Task): Int // ✅ Return number of rows affected

    @Delete
    suspend fun deleteTask(task: Task): Int // ✅ Return number of rows affected
}
