package com.example.sololeveling

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

fun getForegroundApp(context: Context): String? {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val time = System.currentTimeMillis()
    val usageEvents = usageStatsManager.queryEvents(time - 5000, time) // Last 5 seconds

    var foregroundApp: String? = null
    val event = UsageEvents.Event()
    while (usageEvents.hasNextEvent()) {
        usageEvents.getNextEvent(event)
        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            foregroundApp = event.packageName
        }
    }
    return foregroundApp
}
