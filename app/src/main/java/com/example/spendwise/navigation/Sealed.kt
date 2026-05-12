package com.example.spendwise.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Transaction : Screen("transaction")
    object Summary : Screen("summary")
    object Settings : Screen("settings")
}