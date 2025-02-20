package com.example.sololeveling

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.net.VpnService
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController


@Composable
fun BlockAppsScreen(navController: NavController, context: Context) {
    val packageManager = context.packageManager
    val sharedPreferences = context.getSharedPreferences("BlockedApps", Context.MODE_PRIVATE)

    // State for loading and app list
    var installedApps by remember { mutableStateOf<List<ApplicationInfo>?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Fetch installed apps asynchronously
    LaunchedEffect(Unit) {
        installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .sortedBy { packageManager.getApplicationLabel(it).toString() }
    }

    // Filtered apps based on search query
    val filteredApps by remember {
        derivedStateOf {
            installedApps?.filter { appInfo ->
                packageManager.getApplicationLabel(appInfo).toString().contains(searchQuery, ignoreCase = true)
            } ?: emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Block Internet Access for Apps", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search apps...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true
        )

        Button(onClick = { navController.navigateUp() }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Back")
        }

        Button(onClick = { requestVpnPermission(context) }) {
            Text("Start VPN Service")
        }

//        Button(onClick = { stopVpnService(context) }, colors = ButtonDefaults.buttonColors(Color.Red)) {
//            Text("Stop VPN")
//        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Show loading indicator until the app list is ready
        if (installedApps == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator() // 🔹 Show loading spinner
            }
        } else {
            // 🔹 Show the filtered app list when loading is done
            val listState = rememberLazyListState()
            LazyColumn(state = listState) {
                items(filteredApps) { appInfo ->
                    AppItem(appInfo, packageManager, sharedPreferences)
                }
            }
        }
    }
}


@Composable
fun AppItem(appInfo: ApplicationInfo, packageManager: PackageManager, sharedPreferences: SharedPreferences) {
    val appName = packageManager.getApplicationLabel(appInfo).toString()
    val isBlocked = remember { mutableStateOf(sharedPreferences.getBoolean(appInfo.packageName, false)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val drawable = packageManager.getApplicationIcon(appInfo)

        // 🔹 Convert AdaptiveIconDrawable or BitmapDrawable to Bitmap
        val bitmap = remember {
            if (drawable is BitmapDrawable) {
                drawable.bitmap.asImageBitmap()
            } else if (drawable is AdaptiveIconDrawable) {
                convertAdaptiveIconToBitmap(drawable).asImageBitmap()
            } else {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap() // Default empty bitmap
            }
        }

        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text(appName, modifier = Modifier.weight(1f))

        Switch(
            checked = isBlocked.value,
            onCheckedChange = {
                isBlocked.value = it
                sharedPreferences.edit().putBoolean(appInfo.packageName, it).apply()
            }
        )
    }
}

private fun requestVpnPermission(context: Context) {
    val intent = VpnService.prepare(context)
    if (intent != null) {
        (context as Activity).startActivityForResult(intent, 100)
    } else {
        context.startService(Intent(context, MyVpnService::class.java))
    }
}

fun stopVpnService(context: Context) {
    Log.d("VPN", "Stop VPN button clicked")
    val intent = Intent("STOP_VPN")
    intent.setPackage(context.packageName) // Ensure it targets our app
    context.sendBroadcast(intent) // Send broadcast instead of direct service call

    Log.d("VPN", "Sent STOP_VPN broadcast")
}

fun convertAdaptiveIconToBitmap(adaptiveIcon: AdaptiveIconDrawable): Bitmap {
    val width = 108 // Standard icon size
    val height = 108
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val canvas = android.graphics.Canvas(bitmap)
    adaptiveIcon.setBounds(0, 0, canvas.width, canvas.height)
    adaptiveIcon.draw(canvas)

    return bitmap
}

