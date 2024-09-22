package com.example.todolist


import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

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

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val widgetText = loadUpcomingTasks(context)

            // Create an intent to open the app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Create the RemoteViews object and attach the pending intent
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            views.setTextViewText(R.id.widgetTaskText, widgetText)
            views.setOnClickPendingIntent(R.id.widgetTaskText, pendingIntent)

            // Tell the AppWidgetManager to perform an update on the current widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        // Load upcoming tasks for widget display
        private fun loadUpcomingTasks(context: Context): String {
            val sharedPreferences = context.getSharedPreferences("ToDoListPrefs", Context.MODE_PRIVATE)
            val tasksSet = sharedPreferences.getStringSet("tasks", emptySet())
            val tasksList = tasksSet?.toList() ?: emptyList()

            return if (tasksList.isNotEmpty()) {
                tasksList.joinToString("\n")
            } else {
                "No upcoming tasks"
            }
        }
    }
}
