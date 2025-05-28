package com.example.apexfitness2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlansActivity : AppCompatActivity() {

    private lateinit var plansRecyclerView: RecyclerView
    private lateinit var addPlanButton: MaterialButton
    private lateinit var backIcon: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var role: String = "Member"
    private var userId: String = ""
    private val plansList = ArrayList<Plan>()
    private lateinit var adapter: PlansAdapter

    // Static data: List of plans
    private val staticPlans = mutableListOf<Plan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plans)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Check authentication state
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("Firebase", "No user signed in, redirecting to LoginActivity")
            startActivity(Intent(this, LoginRegisterActivity::class.java))
            finish()
            return
        }
        userId = currentUser.uid

        // Initialize views
        plansRecyclerView = findViewById(R.id.plansRecyclerView)
        addPlanButton = findViewById(R.id.addPlanButton)
        backIcon = findViewById(R.id.backIcon)

        // Setup RecyclerView
        plansRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PlansAdapter(plansList, role)
        plansRecyclerView.adapter = adapter

        // Initialize static data
        initializeStaticData()

        // Fetch user role from Firestore
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    role = document.getString("role") ?: "Member"
                    Log.d("Firebase", "User role fetched: $role")
                    if (role == "Trainer") {
                        addPlanButton.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("Firebase", "User document does not exist")
                    val userData = hashMapOf("role" to "Member")
                    db.collection("users").document(userId).set(userData)
                        .addOnSuccessListener { Log.d("Firebase", "User document created") }
                        .addOnFailureListener { e -> Log.e("Firebase", "Failed to create user document: ${e.message}") }
                }
                loadPlans() // Load plans after role is confirmed
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to fetch user data: ${e.message}")
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show()
                loadPlans() // Fallback to default role
            }

        // Back button click listener
        backIcon.setOnClickListener {
            finish() // Return to previous activity (DashboardActivity)
        }

        // Add Plan button click listener
        addPlanButton.setOnClickListener {
            showAddPlanDialog()
        }

        // Apply animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        plansRecyclerView.startAnimation(fadeIn)
        addPlanButton.startAnimation(fadeIn)
        backIcon.startAnimation(fadeIn)
    }

    private fun initializeStaticData() {
        // Base timestamp: Current time (07:41 PM PKT, May 25, 2025)
        val now = System.currentTimeMillis() // Current time in milliseconds
        val oneDayMillis = 86400 * 1000L // One day in milliseconds

        // Add sample plans
        if (staticPlans.isEmpty()) {
            staticPlans.addAll(
                listOf(
                    Plan(
                        id = "plan1",
                        title = "Weight Gain",
                        description = "Weight gain krna ha",
                        type = "Workout",
                        assignedToMemberId = "Btg8kJK689N7XwwQ2MqLVGjrv6y2",
                        timestamp = Timestamp((now - 4 * oneDayMillis) / 1000, 0)
                    ),
                    Plan(
                        id = "plan2",
                        title = "Fat Loss Program",
                        description = "Reduce body fat through cardio and diet",
                        type = "Diet",
                        assignedToMemberId = "Btg8kJK689N7XwwQ2MqLVGjrv6y2",
                        timestamp = Timestamp((now - 3 * oneDayMillis) / 1000, 0)
                    ),
                    Plan(
                        id = "plan3",
                        title = "Strength Training",
                        description = "Build muscle with resistance exercises",
                        type = "Workout",
                        assignedToMemberId = "member123",
                        timestamp = Timestamp((now - 2 * oneDayMillis) / 1000, 0)
                    ),
                    Plan(
                        id = "plan4",
                        title = "Flexibility Routine",
                        description = "Improve flexibility with daily stretches",
                        type = "Workout",
                        assignedToMemberId = "Btg8kJK689N7XwwQ2MqLVGjrv6y2",
                        timestamp = Timestamp((now - 1 * oneDayMillis) / 1000, 0)
                    ),
                    Plan(
                        id = "plan5",
                        title = "Balanced Diet Plan",
                        description = "Maintain a balanced diet for health",
                        type = "Diet",
                        assignedToMemberId = "member123",
                        timestamp = Timestamp((now - 5 * oneDayMillis) / 1000, 0)
                    ),
                    Plan(
                        id = "plan6",
                        title = "Endurance Training",
                        description = "Boost stamina with long runs",
                        type = "Workout",
                        assignedToMemberId = "user456",
                        timestamp = Timestamp((now - 6 * oneDayMillis) / 1000, 0)
                    )
                )
            )
        }
    }

    private fun loadPlans() {
        plansList.clear()
        val plansToLoad = if (role == "Member") {
            staticPlans.filter { it.assignedToMemberId == userId }
        } else {
            staticPlans // Trainer sees all plans
        }

        plansList.addAll(plansToLoad)
        adapter.notifyDataSetChanged()
        Log.d("PlansActivity", "Loaded ${plansList.size} plans for role: $role")
    }

    private fun showAddPlanDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_plan, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.planTitleEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.planDescriptionEditText)
        val typeSpinner = dialogView.findViewById<Spinner>(R.id.planTypeSpinner)
        val assignToEditText = dialogView.findViewById<EditText>(R.id.assignToEditText)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Workout", "Diet"))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = spinnerAdapter

        AlertDialog.Builder(this)
            .setTitle("Add New Plan")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val type = typeSpinner.selectedItem.toString()
                val assignedMemberId = assignToEditText.text.toString().trim()

                if (title.isEmpty() || description.isEmpty() || assignedMemberId.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    addPlan(title, description, type, assignedMemberId)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun addPlan(title: String, description: String, type: String, assignedMemberId: String) {
        val newPlan = Plan(
            id = "plan${staticPlans.size + 1}",
            title = title,
            description = description,
            type = type,
            assignedToMemberId = assignedMemberId,
            timestamp = Timestamp.now()
        )

        staticPlans.add(newPlan)
        Toast.makeText(this, "Plan added successfully", Toast.LENGTH_SHORT).show()
        loadPlans()
    }
}