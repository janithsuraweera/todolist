package com.example.todolist

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("ToDoListPrefs", MODE_PRIVATE)
        val darkModeSwitch: Switch = findViewById(R.id.switchDarkMode)

        // Set the switch state based on saved preferences
        darkModeSwitch.isChecked = sharedPreferences.getBoolean("dark_mode", false)

        darkModeSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("dark_mode", isChecked)
            editor.apply()
            recreate() // Recreate activity to apply changes
        }
    }
}
