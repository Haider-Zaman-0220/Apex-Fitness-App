package com.example.apexfitness2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var cardLogout: CardView
    private lateinit var cardAboutApp: CardView
    private lateinit var cardPreferences: CardView
    private lateinit var backIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Check authentication
        if (auth.currentUser == null) {
            Log.e("Firebase", "No user signed in, redirecting to LoginRegisterActivity")
            startActivity(Intent(this, LoginRegisterActivity::class.java))
            finish()
            return
        }

        // Initialize views
        cardLogout = findViewById(R.id.cardLogout)
        cardAboutApp = findViewById(R.id.cardAboutApp)
        cardPreferences = findViewById(R.id.cardPreferences)
        backIcon = findViewById(R.id.backIcon)

        // Apply animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        findViewById<View>(R.id.mainLayout).startAnimation(fadeIn)
        cardLogout.startAnimation(fadeIn)
        cardAboutApp.startAnimation(fadeIn)
        cardPreferences.startAnimation(fadeIn)

        // Back button
        backIcon.setOnClickListener { finish() }

        // Card click listeners
        cardLogout.setOnClickListener {
            Log.d("Settings", "Logout card clicked")
            showLogoutConfirmation()
        }

        cardAboutApp.setOnClickListener {
            Log.d("Settings", "About App card clicked")
            showAboutDialog()
        }

        cardPreferences.setOnClickListener {
            Log.d("Settings", "Preferences card clicked")
            Toast.makeText(this, "Preference feature Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                // Redirect to Login screen
                val intent = Intent(this, LoginRegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Apex Fitness")
            .setMessage("Apex Fitness App\nVersion 1.0\nDeveloped by Apex Team\n\nStay fit and healthy!")
            .setPositiveButton("OK", null)
            .show()
    }
}
