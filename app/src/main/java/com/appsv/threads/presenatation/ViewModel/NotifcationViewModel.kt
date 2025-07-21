package com.appsv.threads.presenatation.ViewModel



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appsv.threads.core.model.NotificationModel
import com.appsv.threads.core.model.ThreadModel
import com.appsv.threads.core.model.UserModel
import java.util.concurrent.TimeUnit

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableLiveData<List<NotificationModel>>(emptyList())
    val notifications: LiveData<List<NotificationModel>> get() = _notifications

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    init {
        fetchMockNotifications()
    }

    private fun fetchMockNotifications() {
        _isLoading.value = true

        // Mock users (similar to HomeViewModel's UserModel usage)
        val mockUsers = listOf(
            UserModel(
                username = "john_doe",
                email = "john@example.com",
                password = "password123",
                profileImageUrl = "https://example.com/john.jpg",
                uid = "user1"
            ),
            UserModel(
                username = "jane_smith",
                email = "jane@example.com",
                password = "password456",
                profileImageUrl = "https://example.com/jane.jpg",
                uid = "user2"
            ),
            UserModel(
                username = "alice_wonder",
                email = "alice@example.com",
                password = "password789",
                profileImageUrl = "",
                uid = "user3"
            )
        )

        // Mock threads (similar to HomeViewModel's ThreadModel usage)
        val mockThreads = listOf(
            ThreadModel(
                text = "Enjoying the sunny day! 🌞",
                imageUrl = "https://example.com/thread1.jpg",
                timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5),
                userId = "user1"
            ),
            ThreadModel(
                text = "Just finished a great book!",
                imageUrl = null,
                timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2),
                userId = "user2"
            )
        )

        // Mock notifications
        val mockNotifications = listOf(
            NotificationModel(
                id = "notif1",
                type = "like",
                fromUserId = mockUsers[1].uid!!,
                fromUsername = mockUsers[1].username,
                fromProfileImageUrl = mockUsers[1].profileImageUrl,
                threadId = mockThreads[0].userId,
                timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)
            ),
            NotificationModel(
                id = "notif2",
                type = "follow",
                fromUserId = mockUsers[2].uid!!,
                fromUsername = mockUsers[2].username,
                fromProfileImageUrl = mockUsers[2].profileImageUrl,
                timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30)
            ),
            NotificationModel(
                id = "notif3",
                type = "comment",
                fromUserId = mockUsers[0].uid!!,
                fromUsername = mockUsers[0].username,
                fromProfileImageUrl = mockUsers[0].profileImageUrl,
                threadId = mockThreads[1].userId,
                message = "Great post!",
                timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10)
            )
        )

        // Simulate delay to show shimmer effect
        Thread.sleep(1000) // Simulate network delay (remove in production)
        _notifications.value = mockNotifications.sortedByDescending { it.timestamp }
        _isLoading.value = false
    }
}