package com.example.storyefun.data.models
data class Quest(
    val id: String,
    val type: String,
    val completed: Boolean,
    val progress: Long,
    val requiredProgress: Long,
    val reward: Long,
    val resetTime: com.google.firebase.Timestamp?
)