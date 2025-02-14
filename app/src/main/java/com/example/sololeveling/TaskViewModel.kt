package com.example.sololeveling

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sololeveling.TaskDatabase
import com.example.sololeveling.model.Task
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = TaskDatabase.getDatabase(application).taskDao()

    var selectedDay by mutableStateOf(getCurrentDay())

    private var _tasks = mutableStateOf<List<Task>>(emptyList())
    val tasks: State<List<Task>> get() = _tasks

    init {
        viewModelScope.launch {
            insertDefaultTasks()  // ✅ Insert tasks on first launch
            loadTasksForDay(selectedDay)
        }
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            val task = Task(title = title, day = selectedDay, isChecked = false)
            taskDao.insertTask(task)
            loadTasksForDay(selectedDay)
        }
    }

    fun loadTasksForDay(day: String) {
        viewModelScope.launch {
            _tasks.value = taskDao.getTasksForDay(day)
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isChecked = !task.isChecked))
            loadTasksForDay(selectedDay)
        }
    }

    fun setDay(day: String) {
        selectedDay = day
        loadTasksForDay(day)
    }

    private fun getCurrentDay(): String {
        return LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }

    private suspend fun insertDefaultTasks() {
        val existingTasks = taskDao.getAllTasks()
        if (existingTasks.isEmpty()) {  // ✅ Insert only if the database is empty
            val defaultTasks = listOf(
                Task(title = "Morning Workout", day = "Monday"),
                Task(title = "Morning Applications", day = "Monday"),
                Task(title = "Evening Applications", day = "Monday"),
                Task(title = "Personal Project", day = "Monday"),
                Task(title = "Morning Workout", day = "Tuesday"),
                Task(title = "Morning Applications", day = "Tuesday"),
                Task(title = "Evening Applications", day = "Tuesday"),
                Task(title = "Leet Programming Languages", day = "Tuesday"),
                Task(title = "Morning Workout", day = "Wednesday"),
                Task(title = "Morning Applications", day = "Wednesday"),
                Task(title = "Evening Applications", day = "Wednesday"),
                Task(title = "Database Management", day = "Wednesday"),
                Task(title = "Morning Workout", day = "Thursday"),
                Task(title = "Morning Applications", day = "Thursday"),
                Task(title = "Evening Applications", day = "Thursday"),
                Task(title = "Leet Code DBMS", day = "Thursday"),
                Task(title = "Morning Workout", day = "Friday"),
                Task(title = "Morning Applications", day = "Friday"),
                Task(title = "Evening Applications", day = "Friday"),
                Task(title = "Research Papers & Code", day = "Friday"),
                Task(title = "Morning Workout", day = "Saturday"),
                Task(title = "Morning Applications", day = "Saturday"),
                Task(title = "Evening Applications", day = "Saturday"),
                Task(title = "Interview Prep", day = "Saturday"),
                Task(title = "Morning Workout", day = "Sunday"),
                Task(title = "Morning Applications", day = "Sunday"),
                Task(title = "Evening Applications", day = "Sunday"),
                Task(title = "Personal Project", day = "Sunday")
            )
            taskDao.insertTasks(defaultTasks)
        }
    }
}
