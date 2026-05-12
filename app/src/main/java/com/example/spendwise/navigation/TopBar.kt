package com.example.spendwise.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwise.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar() {

    TopAppBar(
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.spendwise1),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                )

                // Text next to logo
                Text(
                    text = "SpendWise",
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF29CFAE),
            titleContentColor = Color.White
        ),

        modifier = Modifier.height(70.dp)
    )
}