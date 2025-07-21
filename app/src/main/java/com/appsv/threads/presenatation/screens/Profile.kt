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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appsv.threads.core.item_View.ThreadItem
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.appsv.threads.core.model.UserModel
import com.appsv.threads.core.navigation.Routes
import com.appsv.threads.presenatation.ViewModel.AuthViewModel
import com.appsv.threads.presenatation.ViewModel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(navController: NavHostController) {
    val authVM: AuthViewModel = viewModel()
    val homeVM: HomeViewModel = viewModel()
    val firebaseUser by authVM.firebaseUser.observeAsState()
    val threadsAndUsers by homeVM.threadsAndUsers.observeAsState(emptyList())
    val context = LocalContext.current

    // Local states
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf<String?>(null) }

    // Fetch profile data once
    LaunchedEffect(firebaseUser?.uid) {
        firebaseUser?.uid?.let { uid ->
            FirebaseDatabase.getInstance()
                .getReference("users").child(uid)
                .get()
                .addOnSuccessListener { snap ->
                    snap.getValue(UserModel::class.java)?.let { m ->
                        username = m.username
                        profileImageUrl = m.profileImageUrl
                    }
                }
        }
    }

    // Redirect out if logged out
    LaunchedEffect(firebaseUser) {
        if (firebaseUser == null) {
            navController.navigate(Routes.Login.routes) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    firebaseUser?.let { user ->
        // Filter to just this user's posts
        val myPosts = remember(threadsAndUsers, user.uid) {
            threadsAndUsers.filter { it.first.userId == user.uid }
        }

        // Collapse-on-scroll behavior
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
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
                    // Profile header as first item
                    item {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar + Edit
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
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color.DarkGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = Color.Gray)
                                    }
                                }
                                TextButton(onClick = { /* Edit */ }) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit Profile", color = Color.White)
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = username ?: firebaseUser!!.email.orEmpty(),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = firebaseUser!!.email.orEmpty(),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                "${myPosts.size} Posts",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = { authVM.logOut() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                            ) {
                                Icon(Icons.Default.Logout, null, tint = Color.White)
                                Spacer(Modifier.width(4.dp))
                                Text("Log Out", color = Color.White)
                            }

                            Spacer(Modifier.height(8.dp))
                            Divider(color = Color.Gray)
                        }
                    }

                    // Then the user's threads
                    items(myPosts) { (thread, userModel) ->
                        ThreadItem(
                            thread = thread,
                            user = userModel,
                            navController = navController,
                            userId = user.uid
                        )
                    }
                }
            }
        }
    }
}
