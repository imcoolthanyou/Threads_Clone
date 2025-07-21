package com.appsv.threads.presenatation.screens
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appsv.threads.core.model.BottomNavItem
import com.appsv.threads.core.navigation.Routes
import com.appsv.threads.core.navigation.Routes.Home


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun BottomNav(navController: NavHostController) {

    val navcontroller_1= rememberNavController()

    Scaffold(
        bottomBar = {MyBottomNavigation(
            navController1= navcontroller_1,

        )}
    ) { innerPadding->

        NavHost(navController=navcontroller_1,startDestination = Routes.Home.routes,
            modifier = Modifier.padding(innerPadding)){
            composable(route=Routes.Home.routes) {
                Home(navController)
            }
            composable(route=Routes.Notification.routes) {
                Notification(navController)
            }
            composable(route=Routes.Search.routes) {
                Search(navController)
            }
            composable(route=Routes.AddThreads.routes) {
                AddThreads(
                    navcontroller_1
                )
            }
            composable(route=Routes.Profile.routes) {
               Profile(navController)
            }

        }
    }
    
}

@Composable
fun MyBottomNavigation(navController1: NavHostController) {

    val backStackEntry=navController1.currentBackStackEntryAsState()


    val list=listOf(
        BottomNavItem("Home",Routes.Home.routes,Icons.Default.Home),
        BottomNavItem("Search",Routes.Search.routes,Icons.Default.Search),
        BottomNavItem("Add",Routes.AddThreads.routes,Icons.Default.Add),
        BottomNavItem("Notification",Routes.Notification.routes,Icons.Default.Notifications),
        BottomNavItem("Profile",Routes.Profile.routes,Icons.Default.Person)

    )

    BottomAppBar {
        list.forEach {
            val selected=it.route==backStackEntry.value?.destination?.route

            NavigationBarItem(selected = selected, onClick = {
                navController1.navigate(it.route){
                    popUpTo(navController1.graph.findStartDestination().id){
                        saveState=true
                    }
                    launchSingleTop=true

                }
            }, icon = {
                Icon(imageVector = it.icon, contentDescription = it.title)
            })
        }


    }
}