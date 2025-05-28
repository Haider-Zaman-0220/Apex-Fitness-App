package com.example.apexfitness2

import com.google.firebase.Timestamp

data class Plan(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "", // "Workout" or "Diet"
    val assignedToMemberId: String = "", // member uid
    val timestamp: Timestamp
)