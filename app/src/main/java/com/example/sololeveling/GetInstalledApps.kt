package com.example.sololeveling

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

fun getInstalledApps(context: Context): List<String> {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)

    val apps = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
    return apps.map { it.activityInfo.packageName }
}
