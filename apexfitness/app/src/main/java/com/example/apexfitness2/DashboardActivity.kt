package com.example.apexfitness2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var role: String = "Member"
    private var assignedMemberId: String? = null // For trainers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase
        try {
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()
            Log.d("Firebase", "Firebase initialized: Auth=$auth, Firestore=$db")
        } catch (e: Exception) {
            Log.e("Firebase", "Firebase initialization failed: ${e.message}")
            Toast.makeText(this, "Error initializing Firebase", Toast.LENGTH_SHORT).show()
            return
        }

        // Check authentication state
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("Firebase", "No user signed in, redirecting to LoginActivity")
            startActivity(Intent(this, LoginRegisterActivity::class.java))
            finish()
            return
        }
        Log.d("Firebase", "User signed in: ${currentUser.uid}, Email: ${currentUser.email}")

        // Initialize views
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val cardHealthCalculators = findViewById<CardView>(R.id.cardHealthCalculators)
        val cardViewPlans = findViewById<CardView>(R.id.cardViewPlans)
        val cardTrackProgress = findViewById<CardView>(R.id.cardTrackProgress)
        val cardProfile = findViewById<CardView>(R.id.cardProfile)
        val cardSettings = findViewById<CardView>(R.id.cardSettings)
        val profileAvatar = findViewById<ImageView>(R.id.profileAvatar)
        val messageIcon = findViewById<ImageView>(R.id.messageIcon)
        val notificationIcon = findViewById<ImageView>(R.id.notificationIcon)

        // Fetch user data from Firestore
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    role = document.getString("role") ?: "Member"
                    Log.d("Firebase", "User data fetched: role=$role")
                    welcomeText.text = "Welcome, $role"
                    // Update CardView text based on role
                    if (role == "Trainer") {
                        findViewById<TextView>(R.id.progressText).text = "View Member Progress"
                        findViewById<TextView>(R.id.plansText).text = "Create/Assign Plans"
                        // Fetch a member ID for the trainer (simplified for static data)
                        assignedMemberId = "member123" // Hardcoded for testing
                        Log.d("Dashboard", "Assigned MEMBER_ID for trainer: $assignedMemberId")
                    } else {
                        findViewById<TextView>(R.id.progressText).text = "Track My Progress"
                        findViewById<TextView>(R.id.plansText).text = "View My Plans"
                    }
                } else {
                    Log.e("Firebase", "User document does not exist")
                    welcomeText.text = "Welcome, Member"
                    // Create default user document
                    val userData = hashMapOf("role" to "Member")
                    db.collection("users").document(currentUser.uid).set(userData)
                        .addOnSuccessListener { Log.d("Firebase", "User document created") }
                        .addOnFailureListener { e -> Log.e("Firebase", "Failed to create user document: ${e.message}") }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to fetch user data: ${e.message}")
                welcomeText.text = "Welcome, Member"
                Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // Apply animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        cardHealthCalculators.startAnimation(fadeIn)
        cardViewPlans.startAnimation(fadeIn)
        cardTrackProgress.startAnimation(fadeIn)
        cardProfile.startAnimation(fadeIn)
        cardSettings.startAnimation(fadeIn)

        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_animation)
        profileAvatar.startAnimation(scaleAnimation)

        // Set click listeners for CardViews
        cardHealthCalculators.setOnClickListener {
            startActivity(Intent(this, HealthCalculatorsActivity::class.java))
        }

        cardViewPlans.setOnClickListener {
            val intent = Intent(this, PlansActivity::class.java)
            intent.putExtra("ROLE", role)
            startActivity(intent)
        }

        cardTrackProgress.setOnClickListener {
            val intent = Intent(this, ProgressTrackerActivity::class.java)
            intent.putExtra("USER_ROLE", role) // Match the key expected by ProgressTrackerActivity
            if (role == "Trainer" && assignedMemberId != null) {
                intent.putExtra("MEMBER_ID", assignedMemberId)
            }
            startActivity(intent)
        }

        cardProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("ROLE", role)
            startActivity(intent)
        }

        cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Message icon click listener
        messageIcon.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("ROLE", role)
            startActivity(intent)
        }

        // Notification icon (placeholder for future implementation)
        notificationIcon.setOnClickListener {
            // TODO: Implement notification activity
            Toast.makeText(this, "Notifications feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
