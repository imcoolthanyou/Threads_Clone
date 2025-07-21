package com.appsv.threads.presenatation.ViewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import com.appsv.threads.core.model.ThreadModel

class AddThreadViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val threadsRef = db.getReference("threads")

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isPosted = MutableLiveData(false)
    val isPosted: LiveData<Boolean> get() = _isPosted

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    /**
     * Posts a new thread. If imageUri is provided, upload to Cloudinary first.
     */
    fun postThread(text: String, imageUri: Uri?) {
        val user = auth.currentUser
        if (user == null) {
            _error.value = "User not logged in"
            return
        }
        _isLoading.value = true
        _error.value = null
        val userId = user.uid
        val threadId = threadsRef.push().key ?: UUID.randomUUID().toString()

        if (imageUri != null) {
            uploadToCloudinary(userId, threadId, text, imageUri)
        } else {
            saveThreadToFirebase(userId, threadId, text, null)
        }
    }

    private fun uploadToCloudinary(
        userId: String,
        threadId: String,
        text: String,
        imageUri: Uri
    ) {
        val publicId = "threads/$userId/$threadId"
        MediaManager.get().upload(imageUri)
            .option("public_id", publicId)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    saveThreadToFirebase(userId, threadId, text, url)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    _isLoading.value = false
                    _error.value = "Image upload failed: ${error.description}"
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun saveThreadToFirebase(
        userId: String,
        threadId: String,
        text: String,
        imageUrl: String?
    ) {
        val thread = ThreadModel(
            id = threadId,
            userId = userId,
            text = text,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        threadsRef.child(threadId).setValue(thread)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _isPosted.value = true
                } else {
                    _error.value = task.exception?.message
                }
            }
    }

    /**
     * Reset isPosted flag after UI navigation.
     */
    fun resetPosted() {
        _isPosted.value = false
    }
}
