package com.example.sololeveling

import android.app.Activity
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
import android.content.pm.PackageManager
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
import android.net.VpnService
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat
import android.Manifest
import android.icu.util.Calendar
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasUsageAccessPermission(this)) {
            requestUsageAccessPermission(this)
        }

        if (!hasNotificationPermission(this)) {
            requestNotificationPermission(this)
        }

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState(initial = false)

            MaterialTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
                val snackbarHostState = remember { SnackbarHostState() }
                val viewModel: TaskViewModel = viewModel()

                // Observe Snackbar messages from ViewModel
                LaunchedEffect(viewModel.snackbarMessage) {
                    viewModel.snackbarMessage.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }


                AppNavigation(snackbarHostState, viewModel, themeViewModel)
            }
        }
        scheduleTaskCheckWorker(this)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()

        if (hasUsageAccessPermission(this)) {
            setContent {
                val themeViewModel: ThemeViewModel = viewModel()
                val isDarkTheme by themeViewModel.isDarkTheme.collectAsState(initial = false)

                MaterialTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {

                    val snackbarHostState = remember { SnackbarHostState() }
                    val viewModel: TaskViewModel = viewModel()

                    LaunchedEffect(viewModel.snackbarMessage) {
                        viewModel.snackbarMessage.collect { message ->
                            snackbarHostState.showSnackbar(message)
                        }
                    }

                    AppNavigation(snackbarHostState, viewModel,themeViewModel)
                }
            }
        } else {
            requestUsageAccessPermission(this)
        }
        scheduleTaskCheckWorker(this)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(snackbarHostState: SnackbarHostState, viewModel: TaskViewModel, themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState(initial = false)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Solo Leveling Tasker") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { themeViewModel.toggleTheme() },
                containerColor = Color(0xFFDDEB9B),
                contentColor = Color(0xFF143D60)
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    contentDescription = "Toggle Theme"
                )
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "tasks", modifier = Modifier.padding(padding)) {
            composable("tasks") { WeeklyTaskPlannerApp(navController, viewModel, snackbarHostState) }
            composable("block_apps") { BlockAppsScreen(navController, LocalContext.current) }
        }
    }
}




@Composable
fun WeeklyTaskPlannerApp(
    navController: NavController,
    viewModel: TaskViewModel,
    snackbarHostState: SnackbarHostState
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Weekly Tasks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        DaySelector(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        TaskList(viewModel, snackbarHostState)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("block_apps") },
            colors = ButtonDefaults.buttonColors(
                containerColor =  Color(0xFFDDEB9B),
                contentColor = Color(0xFF143D60)
            )
        )
        {
            Text("Select Apps to Block")
        }
    }
}


@Composable
fun DaySelector(viewModel: TaskViewModel) {
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val accentColor = Color(0x97143D60)
    val finishedColor = Color(0x9FA0C878)


    val lazyListState = rememberLazyListState()
    val currentDayIndex = daysOfWeek.indexOf(viewModel.selectedDay)

    // Scroll to the current day when the component loads
    LaunchedEffect(currentDayIndex) {
        if (currentDayIndex != -1) {
            lazyListState.animateScrollToItem(currentDayIndex)
        }
    }
    LazyRow(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth()) {
        items(daysOfWeek) { day ->
            val isSelected = viewModel.selectedDay == day
            Button(
                onClick = { viewModel.setDay(day) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) finishedColor else accentColor
                ),
                modifier = Modifier.padding(4.dp)
            ) {
                Text(day, color = Color.White)
            }
        }
    }
}

@Composable
fun TaskList(viewModel: TaskViewModel, snackbarHostState: SnackbarHostState) {
    val tasks by viewModel.tasks

    LazyColumn {
        items(tasks) { task ->
            TaskItem(task, { viewModel.toggleTask(task) }, snackbarHostState)
        }
    }
}


@Composable
fun TaskItem(task: Task, onToggle: () -> Unit, snackbarHostState: SnackbarHostState) {
    val accentColor = Color(0x97143D60)
    val finishedColor = Color(0x9FA0C878)
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (task.isChecked) finishedColor   else accentColor)
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun requestNotificationPermission(activity: Activity) {
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100
        )
    }
}

fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // No need to request permission below Android 13
    }
}

fun scheduleTaskCheckWorker(context: Context) {
    val customHour = 0 // 🔹 Change this to your desired hour (24-hour format)
    val customMinute = 1 // 🔹 Change this to your desired minute

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, customHour)
        set(Calendar.MINUTE, customMinute)
        set(Calendar.SECOND, 0)
    }

    val now = Calendar.getInstance()
    var delay = calendar.timeInMillis - now.timeInMillis
    if (delay < 0) {
        delay += TimeUnit.DAYS.toMillis(1) // Schedule for the next day if time has passed
    }


    val workRequest = OneTimeWorkRequestBuilder<TaskCheckWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "TaskCheckWorker",
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
}

