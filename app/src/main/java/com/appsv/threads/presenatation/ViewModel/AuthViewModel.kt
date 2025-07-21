package com.appsv.threads.presenatation.ViewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.appsv.threads.core.model.UserModel
import com.appsv.threads.core.utils.SharedPrep
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val usersRef = db.getReference("users")

    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    val firebaseUser: LiveData<FirebaseUser?> = _firebaseUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    init {
        _firebaseUser.value = auth.currentUser
    }

    fun login(email: String, password: String, context: Context) {
        _isLoading.value = true
        _error.value = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        getData(it.uid, context) {
                            _firebaseUser.value = user
                            _loginSuccess.value = true
                            _isLoading.value = false
                        }
                    } ?: run {
                        _isLoading.value = false
                        _error.value = "Login failed: user is null"
                        _loginSuccess.value = false
                    }
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message
                    _loginSuccess.value = false
                }
            }
    }

    private fun getData(uid: String, context: Context, onComplete: () -> Unit) {
        usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserModel::class.java)
                userData?.let {
                    SharedPrep.storeData(
                        it.username,
                        it.email,
                        it.password,
                        it.profileImageUrl,
                        context
                    )
                }
                onComplete()
            }

            override fun onCancelled(error: DatabaseError) {
                _error.value = error.message
                _isLoading.value = false
            }
        })
    }

    fun register(
        userName: String,
        email: String,
        password: String,
        profileImageUri: Uri,
        context: Context
    ) {
        _isLoading.value = true
        _error.value = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (!authTask.isSuccessful) {
                    _error.value = authTask.exception?.message
                    _isLoading.value = false
                    return@addOnCompleteListener
                }

                val uid = auth.currentUser?.uid
                if (uid.isNullOrEmpty()) {
                    _error.value = "User ID is null"
                    _isLoading.value = false
                    return@addOnCompleteListener
                }

                uploadToCloudinary(uid, userName, email, password, profileImageUri, context)
            }
    }

    private fun uploadToCloudinary(
        uid: String,
        userName: String,
        email: String,
        password: String,
        uri: Uri,
        context: Context
    ) {
        val publicId = "users/$uid/${System.currentTimeMillis()}"

        MediaManager.get().upload(uri)
            .option("public_id", publicId)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as? String ?: ""
                    saveUserToFirebase(uid, userName, email, password, imageUrl, context)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    _isLoading.value = false
                    _error.value = "Upload failed: ${error.description}"
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun saveUserToFirebase(
        uid: String,
        userName: String,
        email: String,
        password: String,
        imageUrl: String,
        context: Context
    ) {
        val user = UserModel(
            username = userName,
            email = email,
            password = password,
            profileImageUrl = imageUrl,
            uid = uid
        )

        usersRef.child(uid).setValue(user)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    SharedPrep.storeData(userName, email, password, imageUrl, context)
                    _registerSuccess.value = true
                } else {
                    _error.value = task.exception?.message
                }
            }
    }

    fun resetRegisterSuccess() {
        _registerSuccess.value = false
    }

    fun resetLoginSuccess() {
        _loginSuccess.value = false
    }

    fun logOut() {
        auth.signOut()
        _firebaseUser.postValue(null)
    }
}
