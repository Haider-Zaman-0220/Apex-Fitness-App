package com.example.apexfitness2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class ChatActivity : AppCompatActivity() {

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var chatId: String
    private lateinit var currentUserId: String
    private lateinit var chatPartnerId: String

    private val messagesList = mutableListOf<Message>()
    private lateinit var adapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        currentUserId = auth.currentUser?.uid ?: ""

        // Get chatPartnerId from intent extras (passed when opening chat)
        chatPartnerId = intent.getStringExtra("CHAT_PARTNER_ID") ?: ""

        if (chatPartnerId.isEmpty()) {
            Toast.makeText(this, "Chat partner ID missing", Toast.LENGTH_SHORT).show()
            finish()  // Close activity if no partnerId passed
            return
        }

        chatId = generateChatId(currentUserId, chatPartnerId)

        adapter = MessagesAdapter(messagesList)
        messagesRecyclerView.adapter = adapter
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)

        listenForMessages()

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun generateChatId(user1: String, user2: String): String {
        // Ensures consistent chat ID irrespective of sender/receiver order
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    private fun listenForMessages() {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                if (snapshots != null) {
                    messagesList.clear()
                    for (doc in snapshots.documents) {
                        val message = doc.toObject(Message::class.java)
                        if (message != null) messagesList.add(message)
                    }
                    adapter.notifyDataSetChanged()
                    messagesRecyclerView.scrollToPosition(messagesList.size - 1)
                }
            }
    }

    private fun sendMessage() {
        val msg = messageEditText.text.toString().trim()
        if (msg.isEmpty()) return

        val message = Message(
            senderId = currentUserId,
            message = msg,
            timestamp = Timestamp.now()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                messageEditText.text.clear()
            }
    }
}
