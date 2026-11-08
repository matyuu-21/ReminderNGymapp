package com.example.remindergym.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.remindergym.notifications.showNotification

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        showNotification(context, title, "Time for your reminder!")
    }
}
