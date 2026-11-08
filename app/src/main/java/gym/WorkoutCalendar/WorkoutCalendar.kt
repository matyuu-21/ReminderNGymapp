package com.example.remindergym.gym

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun WorkoutCalendar(workouts: List<Workout>, modifier: Modifier = Modifier) {
    val weeks = 12
    val today = LocalDate.now()

    val countPerDay = workouts
        .groupingBy { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() }
        .eachCount()

    val thisMonday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val firstMonday = thisMonday.minusWeeks((weeks - 1).toLong())

    val daysTrained = countPerDay.keys.count {!it.isBefore(firstMonday) && !it.isAfter(today)}

    val filled = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$daysTrained days trained in the last $weeks weeks",
            style = MaterialTheme.typography.titleMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (week in 0 until weeks) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (day in 0 until 7) {
                        val date = firstMonday.plusDays((week * 7 + day).toLong())
                        val count = countPerDay[date] ?:0
                        val color = when {
                            date.isAfter(today) -> Color.Transparent
                            count == 0 -> empty
                            count == 1 -> filled.copy(alpha = 0.4f)
                            count == 2 -> filled.copy(alpha = 0.7f)
                            else -> filled
                        }
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(color, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
        Text(
            text = "Darker = more entries logged that day",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}