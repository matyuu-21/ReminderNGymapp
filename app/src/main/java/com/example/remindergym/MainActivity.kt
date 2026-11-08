package com.example.remindergym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.remindergym.ui.theme.ReminderGymTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.mutableIntStateOf
import android.Manifest
import android.graphics.Outline
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.remindergym.data.AppDatabase
import com.example.remindergym.reminders.Reminder
import com.example.remindergym.notifications.createNotificationChannel
import com.example.remindergym.notifications.scheduleReminder
import com.example.remindergym.notifications.cancelReminder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.example.remindergym.gym.GymScreen
import com.example.remindergym.gym.Workout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel(this)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ ->

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val db = AppDatabase.getDatabase(this)
        val dao = db.reminderDao()
        val workoutDao = db.workoutDao()
        val remindersFlow = dao.getAll()
        val workoutsFlow = workoutDao.getAll()

        setContent {
            ReminderGymTheme {
                val reminders by remindersFlow.collectAsState(initial = emptyList())
                val workouts by workoutsFlow.collectAsState(initial = emptyList())
                val scope = rememberCoroutineScope()
                var selectedTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null
                                    )
                                },
                                label = { Text("Reminders") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                                label = { Text("Gym") }
                            )
                        }
                    }
                ) { innerPadding ->
                    if (selectedTab == 0) {
                        ReminderHomescreen(
                            reminders = reminders,
                            onAdd = { title, hour, minute ->
                                scope.launch {
                                    val id = dao.insert(
                                        Reminder(
                                            title = title,
                                            hour = hour,
                                            minute = minute
                                        )
                                    )
                                    scheduleReminder(
                                        this@MainActivity,
                                        Reminder(id.toInt(), title, hour, minute)
                                    )
                                }
                            },
                            onDelete = { reminder ->
                                scope.launch {
                                    cancelReminder(this@MainActivity, reminder)
                                    dao.delete(reminder)
                                }
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        GymScreen(
                            workouts = workouts,
                            onAdd = { exercise, sets, reps, weight, rir ->
                                scope.launch {
                                    workoutDao.insert(
                                        Workout(
                                            exercise = exercise,
                                            sets = sets,
                                            reps = reps,
                                            weight = weight,
                                            rir = rir
                                        )
                                    )
                                }
                            },
                            onDelete = { workout ->
                                scope.launch { workoutDao.delete(workout) }
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val hour12 = if (hour % 12 == 0) 12 else hour % 12
    return "%d:%02d %s".format(hour12, minute, amPm)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderHomescreen(
    reminders: List<Reminder>,
    onAdd: (String, Int, Int) -> Unit,
    onDelete: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {

    var showForm by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf("") }
    var pickedHour by remember { mutableIntStateOf((-1)) }
    var pickedMinute by remember{ mutableIntStateOf(0) }
    var showTimePicker by remember {mutableStateOf(false)}
    val timePickerState = rememberTimePickerState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "My Reminders",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Stay organized!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!showForm) {
            Button(
                onClick = { showForm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add reminder")
            }
        }
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = {showTimePicker = false},
                confirmButton = {
                    TextButton(onClick = {
                        pickedHour = timePickerState.hour
                        pickedMinute = timePickerState.minute
                        showTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {showTimePicker = false}) {
                        Text("Cancel")
                    }
                },
                text = {TimePicker(state = timePickerState)}
            )
        }
        if (showForm) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {showTimePicker = true},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (pickedHour < 0) "Pick a time" else formatTime(pickedHour, pickedMinute))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            if (titleText.isNotBlank() && pickedHour >= 0) {
                                onAdd(titleText, pickedHour, pickedMinute)
                                titleText = ""
                                pickedHour = -1
                                showForm = false
                            }
                        }) {
                            Text("Save")
                        }
                        TextButton(onClick = { showForm = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
        if (reminders.isEmpty()) {
            Text(
                text = "No reminders yet, Add your first one!",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(reminders, key = { it.id }) { reminder ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = reminder.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = formatTime(reminder.hour, reminder.minute),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDelete(reminder) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ReminderHomescreenPreview() {
    ReminderGymTheme {
        ReminderHomescreen(
            reminders = listOf(
                Reminder(1, "Eat Food", 11, 0),
                Reminder(2, "Go Gym", 17, 0)
            ),
            onAdd = { _, _, _ -> },
            onDelete = { }
        )
    }
}
