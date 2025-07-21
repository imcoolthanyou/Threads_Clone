package com.appsv.threads.core.navigation

sealed class Routes(var routes: String) {

    object Home:Routes("home")
    object Notification:Routes("notification")
    object AddThreads:Routes("add_threads")
    object Profile:Routes("profile")
    object Search:Routes("search")
    object Splash:Routes("splash")
    object BottomNav:Routes("bottom_nav")
    object Login:Routes("login")
    object Register:Routes("register")
    object OtherUsers:Routes("other_user/{userId}")
}