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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dao = AppDatabase.getDatabase(this).reminderDao()
        val remindersFlow = dao.getAll()

        setContent {
            ReminderGymTheme {
                val reminders by remindersFlow.collectAsState(initial = emptyList())
                val scope = rememberCoroutineScope()


                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ReminderHomescreen(
                        reminders = reminders,
                        onAdd = { title, hour, minute ->
                            scope.launch { dao.insert(Reminder(title = title, hour = hour, minute = minute)) }
                        },
                        onDelete = { reminder ->
                            scope.launch { dao.delete(reminder) }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
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
