package com.example.sololeveling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Color.Companion.Cyan
//import androidx.compose.ui.graphics.Color.Companion.Green
//import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sololeveling.model.Task
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.compose.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import android.os.PowerManager
import android.net.Uri



class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasUsageAccessPermission(this)) {
            requestUsageAccessPermission(this) // Redirects user to settings
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, AppMonitorService::class.java))
        } else {
            startService(Intent(this, AppMonitorService::class.java))
        }

        setContent {
            AppNavigation()
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()

        if (hasUsageAccessPermission(this)) {
            startService(Intent(this, AppMonitorService::class.java))
            setContent {
                AppNavigation()
            }
        } else {
            requestUsageAccessPermission(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Task & App Lock Manager") })
        }
    ) { padding ->
        NavHost(navController, startDestination = "tasks", modifier = Modifier.padding(padding)) {
            composable("tasks") { WeeklyTaskPlannerApp(navController) }
            composable("app_lock") { AppLockScreen(navController) }
        }
    }
}

@Composable
fun WeeklyTaskPlannerApp(navController: NavController,viewModel: TaskViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        val gradientColors = listOf(Cyan, Green, Red /*...*/)

        Text(
            text = "Weekly Tasks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Day Selection Row
        DaySelector(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Task List for Selected Day
        TaskList(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Navigate to App Lock Screen
        Button(onClick = { navController.navigate("app_lock") }) {
            Text("Go to App Lock Manager")
        }
    }
}

@Composable
fun DaySelector(viewModel: TaskViewModel) {
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    LazyRow(modifier = Modifier.fillMaxWidth()) {
        items(daysOfWeek) { day ->
            val isSelected = viewModel.selectedDay == day
            Button(
                onClick = { viewModel.setDay(day) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color.Blue else Color.Gray
                ),
                modifier = Modifier.padding(4.dp)
            ) {
                Text(day, color = Color.White)
            }
        }
    }
}

@Composable
fun TaskList(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks

    LazyColumn {
        items(tasks) { task ->
            TaskItem(task = task, onToggle = { viewModel.toggleTask(task) })
        }
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (task.isChecked) Color.LightGray else Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isChecked,
                onCheckedChange = { onToggle() }
            )
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
fun hasUsageAccessPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

fun requestUsageAccessPermission(context: Context) {
    Toast.makeText(
        context,
        "Please enable Usage Access permission to lock apps.",
        Toast.LENGTH_LONG
    ).show()

    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    context.startActivity(intent) // Opens system settings for the user to enable the permission
}

fun disableBatteryOptimization(context: Context) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    }
}