package com.example.todolist


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskDescription = intent.getStringExtra("task_description")

        val notificationBuilder = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.applogo)
            .setContentTitle("Task Reminder")
            .setContentText(taskDescription)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(1001, notificationBuilder.build())
        }
    }
}
