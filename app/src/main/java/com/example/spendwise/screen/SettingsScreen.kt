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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwise.navigation.TopNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {},
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var userData by remember {
        mutableStateOf(UserProfile(
            name = currentUser?.displayName ?: "Loading...",
            phone = "Not Set",
            email = currentUser?.email ?: "No Email",
            work = "Student"
        ))
    }

    // Modal state for editing
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).addSnapshotListener { document, _ ->
                if (document != null && document.exists()) {
                    userData = UserProfile(
                        name = document.getString("name") ?: "User",
                        phone = document.getString("phone") ?: "Not Set",
                        email = currentUser.email ?: "No Email",
                        work = document.getString("work") ?: "Student"
                    )
                }
            }
        }
    }

    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showAppGuideDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8FAFB),
        topBar = { TopNavBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // --- PROFILE HEADER SECTION ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture Holder
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFF29CFAE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color(0xFF29CFAE)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userData.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E)
                        )
                    )
                    Text(
                        text = userData.work,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit Profile Button
                    Button(
                        onClick = { isEditing = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = borderStroke(1.dp, Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile", color = Color.Black, fontSize = 14.sp)
                    }
                }
            }

            // --- USER DETAILS CARD ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Account Information", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        DetailItem(icon = Icons.Rounded.Phone, label = "Phone", value = userData.phone)
                        DetailItem(icon = Icons.Rounded.Email, label = "Email", value = userData.email)
                        DetailItem(icon = Icons.Rounded.Work, label = "Occupation", value = userData.work)
                    }
                }
            }

            // --- APP INFO SECTION ---
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Support & About", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsActionRow("App Guide", Icons.Rounded.MenuBook) { showAppGuideDialog = true }
                    SettingsActionRow("Terms & Conditions", Icons.Rounded.Gavel) { showTermsDialog = true }
                    SettingsActionRow("About SpendWise", Icons.Rounded.Info) { showAboutDialog = true }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Logout
                    Button(
                        onClick = { showLogoutConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text("Log Out", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- DIALOGS (Logout, Guide, etc. stay same but themed) ---
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to exit?") },
            confirmButton = {
                TextButton(onClick = { auth.signOut(); onLogout() }) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // --- EDIT PROFILE SHEET ---
    if (isEditing) {
        EditProfileDialog(
            currentProfile = userData,
            onDismiss = { isEditing = false },
            onSave = { updatedName, updatedPhone, updatedWork ->
                currentUser?.uid?.let { uid ->
                    val updates = mapOf("name" to updatedName, "phone" to updatedPhone, "work" to updatedWork)
                    db.collection("users").document(uid).update(updates)
                }
                isEditing = false
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
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF29CFAE), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingsActionRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF455A64), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), fontSize = 16.sp)
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.name) }
    var phone by remember { mutableStateOf(currentProfile.phone) }
    var work by remember { mutableStateOf(currentProfile.work) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(name, phone, work) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29CFAE))) {
                Text("Save Changes")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = work, onValueChange = { work = it }, label = { Text("Occupation") }, shape = RoundedCornerShape(12.dp))
            }
        }
    )
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)