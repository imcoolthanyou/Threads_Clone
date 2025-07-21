package com.appsv.threads.core.navigation
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.appsv.threads.core.navigation.Routes.Profile
import com.appsv.threads.presenatation.screens.AddThreads
import com.appsv.threads.presenatation.screens.BottomNav
import com.appsv.threads.presenatation.screens.Home
import com.appsv.threads.presenatation.screens.Notification
import com.appsv.threads.presenatation.screens.OtherUsers
import com.appsv.threads.presenatation.screens.Profile
import com.appsv.threads.presenatation.screens.Registration
import com.appsv.threads.presenatation.screens.Search
import com.appsv.threads.presenatation.screens.Splash
import com.example.threadsclone.screens.Login

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SetUpNavGraph(navController: NavHostController) {

    NavHost(navController=navController, startDestination = Routes.Splash.routes){


        composable(Routes.Splash.routes) {
            Splash(navController)
        }
        composable(Routes.AddThreads.routes) {
            AddThreads(
                navController
            )
        }
        composable(Routes.Home.routes) {
            Home(navController)
        }
        composable(Routes.Notification.routes) {
            Notification(navController)
        }


        composable(Profile.routes) {
            Profile(navController)
        }
        composable(Routes.Search.routes) {
            Search(navController)
        }

        composable(Routes.BottomNav.routes) {
            BottomNav(navController)
        }

        composable(Routes.Login.routes){
            Login(navController)
        }

        composable(Routes.Register.routes) {
            Registration(navController)
        }
        composable(Routes.OtherUsers.routes) {
            val userId=it.arguments!!.getString("userId")
            OtherUsers(
                navController,
                userId = userId.toString()
            )

        }

    }
}