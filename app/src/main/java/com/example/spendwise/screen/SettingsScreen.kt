package com.example.spendwise.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.spendwise.navigation.TopNavBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.spendwise.viewmodel.UserProfile

@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {},
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // State for dynamic user data
    var userData by remember {
        mutableStateOf(UserProfile(
            name = currentUser?.displayName ?: "Loading...",
            phone = "Not Set",
            email = currentUser?.email ?: "No Email",
            work = "Student"
        ))
    }

    // Fetch name and details from Firebase Firestore
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        userData = UserProfile(
                            name = document.getString("name") ?: currentUser.displayName ?: "User",
                            phone = document.getString("phone") ?: "Not Set",
                            email = currentUser.email ?: "No Email",
                            work = document.getString("work") ?: "Student"
                        )
                    }
                }
        }
    }

    // Dialog state variables
    var showTermsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAppGuideDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopNavBar() }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            item {
                Text(
                    text = "User Details",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(label = "Name", value = userData.name)
                        DetailRow(label = "Phone", value = userData.phone)
                        DetailRow(label = "Email", value = userData.email)
                        DetailRow(label = "Work", value = userData.work)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }

            item {
                Text(
                    text = "App Info",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                Button(
                    onClick = { showAppGuideDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667974))
                ) {
                    Text("App Guide", color = Color.White)
                }
            }
            item { Spacer(modifier = Modifier.height(5.dp)) }
            item {
                Button(
                    onClick = { showTermsDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667974))
                ) {
                    Text("Terms & Conditions", color = Color.White)
                }
            }
            item { Spacer(modifier = Modifier.height(5.dp)) }
            item {
                Button(
                    onClick = { showAboutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667974))
                ) {
                    Text("About", color = Color.White)
                }
            }

            // --- LOGOUT BUTTON (Added as per request) ---
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item {
                Button(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Logout", color = Color.White)
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) } // Padding for BottomNav
        }
    }

// ---------------- LOGOUT CONFIRMATION ----------------
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout from SpendWise?") },
            confirmButton = {
                TextButton(onClick = {
                    auth.signOut()
                    onLogout() // Calls the navigation logic to go back to Login
                }) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

// ---------------- APP GUIDE ----------------
    if (showAppGuideDialog) {
        AlertDialog(
            onDismissRequest = { showAppGuideDialog = false },
            title = { Text("App Guide") },
            text = {
                Text(
                    """
                Welcome to SpendWise! Here's how to get started:

                • Tap "Income" to add your income.
                • Tap "Expense" to record an expense.
                • Your dashboard will show monthly summaries and remaining balance.
                • Use the Transactions screen to view, edit, or delete past entries.
                
                Keep track of your spending and make smarter financial decisions!
                """.trimIndent()
                )
            },
            confirmButton = {
                TextButton(onClick = { showAppGuideDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

// ---------------- TERMS & CONDITIONS ----------------
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Conditions") },
            text = {
                Text(
                    """
                By using SpendWise, you agree to the following terms:

                1. SpendWise is for personal finance tracking only.
                2. All data is stored locally on your device.
                3. We are not responsible for any financial decisions made based on app data.
                4. You agree not to misuse the app for any illegal activity.

                For full legal information, please contact the developer.
                """.trimIndent()
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

// ---------------- ABOUT ----------------
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About SpendWise") },
            text = {
                Text(
                    """
                SpendWise v1.0
                
                Developed by Jayson Mancol.
                
                SpendWise is a mobile app designed to help students and individuals track their daily income and expenses. 
                View monthly summaries, analyze spending, and manage your budget efficiently.
                """.trimIndent()
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}