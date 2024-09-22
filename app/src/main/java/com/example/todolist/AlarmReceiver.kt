package com.example.todolist

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val task = intent.getStringExtra("TASK") ?: "No Task"
        val description = intent.getStringExtra("DESCRIPTION") ?: "No Description"

        showNotification(context, task, description)
    }

    private fun showNotification(context: Context, task: String, description: String) {
        val notificationId = 101
        val channelId = "todo_channel"

        // Set notification sound
        val sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.applogo) // Replace with your notification icon
            .setContentTitle(task)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(sound)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }
}
