package com.example.todolist

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Load tasks from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("ToDoListPrefs", Context.MODE_PRIVATE)
        val tasksSet = sharedPreferences.getStringSet("tasks", emptySet()) ?: emptySet()

        // Display the tasks in the widget
        val tasksList = tasksSet.joinToString("\n") // Join tasks with new line
        views.setTextViewText(R.id.widgetTextView, tasksList)

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    // Handle task removal
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(Intent(context, TaskWidgetProvider::class.java).component)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
