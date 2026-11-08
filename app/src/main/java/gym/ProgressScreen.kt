package com.example.remindergym.gym

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId

@Composable
fun ProgressScreen(
    workouts: List<Workout>,
    modifier: Modifier = Modifier
) {
    val exercises = workouts.map {it.exercise}.distinct()
    var selected by remember {mutableStateOf((""))}
    val current = if (selected in exercises) selected else exercises.firstOrNull() ?: ""

    val bestPerDay = workouts
        .filter {it.exercise == current}
        .groupBy {Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() }
        .toSortedMap()
        .map {(_, dayWorkouts) -> dayWorkouts.maxOf {it.weight}}

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Progress",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            WorkoutCalendar(
                workouts = workouts,
                modifier = modifier.padding(16.dp)
            )
        }
        if (exercises.isEmpty()) {
            Text(
                text = "Log some workouts first, then your progress show up here.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exercises) {name ->
                    FilterChip(
                        selected = name == current,
                        onClick = {selected = name},
                        label = {Text(name)}
                    )
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = current, style = MaterialTheme.typography.titleMedium)

                    if (bestPerDay.size < 2) {
                        Text(
                            text = "Log this exercise on at least 2 different days to see a chart",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LineChart(
                            values = bestPerDay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(12.dp)
                        )
                        val change = bestPerDay.last() - bestPerDay.first()
                        Text("Best: %.1f kg".format(bestPerDay.max()))
                        Text(
                            text = "Change since first day: %+.1f kg".format(change),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart(values: List<Double>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val min = values.min()
        val max = values.max()
        val range = if (max - min == 0.0) 1.0 else max - min
        val stepX = size.width / (values.size - 1)

        val points = values.mapIndexed { index, value ->
            Offset(
                x = index * stepX,
                y = size.height - ((value - min) / range * size.height).toFloat()
            )
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach {lineTo(it.x, it.y)}
        }
        drawPath(path, lineColor, style = Stroke(width = 6f))
        points.forEach {drawCircle(lineColor, radius = 10f, center = it)}
    }
}