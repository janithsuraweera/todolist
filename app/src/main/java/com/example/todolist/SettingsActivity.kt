package com.example.todolist

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import android.provider.Settings

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var ringtoneButton: Button
    private lateinit var vibrationSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("ToDoListPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        ringtoneButton = findViewById(R.id.buttonRingtone)
        vibrationSwitch = findViewById(R.id.switchVibration)

        // Load saved vibration state
        val isVibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true)
        vibrationSwitch.isChecked = isVibrationEnabled

        // Set ringtone button click listener
        ringtoneButton.setOnClickListener {
            // Start Ringtone picker
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Tone")
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getSavedRingtoneUri())
            startActivityForResult(intent, 999)
        }

        // Set vibration switch listener
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("vibration_enabled", isChecked)
            editor.apply()
        }
    }

    // Get saved ringtone URI
    private fun getSavedRingtoneUri(): Uri? {
        val ringtoneString = sharedPreferences.getString("ringtone", null)
        return if (ringtoneString != null) Uri.parse(ringtoneString) else Settings.System.DEFAULT_NOTIFICATION_URI
    }

    // Save selected ringtone URI
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 999 && resultCode == RESULT_OK) {
            val ringtoneUri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (ringtoneUri != null) {
                editor.putString("ringtone", ringtoneUri.toString())
                editor.apply()
            }
        }
    }
}
