package com.example.todolist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val task = intent.getStringExtra("TASK") ?: "Task"
        val description = intent.getStringExtra("DESCRIPTION") ?: "Description"
        showNotification(context, task, description)
    }

    private fun showNotification(context: Context, task: String, description: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_notification_channel"

        // Get the ringtone URI from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("ToDoListPrefs", Context.MODE_PRIVATE)
        val ringtoneUriString = sharedPreferences.getString("ringtone", Settings.System.DEFAULT_NOTIFICATION_URI.toString())
        val ringtoneUri: Uri = Uri.parse(ringtoneUriString)

        // Create notification channel for Android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open MainActivity when notification is clicked
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(task) // Task title
            .setContentText(description) // Task description
            .setSmallIcon(R.drawable.applogo) // Replace with your icon
            .setSound(ringtoneUri) // Use the selected notification sound
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }
}
