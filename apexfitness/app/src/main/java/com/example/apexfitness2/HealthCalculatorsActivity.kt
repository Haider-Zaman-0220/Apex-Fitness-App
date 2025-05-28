package com.example.apexfitness2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar

import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HealthCalculatorsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var weightInputLayout: TextInputLayout
    private lateinit var heightInputLayout: TextInputLayout
    private lateinit var ageInputLayout: TextInputLayout
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var calculateButton: Button
    private lateinit var bmiResultTextView: TextView
    private lateinit var bmrResultTextView: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var errorText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_calculators)

        // Initialize Firebase (requires google-services.json in app/)
        try {
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()
            Log.d("Firebase", "Firebase initialized: Auth=$auth, Firestore=$db")
        } catch (e: Exception) {
            Log.e("Firebase", "Firebase initialization failed: ${e.message}")
            showError("Firebase initialization failed: ${e.message}")
            return
        }

        // Check authentication state
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("Firebase", "No user signed in, redirecting to LoginActivity")
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginRegisterActivity::class.java))
            finish()
            return
        }
        Log.d("Firebase", "User signed in: UID=${currentUser.uid}, Email=${currentUser.email}")

        // Initialize views
        weightInputLayout = findViewById(R.id.weightInputLayout)
        heightInputLayout = findViewById(R.id.heightInputLayout)
        ageInputLayout = findViewById(R.id.ageInputLayout)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        calculateButton = findViewById(R.id.calculateButton)
        bmiResultTextView = findViewById(R.id.bmiResultTextView)
        bmrResultTextView = findViewById(R.id.bmrResultTextView)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        errorText = findViewById(R.id.errorText)
        val backIcon = findViewById<ImageView>(R.id.backIcon)

        // Set click listeners
        backIcon.setOnClickListener { finish() }
        calculateButton.setOnClickListener { calculateAndSaveBMIAndBMR() }
    }

    private fun calculateAndSaveBMIAndBMR() {
        val weightStr = weightInputLayout.editText?.text.toString()
        val heightStr = heightInputLayout.editText?.text.toString()
        val ageStr = ageInputLayout.editText?.text.toString()
        val selectedGenderId = genderRadioGroup.checkedRadioButtonId

        // Reset error states
        weightInputLayout.error = null
        heightInputLayout.error = null
        ageInputLayout.error = null
        errorText.visibility = View.GONE

        // Validate inputs
        if (weightStr.isEmpty()) {
            weightInputLayout.error = "Enter weight"
            return
        }
        if (heightStr.isEmpty()) {
            heightInputLayout.error = "Enter height"
            return
        }
        if (ageStr.isEmpty()) {
            ageInputLayout.error = "Enter age"
            return
        }
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toFloatOrNull()
        val heightCm = heightStr.toFloatOrNull()
        val age = ageStr.toIntOrNull()

        // Validate ranges
        if (weight == null || weight <= 0) {
            weightInputLayout.error = "Invalid weight"
            return
        }
        if (heightCm == null || heightCm < 50) {
            heightInputLayout.error = "Height must be at least 50 cm"
            return
        }
        if (age == null || age < 1 || age > 120) {
            ageInputLayout.error = "Invalid age (1-120)"
            return
        }

        loadingIndicator.visibility = View.VISIBLE

        val heightM = heightCm / 100
        val bmi = weight / (heightM * heightM)
        val bmr = when (selectedGenderId) {
            R.id.maleRadioButton -> (10 * weight) + (6.25 * heightCm) - (5 * age) + 5
            R.id.femaleRadioButton -> (10 * weight) + (6.25 * heightCm) - (5 * age) - 161
            else -> 0.0
        }

        bmiResultTextView.text = "BMI: %.2f".format(bmi)
        bmrResultTextView.text = "BMR: %.2f kcal/day".format(bmr)

        // Save to Firebase
        val userId = auth.currentUser?.uid ?: return
        val resultData = hashMapOf(
            "bmi" to bmi,
            "bmr" to bmr,
            "weight" to weight,
            "height" to heightCm,
            "age" to age,
            "gender" to (if (selectedGenderId == R.id.maleRadioButton) "Male" else "Female"),
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId).collection("health_results")
            .add(resultData)
            .addOnSuccessListener {
                Log.d("Firebase", "Health results saved")
                loadingIndicator.visibility = View.GONE
                Toast.makeText(this, "Results saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to save health results: ${e.message}")
                loadingIndicator.visibility = View.GONE
                showError("Failed to save results: ${e.message}")
            }
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
