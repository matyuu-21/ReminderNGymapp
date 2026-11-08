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
import java.util.Calendar
import android.content.Intent
import com.example.remindergym.reminders.Reminder
import com.example.remindergym.reminders.ReminderReceiver


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
    val alarmManager = context.getSystemService(AlarmManager::class.java)

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, reminder.hour)
        set(Calendar.MINUTE, reminder.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR,1 )
    }

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        reminderIntent(context, reminder.id, reminder.title)
    )

}

fun cancelReminder(context: Context, reminder: Reminder) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    alarmManager.cancel(reminderIntent(context, reminder.id, reminder.title))
}
private fun reminderIntent(context: Context, id: Int, title: String): PendingIntent {
    val intent = Intent(context, ReminderReceiver::class.java).putExtra("title", title)
    return PendingIntent.getBroadcast(
        context, id, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}