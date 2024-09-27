package com.example.todolist

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
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

    private var selectedPosition: Int = -1
    private var previouslySelectedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI Components activity main eke
        val editTextTask: EditText = findViewById(R.id.editTextTask)
        val editTextDescription: EditText = findViewById(R.id.editTextDescription)
        val buttonAdd: Button = findViewById(R.id.buttonAdd)
        searchBar = findViewById(R.id.searchBar)
        listView = findViewById(R.id.listView)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Settings Button navigation
        val settingsButton: ImageButton = findViewById(R.id.buttonSettings)
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // SharedPreferences Initialize kiriima
        sharedPreferences = getSharedPreferences("ToDoListPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Load saved tasks
        todoList = loadTasks()
        filteredList = todoList.toMutableList()
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
                editTextDescription.text.clear()
            } else {
                Toast.makeText(this, "Please enter a task and description", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Search Task
        searchBar.addTextChangedListener { s -> filterTasks(s.toString()) }

        // On item click to select a task
        listView.setOnItemClickListener { _, view, position, _ ->
            // Reset the previously selected item background color
            if (previouslySelectedPosition != -1) {
                listView.getChildAt(previouslySelectedPosition)
                    ?.setBackgroundColor(resources.getColor(android.R.color.transparent))
            }

            // Highlight the newly selected task
            selectedPosition = position
            view.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
            previouslySelectedPosition = selectedPosition

            // Show update/delete dialog
            showUpdateDeleteDialog(position)
        }
    }




    //sharedPreferences ekeata data set kiriima
    private fun loadTasks(): MutableList<String> {
        val tasksSet = sharedPreferences.getStringSet("tasks", emptySet())
        return tasksSet?.toMutableList() ?: mutableListOf()
    }


    private fun saveTasks() {
        editor.putStringSet("tasks", todoList.toSet())
        editor.apply()
    }



    //CRUD
    private fun addTask(task: String) {
        todoList.add(task)
        filterTasks(searchBar.text.toString())
        saveTasks()
        updateWidget() // Update the widget when a task is added
    }

    private fun updateTask(oldTask: String, newTask: String, position: Int) {
        todoList[position] = newTask
        filterTasks(searchBar.text.toString())
        saveTasks()
        updateWidget() // Update the widget when a task is updated
    }

    private fun deleteTask(position: Int) {
        todoList.removeAt(position)
        filterTasks(searchBar.text.toString())
        saveTasks()
        updateWidget() // Update the widget when a task is deleted
    }




    //Widget eka auto update wiima
    private fun updateWidget() {
        val intent = Intent(this, TaskWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        sendBroadcast(intent) // Send the broadcast
    }

    private fun filterTasks(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(todoList)
        } else {
            for (task in todoList) {
                if (task.contains(query, ignoreCase = true)) {
                    filteredList.add(task)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }


    //time ekae validation
    private fun showTimePickerDialog(task: String, description: String) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                // Show error if the selected time is in the past
                Toast.makeText(this, "You cannot set a reminder in the past", Toast.LENGTH_SHORT).show()
            } else {
                val timeInMillis = calendar.timeInMillis
                scheduleAlarm(task, description, timeInMillis)

                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                addTask("$task: $description @ $formattedTime")

                // Calculate the time left until the alarm goes off
                val timeLeft = timeInMillis - System.currentTimeMillis()
                val minutesLeft = timeLeft / 60000
                Toast.makeText(this, "Alarm set for $minutesLeft minutes from now", Toast.LENGTH_LONG).show()
            }
        }, currentHour, currentMinute, true)

        timePickerDialog.show()
    }


    //alam eka set
    private fun scheduleAlarm(task: String, description: String, timeInMillis: Long) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("TASK", task)
            putExtra("DESCRIPTION", description)
            putExtra("TIME", timeInMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            todoList.size,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }


    //promt box befor deleting
    private fun showUpdateDeleteDialog(position: Int) {
        val selectedTask = filteredList[position]
        val options = arrayOf("Update", "Delete")

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Select Action")
        dialog.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    // Update Action
                    showUpdateDialog(selectedTask, position)
                }

                1 -> {
                    // Delete Action
                    showConfirmationDialog(position)
                }
            }
        }
        dialog.setNegativeButton("Cancel", null)
        dialog.create().show()
    }


    //promtbot befotr updating
    private fun showUpdateDialog(task: String, position: Int) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Update Task")
        val input = EditText(this)
        input.setText(task)

        dialog.setView(input)

        dialog.setPositiveButton("Update") { _, _ ->
            val updatedTask = input.text.toString()
            if (updatedTask.isNotEmpty()) {
                updateTask(task, updatedTask, position)
            }
        }

        dialog.setNegativeButton("Cancel", null)

        dialog.create().show()
    }


    //delet task befor promtbox
    private fun showConfirmationDialog(position: Int) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Delete Task")
        dialog.setMessage("Are you sure you want to delete this task?")

        dialog.setPositiveButton("Yes") { _, _ ->
            deleteTask(position)
        }

        dialog.setNegativeButton("No", null)

        dialog.create().show()
    }
}
