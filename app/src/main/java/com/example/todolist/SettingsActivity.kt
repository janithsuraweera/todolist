package com.example.todolist

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ToDoListPrefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Dark Mode Switch
        val darkModeSwitch: Switch = findViewById(R.id.switchDarkMode)
        val isDarkMode = sharedPreferences.getBoolean("darkMode", false)

        darkModeSwitch.isChecked = isDarkMode
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("darkMode", true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("darkMode", false)
            }
            editor.apply()
        }
    }
}
