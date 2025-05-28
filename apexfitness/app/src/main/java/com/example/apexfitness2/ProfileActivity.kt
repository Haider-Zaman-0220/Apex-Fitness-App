package com.example.apexfitness2

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var roleEditText: EditText
    private lateinit var assignedTrainerLabel: TextView
    private lateinit var assignedTrainerTextView: TextView
    private lateinit var assignedMembersLabel: TextView
    private lateinit var assignedMembersContainer: LinearLayout
    private lateinit var saveProfileButton: Button
    private lateinit var backIcon: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var role: String = "Member"
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        userId = auth.currentUser?.uid ?: ""
        role = intent.getStringExtra("ROLE") ?: "Member"

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        roleEditText = findViewById(R.id.roleEditText)
        assignedTrainerLabel = findViewById(R.id.assignedTrainerLabel)
        assignedTrainerTextView = findViewById(R.id.assignedTrainerTextView)
        assignedMembersLabel = findViewById(R.id.assignedMembersLabel)
        assignedMembersContainer = findViewById(R.id.assignedMembersContainer)
        saveProfileButton = findViewById(R.id.saveProfileButton)
        backIcon = findViewById(R.id.backIcon)
        roleEditText.setText(role)

        // Fetch and display email from FirebaseAuth
        val userEmail = auth.currentUser?.email ?: ""
        emailEditText.setText(userEmail)
        emailEditText.isEnabled = false // Prevent editing email

        loadUserProfile()

        saveProfileButton.setOnClickListener {
            saveUserProfile()
        }

        // Back button
        backIcon.setOnClickListener { finish() }
    }

    private fun loadUserProfile() {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    nameEditText.setText(document.getString("name") ?: "")
                    phoneEditText.setText(document.getString("phone") ?: "")
                    // Email is already set from FirebaseAuth
                    // roleEditText set in onCreate

                    if (role == "Member") {
                        val trainerId = document.getString("assignedTrainerId")
                        if (!trainerId.isNullOrEmpty()) {
                            assignedTrainerLabel.visibility = View.VISIBLE
                            assignedTrainerTextView.visibility = View.VISIBLE
                            // Fetch trainer name
                            db.collection("users").document(trainerId).get()
                                .addOnSuccessListener { trainerDoc ->
                                    val trainerName = trainerDoc.getString("name") ?: "N/A"
                                    assignedTrainerTextView.text = trainerName
                                }
                        }
                    } else if (role == "Trainer") {
                        assignedMembersLabel.visibility = View.VISIBLE
                        assignedMembersContainer.visibility = View.VISIBLE
                        loadAssignedMembers()
                    }
                }
            }
    }

    private fun loadAssignedMembers() {
        assignedMembersContainer.removeAllViews()

        db.collection("users")
            .whereEqualTo("assignedTrainerId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val memberName = doc.getString("name") ?: "Unnamed"
                    val memberView = TextView(this).apply {
                        text = memberName
                        textSize = 16f
                        setPadding(0, 4, 0, 4)
                    }
                    assignedMembersContainer.addView(memberView)
                }
            }
    }

    private fun saveUserProfile() {
        val name = nameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = hashMapOf(
            "name" to name,
            "phone" to phone,
            "email" to (auth.currentUser?.email ?: "") // Ensure email is saved in Firestore
        )

        db.collection("users").document(userId)
            .update(updateData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
}
