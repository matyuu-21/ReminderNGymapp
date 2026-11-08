package com.example.remindergym.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import com.example.remindergym.reminders.Reminder
import com.example.remindergym.reminders.ReminderReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId



const val CHANNEL_ID = "reminders"

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Reminders",
        NotificationManager.IMPORTANCE_HIGH
    )
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}

fun showNotification(context: Context, title: String, text: String) {
    //does nothing if u click don't allow
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) return

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context)
        .notify(System.currentTimeMillis().toInt(), notification)
}

fun scheduleReminder(context: Context, reminder: Reminder) {
    val days = reminder.days.map {it.digitToInt() }.filter {it in 1..7}
    if (days.isEmpty()) return

    val alarmManager = context.getSystemService(AlarmManager::class.java)

    val now = LocalDateTime.now()
    var next = LocalDate.now().atTime(reminder.hour, reminder.minute)
    while (!next.isAfter(now) || next.dayOfWeek.value !in days){
        next = next.plusDays(1)
    }
    val millis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        millis,
        reminderIntent(context, reminder)
    )

}
fun cancelReminder(context: Context, reminder: Reminder) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    alarmManager.cancel(reminderIntent(context, reminder))
}
private fun reminderIntent(context: Context, reminder: Reminder): PendingIntent {
    val intent = Intent(context, ReminderReceiver::class.java)
        .putExtra("id", reminder.id)
        .putExtra("title", reminder.title)
        .putExtra("hour", reminder.hour)
        .putExtra("minute", reminder.minute)
        .putExtra("days", reminder.days)
    return PendingIntent.getBroadcast(
        context, reminder.id, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}