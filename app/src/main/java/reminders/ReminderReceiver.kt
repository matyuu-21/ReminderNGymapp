package com.example.remindergym.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.remindergym.notifications.scheduleReminder
import com.example.remindergym.notifications.showNotification

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        val title = intent.getStringExtra("title") ?: "Reminder"
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", 0)
        val days = intent.getStringExtra("days") ?: "1234567"

        showNotification(context, title, "Time for your reminder!")

        if (id != -1 && hour != -1) {
            scheduleReminder(context, Reminder(id, title, hour, minute, days))
        }
    }
}
