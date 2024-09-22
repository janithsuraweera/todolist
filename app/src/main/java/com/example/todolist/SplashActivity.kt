package com.example.todolist


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay for 3 seconds (3000 milliseconds)
        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to MainActivity after the delay
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the SplashActivity
        }, 3000) // Time in milliseconds (3 seconds)
    }
}
