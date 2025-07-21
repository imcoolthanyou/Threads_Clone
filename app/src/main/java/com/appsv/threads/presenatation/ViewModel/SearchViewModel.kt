package com.appsv.threads.presenatation.ViewModel

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appsv.threads.core.model.ThreadModel
import com.appsv.threads.core.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class SearchViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val users = db.getReference("users")

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> get() = _searchQuery

    private var allUsers: List<UserModel> = emptyList()
    private val _users = MutableLiveData<List<UserModel>>(emptyList())
    val usersList: LiveData<List<UserModel>> = _users

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val searchHistoryKey = "search_history"
    private val maxHistorySize = 5

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        fetchUsers()
    }

    private fun fetchUsers() {
        Log.d("SearchViewModel", "Fetching users from Firebase")
        _isLoading.value = true
        users.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("SearchViewModel", "Data received, snapshot count: ${snapshot.childrenCount}")
                val result = mutableListOf<UserModel>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserModel::class.java)?.copy(uid = userSnapshot.key)
                    user?.let {
                        Log.d("SearchViewModel", "User added: ${it.username}, UID: ${it.uid}")
                        result.add(it)
                    }
                }
                allUsers = result
                Log.d("SearchViewModel", "Total users fetched: ${allUsers.size}")
                updateFilteredUsers()
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SearchViewModel", "Firebase error: ${error.message}")
                _error.value = error.message
                _isLoading.value = false
            }
        })
    }

    private fun updateFilteredUsers() {
        val query = _searchQuery.value.orEmpty().trim()
        Log.d("SearchViewModel", "Updating filtered users with query: '$query'")
        val searchHistory = getSearchHistory()
        val filteredList = if (query.isEmpty()) {
            Log.d("SearchViewModel", "Query empty, showing search history: ${searchHistory.size} items")
            searchHistory
        } else {
            val filteredUsers = allUsers.filter { it.username.contains(query, ignoreCase = true) }
            Log.d("SearchViewModel", "Filtered users: ${filteredUsers.size}, combining with history")
            (searchHistory + filteredUsers).distinctBy { it.uid }
        }
        _users.value = filteredList
        Log.d("SearchViewModel", "Updated users list: ${filteredList.size} items")
    }

    fun onSearchQueryChanged(query: String) {
        Log.d("SearchViewModel", "Search query changed: '$query'")
        _searchQuery.value = query
        _isLoading.value = query.isNotEmpty() // Only show loading for non-empty queries
        updateFilteredUsers()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun addToSearchHistory(user: UserModel) {
        if (user.uid == null) {
            Log.w("SearchViewModel", "Cannot add user to history, UID is null")
            return
        }
        Log.d("SearchViewModel", "Adding user to search history: ${user.username}")
        val currentHistory = getSearchHistory().toMutableList()
        currentHistory.removeAll { it.uid == user.uid }
        currentHistory.add(0, user)
        if (currentHistory.size > maxHistorySize) {
            currentHistory.removeLast()
        }
        saveSearchHistory(currentHistory)
        updateFilteredUsers()
    }

    private fun getSearchHistory(): List<UserModel> {
        val json = sharedPreferences.getString(searchHistoryKey, null)
        if (json == null) {
            Log.d("SearchViewModel", "No search history found")
            return emptyList()
        }
        val type = object : TypeToken<List<UserModel>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveSearchHistory(history: List<UserModel>) {
        Log.d("SearchViewModel", "Saving search history: ${history.size} items")
        val json = gson.toJson(history)
        sharedPreferences.edit { putString(searchHistoryKey, json) }
    }

    fun fetchUserFromThread(thread: ThreadModel, onResult: (UserModel) -> Unit) {
        db.getReference("users").child(thread.userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)?.copy(uid = snapshot.key)
                    user?.let {
                        Log.d("SearchViewModel", "Fetched user for thread: ${it.username}")
                        onResult(it)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SearchViewModel", "Thread user fetch error: ${error.message}")
                    _error.value = error.message
                    _isLoading.value = false
                }
            })
    }
}