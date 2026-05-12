package com.example.spendwise.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spendwise.screen.*
import com.example.spendwise.authentication.SignUpScreen
import com.example.spendwise.authentication.SignInScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    val authRoutes = listOf(Screen.Splash.route, "signin", "signup")

    Scaffold(
        bottomBar = {

            if (currentRoute !in authRoutes) {
                BottomNavBar(navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding)
        ) {


            composable(Screen.Splash.route) {
                SplashScreen(
                    navController = navController,
                    onTimeout = {

                        val currentUser = Firebase.auth.currentUser

                        if (currentUser != null) {

                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {

                            navController.navigate("signin") {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable("signin") {
                SignInScreen(
                    onSignInSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo("signin") { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate("signup")
                    }
                )
            }

            composable("signup") {
                SignUpScreen(
                    onSignUpSuccess = {

                        navController.navigate("signin") {
                            popUpTo("signup") { inclusive = true }
                        }
                    },
                    onNavigateToSignIn = {
                        navController.popBackStack()
                    }
                )
            }

            // --- MAIN APP FLOW ---
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Transaction.route) { TransactionScreen() }
            composable(Screen.Summary.route) { SummaryScreen() }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLogout = {

                        Firebase.auth.signOut()

                        navController.navigate("signin") {

                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}