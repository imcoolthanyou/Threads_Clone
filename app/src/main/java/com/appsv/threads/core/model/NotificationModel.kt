package com.appsv.threads.core.model

data class NotificationModel(
    val id: String = "",
    val type: String = "", // "like", "follow", "comment"
    val fromUserId: String = "",
    val fromUsername: String = "",
    val fromProfileImageUrl: String? = null,
    val threadId: String? = null, // For likes/comments
    val message: String? = null, // For comments
    val timestamp: Long = 0L
)