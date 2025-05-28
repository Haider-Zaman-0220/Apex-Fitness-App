package com.example.apexfitness2


data class Message(
    val senderId: String = "",
    val message: String = "",
    val timestamp: com.google.firebase.Timestamp? = null

)
