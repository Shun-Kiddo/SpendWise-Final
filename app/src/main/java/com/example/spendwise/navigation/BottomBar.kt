package com.example.spendwise.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        Triple(Screen.Home, Icons.Default.Home, "Home"),
        Triple(Screen.Transaction, Icons.AutoMirrored.Filled.List, "Activity"),
        Triple(Screen.Chatbot, Icons.Default.SmartToy, "Assistant"),
        Triple(Screen.Summary, Icons.Default.Assessment, "Summary"),
        Triple(Screen.Settings, Icons.Default.Settings, "Settings")
    )

    NavigationBar(
        containerColor = Color(0xFF29CFAE),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        tonalElevation = 0.dp
    ) {
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry?.destination?.route

        items.forEach { (screen, icon, label) ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color(0xFF29CFAE) else Color.White
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }
}