package com.example.todolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val task = intent?.getStringExtra("TASK") ?: "No Task"
        val description = intent?.getStringExtra("DESCRIPTION") ?: "No Description"

        // Create Notification
        val builder = NotificationCompat.Builder(context!!, "task_channel")
            .setSmallIcon(R.drawable.applogo) // Add your task icon
            .setContentTitle("Task Reminder: $task")
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Show Notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, builder.build())
    }
}
