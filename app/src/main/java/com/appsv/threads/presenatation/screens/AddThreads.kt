package com.appsv.threads.presenatation.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter


import com.appsv.threads.core.navigation.Routes

import com.appsv.threads.presenatation.ViewModel.AddThreadViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddThreads(navController: NavHostController) {
    val context = LocalContext.current

    // States for user inputs
    var text by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var capturedPhoto by remember { mutableStateOf<Uri?>(null) }
    var selectedGif by remember { mutableStateOf<Uri?>(null) }
    var recordedAudio by remember { mutableStateOf<Uri?>(null) }
    var locationInfo by remember { mutableStateOf<String?>(null) }
    val cameraUri = remember { mutableStateOf<Uri?>(null) }

    //viewModel
    val threadViewModel: AddThreadViewModel= viewModel()
    val isPosted by threadViewModel.isPosted.observeAsState()

    // Launchers
    val galleryLauncher = rememberLauncherForActivityResult(GetContent()) {
        it?.let { selectedImage = it }
    }
    val cameraLauncher = rememberLauncherForActivityResult(TakePicture()) { ok ->
        if (ok) cameraUri.value?.let { capturedPhoto = it }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            val photoFile = File(context.cacheDir, "thread_photo.jpg")
                .apply { if (!exists()) createNewFile() }
            val photoFileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
            )
            cameraUri.value = photoFileUri
            cameraLauncher.launch(photoFileUri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isPosted) {
        if (isPosted!!){
            text=""
            selectedImage=null
            capturedPhoto=null
            selectedGif=null
            recordedAudio=null
            locationInfo=null
            Toast.makeText(context, "Posted", Toast.LENGTH_SHORT).show()
            threadViewModel.resetPosted()
            navController.navigate(Routes.Home.routes) {
                popUpTo(Routes.AddThreads.routes) { inclusive = true }

            }

        }
    }
    val gifLauncher = rememberLauncherForActivityResult(GetContent()) {
        it?.let { selectedGif = it }
    }
    val audioLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        result.data?.data?.let { recordedAudio = it }
    }
    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { ok ->
        if (!ok) Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        topBar = {
            TopAppBar(
                title = { Text("New thread", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Routes.Home.routes) {
                            popUpTo(Routes.AddThreads.routes) { inclusive = true }

                        }
                    }) {
                        Icon(Icons.Outlined.Close, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* drafts */ }) {
                        Icon(Icons.Filled.UnfoldMore, null, tint = Color.White)
                    }
                    IconButton(onClick = { /* more */ }) {
                        Icon(Icons.Filled.MoreVert, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Black,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Your followers can reply & quote",
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    if (selectedImage==null){
                        threadViewModel.postThread(text,null)
                    }else{
                        threadViewModel.postThread(text,selectedImage)
                    }
                }) {
                    Text("Post")
                }
            }
        }
    ) { innerPad ->
        Box(
            Modifier
                .padding(innerPad)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Input takes up all space until icons
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    decorationBox = { inner ->
                        if (text.isEmpty()) Text(
                            "What’s new?",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        inner()
                    },
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )

                Spacer(Modifier.height(8.dp))

                // Icon row just above bottom bar
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                            Icon(Icons.Filled.Image, null, tint = Color.Gray)
                        }
                        IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Icon(Icons.Filled.CameraAlt, null, tint = Color.Gray)
                        }
                        IconButton(onClick = { gifLauncher.launch("image/gif") }) {
                            Icon(Icons.Filled.Gif, null, tint = Color.Gray)
                        }
                        IconButton(onClick = {
                            Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION).also {
                                audioLauncher.launch(it)
                            }
                        }) {
                            Icon(Icons.Filled.Mic, null, tint = Color.Gray)
                        }
                        IconButton(onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                locationInfo = "Lat: xx, Lon: yy"
                                Toast.makeText(context, locationInfo, Toast.LENGTH_SHORT).show()
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }) {
                            Icon(Icons.Filled.LocationOn, null, tint = Color.Gray)
                        }
                    }
                    IconButton(onClick = { /* expand thread */ }) {
                        Icon(Icons.Filled.MoreVert, null, tint = Color.Gray)
                    }
                }

                // Previews
                selectedImage?.let { ImagePreview(it) }
                capturedPhoto?.let { ImagePreview(it) }
                selectedGif?.let { ImagePreview(it) }
                recordedAudio?.let { AudioPreview(it) }
            }
        }
    }
}

@Composable
private fun ImagePreview(uri: Uri) {
    Image(
        painter = rememberAsyncImagePainter(uri),
        contentDescription = null,
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp)
    )
}

@Composable
private fun AudioPreview(uri: Uri) {
    Text(
        text = "Audio: ${uri.lastPathSegment}",
        color = Color.White,
        modifier = Modifier.padding(4.dp)
    )
}

