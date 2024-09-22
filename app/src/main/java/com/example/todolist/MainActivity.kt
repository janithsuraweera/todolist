package com.example.todolist

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var todoList: MutableList<String>
    private lateinit var filteredList: MutableList<String>
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var alarmManager: AlarmManager
    private lateinit var searchBar: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

// In onCreate() of MainActivity
        val settingsButton: Button = findViewById(R.id.buttonSettings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


        // Initialize UI Components
        val editTextTask: EditText = findViewById(R.id.editTextTask)
        val editTextDescription: EditText = findViewById(R.id.editTextDescription) // Ensure this exists in your layout
        val buttonAdd: Button = findViewById(R.id.buttonAdd)
        searchBar = findViewById(R.id.searchBar)
        listView = findViewById(R.id.listView)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // SharedPreferences Initialize
        sharedPreferences = getSharedPreferences("ToDoListPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Load saved tasks
        todoList = loadTasks()
        filteredList = todoList.toMutableList() // Now this is safe
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filteredList)
        listView.adapter = adapter

        // Add Task with Time Picker for reminder
        buttonAdd.setOnClickListener {
            val task = editTextTask.text.toString()
            val description = editTextDescription.text.toString()

            if (task.isNotEmpty() && description.isNotEmpty()) {
                // Show TimePickerDialog to select reminder time
                showTimePickerDialog(task, description)
                editTextTask.text.clear()
                editTextDescription.text.clear() // Clear description field
            } else {
                Toast.makeText(this, "Please enter a task and description", Toast.LENGTH_SHORT).show()
            }
        }

        // Search Task
        searchBar.addTextChangedListener { s ->
            filterTasks(s.toString())
        }

        // On item long-click (Delete or Update task)
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedTask = filteredList[position]
            val actualPosition = todoList.indexOf(selectedTask) // Find actual position in original list
            showUpdateDeleteDialog(selectedTask, actualPosition)
            true
        }
    }

    private fun loadTasks(): MutableList<String> {
        val tasksSet = sharedPreferences.getStringSet("tasks", emptySet())
        return tasksSet?.toMutableList() ?: mutableListOf()
    }

    private fun saveTasks() {
        editor.putStringSet("tasks", todoList.toSet())
        editor.apply()
    }

    private fun addTask(task: String) {
        todoList.add(task)
        filterTasks(searchBar.text.toString()) // Update filtered list after adding task
        saveTasks()
    }

    private fun updateTask(oldTask: String, newTask: String, position: Int) {
        todoList[position] = newTask
        filterTasks(searchBar.text.toString()) // Update filtered list after updating task
        saveTasks()
    }

    private fun deleteTask(position: Int) {
        todoList.removeAt(position)
        filterTasks(searchBar.text.toString()) // Update filtered list after deleting task
        saveTasks()
    }

    private fun showUpdateDeleteDialog(task: String, position: Int) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Update or Delete Task")
        val input = EditText(this)
        input.setText(task)

        dialog.setView(input)

        dialog.setPositiveButton("Update") { _, _ ->
            val updatedTask = input.text.toString()
            if (updatedTask.isNotEmpty()) {
                updateTask(task, updatedTask, position)
            }
        }

        dialog.setNegativeButton("Delete") { _, _ ->
            deleteTask(position)
        }

        dialog.create().show()
    }

    private fun filterTasks(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(todoList) // If no search query, show all tasks
        } else {
            for (task in todoList) {
                if (task.contains(query, ignoreCase = true)) {
                    filteredList.add(task) // Add matching tasks to filtered list
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    // Show Time Picker and Schedule Alarm
    private fun showTimePickerDialog(task: String, description: String) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            val timeInMillis = calendar.timeInMillis

            // Schedule the alarm with the selected time
            scheduleAlarm(task, description, timeInMillis)

            addTask("$task: $description") // Add the task with description after setting the reminder
        }, hour, minute, true)

        timePickerDialog.show()
    }

    // Function to schedule alarm
    private fun scheduleAlarm(task: String, description: String, timeInMillis: Long) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("TASK", task)
            putExtra("DESCRIPTION", description)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Schedule the alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        Toast.makeText(this, "Alarm set for task: $task", Toast.LENGTH_SHORT).show()
    }

}
