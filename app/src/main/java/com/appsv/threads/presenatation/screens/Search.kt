package com.appsv.threads.presenatation.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.appsv.threads.core.item_View.UserItem
import com.appsv.threads.core.model.UserModel
import com.appsv.threads.presenatation.ViewModel.SearchViewModel
import com.google.firebase.database.FirebaseDatabase

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun Search(navController: NavHostController) {
    val searchViewModel: SearchViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        searchViewModel.initialize(context)
    }

    val usersList = searchViewModel.usersList.observeAsState(emptyList())
    val isLoading = searchViewModel.isLoading.observeAsState(false)
    val searchQuery = searchViewModel.searchQuery.observeAsState("")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TextField(
            value = searchQuery.value,
            onValueChange = { searchViewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Search users", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedContainerColor = Color.DarkGray,
                unfocusedContainerColor = Color.DarkGray
            )
        )

        when {
            isLoading.value -> {
                Column(modifier = Modifier.padding(5.dp)) {
                    repeat(5) {
                        ShimmerUserItem()
                    }
                }
            }
            usersList.value.isEmpty() -> {
                Text(
                    text = if (searchQuery.value.isEmpty()) "Recent searches" else "No results found",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .background(Color.Black)
                ) {
                    itemsIndexed(usersList.value) { index, user ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 50 * (index + 1) })
                        ) {
                            UserItem(
                                user = user,
                                navController = navController,
                                onClick = { searchViewModel.addToSearchHistory(user) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShimmerUserItem() {
    val infiniteTransition = rememberInfiniteTransition()
    val offset = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.LightGray, Color.Gray, Color.LightGray),
                                start = Offset(offset.value * 1000, 0f),
                                end = Offset(offset.value * 1000 + 1000, 0f)
                            )
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(100.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.LightGray, Color.Gray, Color.LightGray),
                                start = Offset(offset.value * 1000, 0f),
                                end = Offset(offset.value * 1000 + 1000, 0f)
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(200.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.LightGray, Color.Gray, Color.LightGray),
                            start = Offset(offset.value * 1000, 0f),
                            end = Offset(offset.value * 1000 + 1000, 0f)
                        )
                    )
            )
        }
    }
}
