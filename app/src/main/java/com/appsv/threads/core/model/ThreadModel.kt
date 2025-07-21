package com.appsv.threads.core.model

// Data class for a thread post
data class ThreadModel(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L
)
