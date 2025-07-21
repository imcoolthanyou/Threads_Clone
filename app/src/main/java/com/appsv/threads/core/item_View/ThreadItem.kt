package com.appsv.threads.core.item_View

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appsv.threads.core.model.ThreadModel
import com.appsv.threads.core.model.UserModel
import java.text.SimpleDateFormat
import java.util.*

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun ThreadItem(
    thread: ThreadModel,
    user: UserModel,
    navController: NavHostController,
    userId: String
) {
    // State for like button
    var isLiked by remember { mutableStateOf(false) }
    // State for image expansion
    var showExpandedImage by remember { mutableStateOf(false) }
    // Animation for like button scale
    val likeScale by animateFloatAsState(targetValue = if (isLiked) 1.2f else 1f)
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile image (larger size)
                if (user.profileImageUrl.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        tint = Color.White,
                        modifier = Modifier
                            .size(56.dp) // Increased size
                            .clip(CircleShape)
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.profileImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp) // Increased size
                            .clip(CircleShape),
                        onError = { Log.e("ThreadItem", "Error loading profile image: ${user.profileImageUrl}") }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Thread text
            if (thread.text.isNotBlank()) {
                Text(
                    text = thread.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Thread image (if any)
            thread.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                Log.d("ThreadItem", "Loading image: $imageUrl")
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Thread Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { showExpandedImage = true },
                    onSuccess = { Log.d("ThreadItem", "✅ Image loaded successfully") },
                    onError = { Log.e("ThreadItem", "❌ Error loading image: $imageUrl") }
                )

                // Expanded image dialog
                if (showExpandedImage) {
                    Dialog(onDismissRequest = { showExpandedImage = false }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .clickable { showExpandedImage = false }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Expanded Thread Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.9f) // Full height with some padding
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row {
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        Log.d("ThreadItem", "Like toggled: $isLiked for thread ${thread.userId}")
                    },
                    modifier = Modifier.scale(likeScale)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.White
                    )
                }
                IconButton(onClick = {
                    Log.d("ThreadItem", "Repost clicked for thread ${thread.userId}")
                    // Placeholder for repost logic (e.g., increment repost count or share to feed)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Repeat,
                        contentDescription = "Repost",
                        tint = Color.White
                    )
                }
                IconButton(onClick = {
                    Log.d("ThreadItem", "Share clicked for thread ${thread.userId}")
                    // Placeholder for share logic (e.g., open share intent)

                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "Check out this thread: ${thread.text}")
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Thread"))
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
            }

            // Timestamp
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(thread.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}