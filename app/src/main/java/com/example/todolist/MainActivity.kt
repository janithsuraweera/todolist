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

        // Initialize UI Components
        val editTextTask: EditText = findViewById(R.id.editTextTask)
        val buttonAdd: Button = findViewById(R.id.buttonAdd)
        searchBar = findViewById(R.id.searchBar)
        listView = findViewById(R.id.listView)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // SharedPreferences Initialize
        sharedPreferences = getSharedPreferences("ToDoListPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Load saved tasks
        todoList = loadTasks()
        filteredList = todoList.toMutableList() // Initially, filtered list is same as todo list
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filteredList)
        listView.adapter = adapter

        // Add Task with Time Picker for reminder
        buttonAdd.setOnClickListener {
            val task = editTextTask.text.toString()
            if (task.isNotEmpty()) {
                // Show TimePickerDialog to select reminder time
                showTimePickerDialog(task)
                editTextTask.text.clear()
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

    private fun showTimePickerDialog(task: String) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            setReminder(task, calendar.timeInMillis)
            addTask(task) // Add the task after setting the reminder
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun setReminder(task: String, timeInMillis: Long) {
        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra("task", task)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }
}
