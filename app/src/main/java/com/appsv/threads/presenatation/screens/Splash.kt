package com.appsv.threads.presenatation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout

import androidx.navigation.NavHostController
import com.appsv.threads.R
import com.appsv.threads.core.navigation.Routes
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@Composable
fun Splash(navController: NavHostController) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (image) = createRefs()

        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(16.dp) // Adding shadow
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    LaunchedEffect(true) {
        delay(3000)

        if (FirebaseAuth.getInstance().currentUser!=null){
            navController.navigate("bottom_nav"){
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop=true
            }

        }else{
            navController.navigate("login"){
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop=true
            }
        }

    }
}