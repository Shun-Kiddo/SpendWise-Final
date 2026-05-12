package com.example.spendwise.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RowScope.SmallCard(
    title: String,
    icon: ImageVector,
    amount: String = "",
    color: Color,
    onClick: () -> Unit
) {

    val iconCircleColor = when (color) {
        Color(0xFFCFDACF) -> Color(0xFF1B5E20) // dark green
        Color(0xFFEED2D2) -> Color(0xFFB71C1C) // dark red
        else -> Color.DarkGray
    }

    Card(
        modifier = Modifier
            .weight(1f)
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon inside circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconCircleColor, shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Texts
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                if (title.isNotEmpty()) {
                    Text(
                        text = title.uppercase(),
                        color = iconCircleColor,
                        fontSize = 12.sp
                    )
                }
                if (amount.isNotEmpty()) {
                    Text(
                        text = amount,
                        color = Color(0xFF333333),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceCard(balance: String,
                //onResetClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF29CFAE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Remaining Balance", color = Color.White, style = MaterialTheme.typography.labelLarge)
                Text(balance, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            }

//            IconButton(onClick = onResetClick) {
//                Icon(
//                    imageVector = Icons.Default.Refresh,
//                    contentDescription = "Reset Balance",
//                    tint = Color.White
//                )
//            }
        }
    }
}
