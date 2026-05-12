package com.example.spendwise.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spendwise.R
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.scale

@Composable
fun SplashScreen(
    navController: NavController,
    onTimeout: () -> Unit = {}
) {

    // Animation state
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        onTimeout()

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF29CFAE)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.spendwise),
            contentDescription = "Logo",
            modifier = Modifier
                .size(230.dp)
                .scale(scale)
        )
    }
}
