package com.example.todolist

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Load tasks from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("ToDoListPrefs", Context.MODE_PRIVATE)
        val tasksSet = sharedPreferences.getStringSet("tasks", emptySet()) ?: emptySet()

        // Convert the task set into a sorted list based on time
        val sortedTasksList = sortTasksByTime(tasksSet)

        // Display the sorted tasks in the widget
        val tasksList = sortedTasksList.joinToString("\n") // Join tasks with new line
        views.setTextViewText(R.id.widgetTextView, tasksList)

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    // Sort tasks by the scheduled time (assuming the format: "Task Description @ HH:mm")
    private fun sortTasksByTime(tasksSet: Set<String>): List<String> {
        val taskTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return tasksSet
            .mapNotNull { task ->
                val timePart = task.substringAfterLast("@").trim() // Extract time (e.g., "08:05")
                try {
                    val taskTime = taskTimeFormat.parse(timePart)
                    Pair(task, taskTime)
                } catch (e: Exception) {
                    null // If the task doesn't have a valid time, skip it
                }
            }
            .sortedBy { it.second } // Sort by the extracted time
            .map { it.first } // Return the sorted list of tasks
    }

    // Handle task removal or other updates
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                Intent(
                    context,
                    TaskWidgetProvider::class.java
                ).component
            )
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
