package com.example.sololeveling

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import kotlinx.coroutines.*
import android.util.Log
import androidx.core.app.NotificationCompat

class AppMonitorService : Service() {
    private lateinit var prefs: SharedPreferences
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)

        val notification = NotificationCompat.Builder(this, "AppMonitorChannel")
            .setContentTitle("App Lock Running")
            .setContentText("Your selected apps are locked")
            .setSmallIcon(R.drawable.ic_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        createNotificationChannel(this)
        startForeground(1, notification)

        startMonitoring()
    }

    private fun startMonitoring() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                checkForegroundApp()
                delay(2000) // Runs every 2 seconds
            }
        }
    }

    private fun checkForegroundApp() {
        val lockedApps = prefs.getStringSet("lockedApps", emptySet()) ?: emptySet()
        val lockEndTime = prefs.getLong("lockEndTime", 0)

        if (System.currentTimeMillis() > lockEndTime) {
            return // Unlock apps automatically after 3 hours
        }

        val topPackage = getForegroundAppPackage(this)
        if (topPackage in lockedApps) {
            val intent = Intent(this, PinEntryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        Log.d("com.example.sololeveling.AppMonitorService", "Foreground app: $topPackage")
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel() // Cancel the coroutine when service stops
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getForegroundAppPackage(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            android.app.usage.UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 10, // Check last 10 seconds
            time
        )

        return stats.maxByOrNull { it.lastTimeUsed }?.packageName
    }

}
private const val CHANNEL_ID = "AppMonitorServiceChannel"

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "App Monitor Service",
        NotificationManager.IMPORTANCE_LOW
    )
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}
