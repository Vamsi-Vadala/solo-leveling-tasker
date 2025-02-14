package com.example.sololeveling

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.security.SecureRandom

@Composable
fun AppLockScreen(navController: NavController) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val installedApps = remember { getInstalledApps(packageManager) }
    val prefs = context.getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
    val selectedApps = remember { mutableStateListOf<String>() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select Apps to Lock", style = MaterialTheme.typography.headlineMedium)

        LazyColumn {
            items(installedApps) { app ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (selectedApps.contains(app.packageName)) {
                            selectedApps.remove(app.packageName)
                        } else {
                            selectedApps.add(app.packageName)
                        }
                    }.padding(8.dp)
                ) {
                    Checkbox(checked = selectedApps.contains(app.packageName), onCheckedChange = null)
                    Text(text = app.name, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Button(
            onClick = {
                saveLockedApps(prefs, selectedApps)
                Toast.makeText(context, "Apps locked!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Lock Selected Apps")
        }
    }
}

fun getInstalledApps(pm: PackageManager): List<AppItem> {
    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // Exclude system apps
        .map { AppItem(it.packageName, pm.getApplicationLabel(it).toString()) }
}

data class AppItem(val packageName: String, val name: String)

fun saveLockedApps(prefs: SharedPreferences, selectedApps: List<String>) {
    val pin = (1000..9999).random().toString()
    with(prefs.edit()) {
        putStringSet("lockedApps", selectedApps.toSet())
        putString("pin", pin)
        putLong("lockEndTime", System.currentTimeMillis() + 3 * 60 * 60 * 1000) // 3-hour lock
        apply()
    }
}
