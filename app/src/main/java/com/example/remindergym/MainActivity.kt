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
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Info
import com.example.remindergym.gym.ProgressScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Edit

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
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = {selectedTab = 2},
                                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                                label = {Text("Progress")}
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        0 -> ReminderHomescreen(
                            reminders = reminders,
                            onAdd = { title, hour, minute, days ->
                                scope.launch {
                                    val id = dao.insert(
                                        Reminder(
                                            title = title,
                                            hour = hour,
                                            minute = minute,
                                            days = days
                                        )
                                    )
                                    scheduleReminder(
                                        this@MainActivity,
                                        Reminder(id.toInt(), title, hour, minute, days)
                                    )
                                }
                            },
                            onEdit = {reminder ->
                                scope.launch {
                                    cancelReminder(this@MainActivity, reminder)
                                    dao.update(reminder)
                                    scheduleReminder(this@MainActivity, reminder)
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
                        1 -> GymScreen(
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

                        else -> ProgressScreen(
                            workouts = workouts,
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
fun formatDays(days: String): String{
    if (days.length == 7) return "Every day"
    val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return days.map {names[it.digitToInt() - 1] }.joinToString(",")
}

@Composable
fun DayPicker(selected: Set<Int>, onToggle: (Int) -> Unit) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEachIndexed { index, label ->
            val day = index + 1
            val isOn = day in selected
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isOn) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .clickable {onToggle(day)},
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isOn) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderHomescreen(
    reminders: List<Reminder>,
    onAdd: (String, Int, Int, String) -> Unit,
    onEdit: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reminder?>(null) }
    var titleText by remember { mutableStateOf("") }
    var pickedHour by remember { mutableIntStateOf(-1) }
    var pickedMinute by remember { mutableIntStateOf(0) }
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    fun closeForm() {
        titleText = ""
        pickedHour = -1
        selectedDays = setOf(1, 2, 3, 4, 5, 6, 7)
        editing = null
        showForm = false
    }

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
                onDismissRequest = { showTimePicker = false },
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
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                },
                text = { TimePicker(state = timePickerState) }
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
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (pickedHour < 0) "Pick a time" else formatTime(pickedHour, pickedMinute))
                    }
                    DayPicker(
                        selected = selectedDays,
                        onToggle = { day ->
                            selectedDays = if (day in selectedDays) selectedDays - day else selectedDays + day
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            if (titleText.isNotBlank() && pickedHour >= 0 && selectedDays.isNotEmpty()) {
                                val daysString = selectedDays.sorted().joinToString("")
                                val current = editing
                                if (current == null) {
                                    onAdd(titleText, pickedHour, pickedMinute, daysString)
                                } else {
                                    onEdit(
                                        current.copy(
                                            title = titleText,
                                            hour = pickedHour,
                                            minute = pickedMinute,
                                            days = daysString
                                        )
                                    )
                                }
                                closeForm()
                            }
                        }) {
                            Text(if (editing == null) "Save" else "Update")
                        }
                        TextButton(onClick = { closeForm() }) {
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
                                text = formatTime(reminder.hour, reminder.minute) +
                                        " • " + formatDays(reminder.days),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            IconButton(onClick = {
                                editing = reminder
                                titleText = reminder.title
                                pickedHour = reminder.hour
                                pickedMinute = reminder.minute
                                selectedDays = reminder.days.map { it.digitToInt() }.toSet()
                                showForm = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
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
}

@Preview(showBackground = true)
@Composable
fun ReminderHomescreenPreview() {
    ReminderGymTheme {
        ReminderHomescreen(
            reminders = listOf(
                Reminder(1, "Eat Food", 11, 0),
                Reminder(2, "Go Gym", 17, 0, "135")
            ),
            onAdd = { _, _, _, _ -> },
            onEdit = { },
            onDelete = { }
        )
    }
}
