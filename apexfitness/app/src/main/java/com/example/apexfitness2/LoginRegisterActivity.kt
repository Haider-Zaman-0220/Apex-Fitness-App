package com.example.apexfitness2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var roleRadioGroup: RadioGroup
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        roleRadioGroup = findViewById(R.id.roleRadioGroup)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val selectedRoleId = roleRadioGroup.checkedRadioButtonId
            val role = if (selectedRoleId == R.id.memberRadioButton) "Member" else "Trainer"

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password, role)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun registerUser(email: String, password: String, role: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    // Save role in Firestore
                    val userMap = hashMapOf("role" to role)
                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            Log.d("Firebase", "User role saved for UID: $userId, Role: $role")
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            navigateToDashboard(role)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Failed to save user role: ${e.message}")
                            Toast.makeText(this, "Failed to save user role", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("Firebase", "Registration failed: ${task.exception?.message}")
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val role = document.getString("role") ?: "Member"
                                Log.d("Firebase", "User logged in: UID: $userId, Role: $role")
                                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                navigateToDashboard(role)
                            } else {
                                Log.e("Firebase", "User role not found for UID: $userId")
                                Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Failed to fetch user role: ${e.message}")
                            Toast.makeText(this, "Failed to fetch user role", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    Log.e("Firebase", "Login failed: $errorMessage")
                    Toast.makeText(this, "Login failed: $errorMessage", Toast.LENGTH_LONG).show()
                    task.exception?.printStackTrace()
                }
            }
    }

    private fun navigateToDashboard(role: String) {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("ROLE", role)
        startActivity(intent)
        finish()
    }
}