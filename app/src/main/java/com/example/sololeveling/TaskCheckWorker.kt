package com.example.sololeveling

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sololeveling.TaskDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class TaskCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {


        val taskDao = TaskDatabase.getDatabase(applicationContext).taskDao()
        val yesterday = getYesterdayDayName()
        val dayBeforeYesterday = getDayBeforeYesterdayDayName()

        return withContext(Dispatchers.IO) {
            val incompleteTasks = taskDao.getTasksForDay(yesterday).filter { !it.isChecked }
            val dayBeforeYesterdayTasks  = taskDao.getTasksForDay(dayBeforeYesterday).filter { !it.isChecked }
            Log.d("Tasks", incompleteTasks.toString())
            Log.d("Yesterday", yesterday)
            Log.d("DayBeforeYesterday", dayBeforeYesterday)

            // 🔹 Reset all tasks from Day Before Yesterday (set isChecked = false)
            if (dayBeforeYesterdayTasks.isNotEmpty()) {
                dayBeforeYesterdayTasks.forEach { task ->
                    taskDao.updateTask(task.copy(isChecked = false)) // Reset Task
                }
                Log.d("Reset Tasks", "All tasks for $dayBeforeYesterday have been reset")
            }

            Log.d("Tasks",incompleteTasks.toString())
            Log.d("Yesterday",yesterday)


            if (incompleteTasks.isNotEmpty()) {
                // Start VPN Service if tasks are incomplete
                startVpnService(applicationContext)
            }
            else{
                stopVpnService(applicationContext)
            }
            scheduleTaskCheckWorker(applicationContext)
            Result.success()
        }
    }

    private fun getYesterdayDayName(): String {
        return LocalDate.now().minusDays(1).dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }

    private fun getDayBeforeYesterdayDayName(): String {
        return LocalDate.now().minusDays(2).dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }

    private fun startVpnService(context: Context) {
        val intent = Intent(context, MyVpnService::class.java)
        context.startService(intent)
    }
}
