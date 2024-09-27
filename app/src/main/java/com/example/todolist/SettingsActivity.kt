package com.example.todolist

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.provider.Settings
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var ringtoneButton: Button
    private lateinit var vibrationSwitch: Switch
    private lateinit var saveButton: Button
    private var selectedRingtoneUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ToDoListPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Initialize UI components
        ringtoneButton = findViewById(R.id.buttonRingtone)
        vibrationSwitch = findViewById(R.id.switchVibration)
        saveButton = findViewById(R.id.buttonSaveSettings)

        // Load saved vibration state from SharedPreferences
        val isVibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true)
        vibrationSwitch.isChecked = isVibrationEnabled

        // Load saved ringtone URI
        selectedRingtoneUri = getSavedRingtoneUri()

        // Ringtone button click listener
        ringtoneButton.setOnClickListener {
            // Start Ringtone picker activity
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Tone")
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri)
            startActivityForResult(intent, 999) // Request code for Ringtone Picker
        }

        // Save button click listener
        saveButton.setOnClickListener {
            // Save vibration state
            editor.putBoolean("vibration_enabled", vibrationSwitch.isChecked)

            // Save selected ringtone URI
            if (selectedRingtoneUri != null) {
                editor.putString("ringtone", selectedRingtoneUri.toString())
            }

            editor.apply() // Apply the changes
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }
    }

    // Get saved ringtone URI or return the default notification sound URI
    private fun getSavedRingtoneUri(): Uri? {
        val ringtoneString = sharedPreferences.getString("ringtone", null)
        return if (ringtoneString != null) Uri.parse(ringtoneString) else Settings.System.DEFAULT_NOTIFICATION_URI
    }

    // Handle the result from the Ringtone Picker activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if the result is from the Ringtone Picker and if it's successful
        if (requestCode == 999 && resultCode == RESULT_OK) {
            selectedRingtoneUri = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        }
    }
}
