package com.appsv.threads.presenatation.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appsv.threads.core.item_View.ThreadItem
import com.appsv.threads.core.model.UserModel
import com.appsv.threads.core.navigation.Routes
import com.appsv.threads.presenatation.ViewModel.AuthViewModel
import com.appsv.threads.presenatation.ViewModel.HomeViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUsers(navController: NavHostController, userId: String) {
    val authVM: AuthViewModel = viewModel()
    val homeVM: HomeViewModel = viewModel()
    val firebaseUser by authVM.firebaseUser.observeAsState()
    val threadsAndUsers by homeVM.threadsAndUsers.observeAsState(emptyList())
    val context = LocalContext.current

    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf<String?>(null) }
    var isFollowing by remember { mutableStateOf(false) }

    fun followUser() {
        FirebaseFirestore.getInstance().collection("followers").document(userId)
            .update("followerIds", FieldValue.arrayUnion(firebaseUser?.uid))
            .addOnFailureListener {
                FirebaseFirestore.getInstance().collection("followers").document(userId)
                    .set(mapOf("followerIds" to listOf(firebaseUser?.uid)))
            }
    }

    fun unfollowUser() {
        FirebaseFirestore.getInstance().collection("followers").document(userId)
            .update("followerIds", FieldValue.arrayRemove(firebaseUser?.uid))
    }

    fun checkFollowStatus() {
        FirebaseFirestore.getInstance().collection("followers").document(userId)
            .get().addOnSuccessListener { snap ->
                val list = snap.get("followerIds") as? List<*>
                isFollowing = list?.contains(firebaseUser?.uid) == true
            }
    }

    LaunchedEffect(userId) {
        FirebaseDatabase.getInstance()
            .getReference("users").child(userId)
            .get()
            .addOnSuccessListener { snap ->
                snap.getValue(UserModel::class.java)?.let { m ->
                    username = m.username
                    profileImageUrl = m.profileImageUrl
                }
            }
        checkFollowStatus()
    }

    val userThreads = remember(threadsAndUsers, userId, isFollowing) {
        if (isFollowing) threadsAndUsers.filter { it.first.userId == userId } else emptyList()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        topBar = {
            LargeTopAppBar(
                title = { Text("Profile", color = Color.White) },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Black),
                scrollBehavior = scrollBehavior
            )
        }
    ) { inner ->
        Box(
            Modifier
                .padding(inner)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            LazyColumn {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!profileImageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(profileImageUrl)
                                        .crossfade(true).build(),
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(80.dp).clip(CircleShape)
                                )
                            } else {
                                Box(
                                    Modifier.size(80.dp).clip(CircleShape).background(Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Edit, null, tint = Color.Gray)
                                }
                            }
                            Button(onClick = {
                                if (isFollowing) {
                                    unfollowUser()
                                } else {
                                    followUser()
                                }
                                isFollowing = !isFollowing
                            }) {
                                Text(if (isFollowing) "Unfollow" else "Follow")
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(text = username ?: userId, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))
                        Divider(color = Color.Gray)
                    }
                }
                items(userThreads) { (thread, userModel) ->
                    ThreadItem(
                        thread = thread,
                        user = userModel,
                        navController = navController,
                        userId = userId
                    )
                }
            }
        }
    }
}