package com.example.spendwise.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwise.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar() {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var userName by remember {
        mutableStateOf("Loading...")
    }

    LaunchedEffect(Unit) {

        currentUser?.uid?.let { uid ->

            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->

                    userName = document.getString("name") ?: "Unknown"

                }
        }
    }

    // Generate initials
    val initials = userName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .joinToString("")
        .take(2)
        .uppercase()

    TopAppBar(

        windowInsets = WindowInsets(0, 0, 0, 0),

        title = {

            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.spendwise1),
                    contentDescription = "Logo",
                    modifier = Modifier.size(50.dp)
                )

                Text(
                    text = "SpendWise",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },

        actions = {

            Box(
                modifier = Modifier
                    .padding(top = 16.dp, end = 15.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = initials,
                    color = Color(0xFF29CFAE),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF29CFAE),
            titleContentColor = Color.White
        ),

        modifier = Modifier.height(72.dp)
    )
}