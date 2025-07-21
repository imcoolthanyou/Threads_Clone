package com.appsv.threads.presenatation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.appsv.threads.R
import com.appsv.threads.core.item_View.ThreadItem
import com.appsv.threads.presenatation.ViewModel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavHostController, modifier: Modifier = Modifier) {

    val homeViewModel: HomeViewModel = viewModel()
    val threadsAndUsers = homeViewModel.threadsAndUsers.observeAsState(emptyList())
    val isLoading = homeViewModel.isLoading.observeAsState(false)
    val error = homeViewModel.error.observeAsState(null)
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Threads Logo",
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Black
                    )
                )
            },
            bottomBar = {
                // BottomNavigationBar(navController = navController) // optional
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(Color.Black)
            ) {
                items(threadsAndUsers.value) { pair ->
                    ThreadItem(
                        thread = pair.first,
                        user = pair.second,
                        navController = navController,
                        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    // You can preview with a fake NavHostController if needed
}
