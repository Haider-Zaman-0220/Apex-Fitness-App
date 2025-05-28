package com.example.apexfitness2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Progress(
    val weight: Float = 0f,
    val bodyFat: Float = 0f,
    val workoutsDone: Int = 0,
    val timestamp: Timestamp? = null
)

class ProgressTrackerActivity : AppCompatActivity() {

    private lateinit var weightEditText: EditText
    private lateinit var bodyFatEditText: EditText
    private lateinit var workoutsDoneEditText: EditText
    private lateinit var saveProgressButton: Button
    private lateinit var backIcon: ImageView

    private lateinit var progressTitleTextView: TextView
    private lateinit var progressRecyclerView: RecyclerView

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var userRole: String
    private lateinit var currentUserId: String
    private var assignedMemberId: String? = null

    // Static data: Map of user IDs to their progress lists
    private val progressData = mutableMapOf<String, MutableList<Progress>>()

    private val progressList = mutableListOf<Progress>()
    private lateinit var progressAdapter: ProgressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_tracker)

        weightEditText = findViewById(R.id.weightEditText)
        bodyFatEditText = findViewById(R.id.bodyFatEditText)
        workoutsDoneEditText = findViewById(R.id.workoutsDoneEditText)
        saveProgressButton = findViewById(R.id.saveProgressButton)

        progressTitleTextView = findViewById(R.id.progressTitleTextView)
        progressRecyclerView = findViewById(R.id.progressRecyclerView)
        backIcon = findViewById(R.id.backIcon)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        // Get user role from intent
        userRole = intent.getStringExtra("USER_ROLE")?.lowercase() ?: "member"
        assignedMemberId = intent.getStringExtra("MEMBER_ID")

        Log.d("ProgressTracker", "User Role: $userRole, Current User ID: $currentUserId, Assigned Member ID: $assignedMemberId")

        // Initialize static data if not already present
        initializeStaticData()

        progressAdapter = ProgressAdapter(progressList)
        progressRecyclerView.adapter = progressAdapter
        progressRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize progress list based on role
        if (userRole == "member") {
            setupMemberUI()
            loadProgress(currentUserId)
        } else if (userRole == "trainer") {
            setupTrainerUI()
            val memberId = assignedMemberId ?: "member123" // Fallback for testing
            loadProgress(memberId)
        } else {
            Log.e("ProgressTracker", "Invalid user role: $userRole")
            Toast.makeText(this, "Invalid user role", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Back button
        backIcon.setOnClickListener { finish() }
    }

    private fun initializeStaticData() {
        // Base timestamp: Current time (07:29 PM PKT, May 25, 2025)
        val now = System.currentTimeMillis() // Current time in milliseconds
        val oneDayMillis = 86400 * 1000L // One day in milliseconds

        // Initialize data for a default user ("user123") if not present
        if (!progressData.containsKey("user123")) {
            progressData["user123"] = mutableListOf(
                Progress(weight = 71.5f, bodyFat = 15.5f, workoutsDone = 2, timestamp = Timestamp((now - 4 * oneDayMillis) / 1000, 0)),
                Progress(weight = 71.2f, bodyFat = 15.4f, workoutsDone = 3, timestamp = Timestamp((now - 3 * oneDayMillis) / 1000, 0)),
                Progress(weight = 70.5f, bodyFat = 15.2f, workoutsDone = 3, timestamp = Timestamp((now - 2 * oneDayMillis) / 1000, 0)),
                Progress(weight = 71.0f, bodyFat = 15.0f, workoutsDone = 4, timestamp = Timestamp((now - oneDayMillis) / 1000, 0)),
                Progress(weight = 70.8f, bodyFat = 14.8f, workoutsDone = 5, timestamp = Timestamp(now / 1000, 0))
            )
        }

        // Initialize data for a default member ("member123") if not present
        if (!progressData.containsKey("member123")) {
            progressData["member123"] = mutableListOf(
                Progress(weight = 65.5f, bodyFat = 18.5f, workoutsDone = 1, timestamp = Timestamp((now - 4 * oneDayMillis) / 1000, 0)),
                Progress(weight = 65.3f, bodyFat = 18.2f, workoutsDone = 2, timestamp = Timestamp((now - 3 * oneDayMillis) / 1000, 0)),
                Progress(weight = 65.0f, bodyFat = 18.0f, workoutsDone = 2, timestamp = Timestamp((now - 2 * oneDayMillis) / 1000, 0)),
                Progress(weight = 64.5f, bodyFat = 17.8f, workoutsDone = 3, timestamp = Timestamp((now - oneDayMillis) / 1000, 0)),
                Progress(weight = 64.0f, bodyFat = 17.5f, workoutsDone = 4, timestamp = Timestamp(now / 1000, 0))
            )
        }

        // Ensure the current user's ID has an entry
        if (!progressData.containsKey(currentUserId)) {
            progressData[currentUserId] = mutableListOf(
                Progress(weight = 68.5f, bodyFat = 16.5f, workoutsDone = 2, timestamp = Timestamp((now - 4 * oneDayMillis) / 1000, 0)),
                Progress(weight = 68.2f, bodyFat = 16.3f, workoutsDone = 3, timestamp = Timestamp((now - 3 * oneDayMillis) / 1000, 0)),
                Progress(weight = 68.0f, bodyFat = 16.0f, workoutsDone = 3, timestamp = Timestamp((now - 2 * oneDayMillis) / 1000, 0)),
                Progress(weight = 67.5f, bodyFat = 15.8f, workoutsDone = 4, timestamp = Timestamp((now - oneDayMillis) / 1000, 0)),
                Progress(weight = 67.0f, bodyFat = 15.5f, workoutsDone = 5, timestamp = Timestamp(now / 1000, 0))
            )
        }

        // Ensure the assigned member ID has an entry (for trainers)
        assignedMemberId?.let { memberId ->
            if (!progressData.containsKey(memberId)) {
                progressData[memberId] = mutableListOf(
                    Progress(weight = 66.5f, bodyFat = 17.5f, workoutsDone = 1, timestamp = Timestamp((now - 4 * oneDayMillis) / 1000, 0)),
                    Progress(weight = 66.2f, bodyFat = 17.3f, workoutsDone = 2, timestamp = Timestamp((now - 3 * oneDayMillis) / 1000, 0)),
                    Progress(weight = 66.0f, bodyFat = 17.0f, workoutsDone = 2, timestamp = Timestamp((now - 2 * oneDayMillis) / 1000, 0)),
                    Progress(weight = 65.5f, bodyFat = 16.8f, workoutsDone = 3, timestamp = Timestamp((now - oneDayMillis) / 1000, 0)),
                    Progress(weight = 65.0f, bodyFat = 16.5f, workoutsDone = 4, timestamp = Timestamp(now / 1000, 0))
                )
            }
        }
    }

    private fun setupMemberUI() {
        Log.d("ProgressTracker", "Setting up Member UI - Input fields should be visible")
        weightEditText.visibility = View.VISIBLE
        bodyFatEditText.visibility = View.VISIBLE
        workoutsDoneEditText.visibility = View.VISIBLE
        saveProgressButton.visibility = View.VISIBLE
        progressTitleTextView.visibility = View.VISIBLE
        progressRecyclerView.visibility = View.VISIBLE

        saveProgressButton.setOnClickListener {
            saveProgress()
        }
    }

    private fun setupTrainerUI() {
        Log.d("ProgressTracker", "Setting up Trainer UI - Input fields should be hidden")
        weightEditText.visibility = View.GONE
        bodyFatEditText.visibility = View.GONE
        workoutsDoneEditText.visibility = View.GONE
        saveProgressButton.visibility = View.GONE
        progressTitleTextView.visibility = View.VISIBLE
        progressRecyclerView.visibility = View.VISIBLE
    }

    private fun saveProgress() {
        val weightStr = weightEditText.text.toString()
        val bodyFatStr = bodyFatEditText.text.toString()
        val workoutsDoneStr = workoutsDoneEditText.text.toString()

        if (weightStr.isEmpty() || bodyFatStr.isEmpty() || workoutsDoneStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val progress = Progress(
            weight = weightStr.toFloatOrNull() ?: 0f,
            bodyFat = bodyFatStr.toFloatOrNull() ?: 0f,
            workoutsDone = workoutsDoneStr.toIntOrNull() ?: 0,
            timestamp = Timestamp.now()
        )

        if (progress.weight == 0f || progress.bodyFat == 0f || progress.workoutsDone == 0) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        // Add to static data for the current user
        val userProgressList = progressData.getOrPut(currentUserId) { mutableListOf() }
        userProgressList.add(0, progress) // Add to top (newest first)
        loadProgress(currentUserId) // Refresh the displayed list

        Toast.makeText(this, "Progress saved!", Toast.LENGTH_SHORT).show()
        weightEditText.text.clear()
        bodyFatEditText.text.clear()
        workoutsDoneEditText.text.clear()
    }

    private fun loadProgress(userId: String) {
        progressList.clear()
        val userProgress = progressData[userId] ?: mutableListOf()
        progressList.addAll(userProgress)
        Log.d("ProgressTracker", "Loaded ${progressList.size} entries for user: $userId")
        progressAdapter.notifyDataSetChanged()
    }
}
