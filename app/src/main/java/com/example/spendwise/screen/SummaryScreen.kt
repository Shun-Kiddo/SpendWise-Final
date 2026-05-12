package com.example.spendwise.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spendwise.data.toTransaction
import com.example.spendwise.navigation.TopNavBar
import com.example.spendwise.viewmodel.Transaction
import com.example.spendwise.viewmodel.TransactionType
import com.example.spendwise.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Scoped to this file only to avoid "Overload resolution ambiguity"
private val SummaryCategoryColors = listOf(
    Color(0xFF29CFAE), // Primary Teal
    Color(0xFF4A71C0), // Blue
    Color(0xFFE9883A), // Orange
    Color(0xFFF1B71C), // Yellow
    Color(0xFF8E44AD), // Purple
    Color(0xFFE53935)  // Red
)

@Composable
fun SummaryScreen(
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {

    val transactionEntities by transactionViewModel.transactions.collectAsState(initial = emptyList())
    val transactions = remember(transactionEntities) { transactionEntities.map { it.toTransaction() } }

    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayTransactions = transactions.filter { it.date.contains(todayDate) }
    val incomeToday = todayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expenseToday = todayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    Scaffold(
        containerColor = Color(0xFFF8FAFB),
        topBar = { TopNavBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Daily Analysis",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C1E)
                    )
                )
                Spacer(Modifier.height(16.dp))
                DailyDoughnutCard(incomeToday, expenseToday)
            }

            item {
                Text(
                    text = "Expense Breakdown",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C1E)
                    )
                )
                Spacer(Modifier.height(16.dp))
                CategoryBreakdownCard(transactions)
            }
        }
    }
}

@Composable
fun DailyDoughnutCard(income: Double, expense: Double) {
    val totalFlow = income + expense
    val incomeProportion = if (totalFlow == 0.0) 0f else (income / totalFlow).toFloat()

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 45f
                    // Background Track
                    drawCircle(Color(0xFFF1F3F4), style = Stroke(strokeWidth))

                    // Expense Arc (Red Base)
                    drawArc(
                        color = Color(0xFFE53935).copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )

                    // Income Arc (Teal Overlay)
                    drawArc(
                        color = Color(0xFF29CFAE),
                        startAngle = -90f,
                        sweepAngle = incomeProportion * 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Flow", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        "₱${localFormatMoney(totalFlow)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                SummaryLegendItem("Income", income, Color(0xFF29CFAE))
                SummaryLegendItem("Expense", expense, Color(0xFFE53935))
            }
        }
    }
}

@Composable
fun CategoryBreakdownCard(transactions: List<Transaction>) {
    val expenseData = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.title.lowercase().replaceFirstChar { it.uppercase() } }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .toList().sortedByDescending { it.second }
    }
    val totalExpense = expenseData.sumOf { it.second }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            if (expenseData.isEmpty()) {
                Text("No expenses tracked yet", color = Color.Gray, modifier = Modifier.padding(16.dp))
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        var currentStartAngle = -90f
                        expenseData.forEachIndexed { index, data ->
                            val sweep = (data.second / totalExpense * 360f).toFloat()
                            drawArc(
                                color = SummaryCategoryColors[index % SummaryCategoryColors.size],
                                startAngle = currentStartAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(35f, cap = StrokeCap.Round)
                            )
                            currentStartAngle += sweep
                        }
                    }
                    Text(
                        "₱${localFormatMoney(totalExpense)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                expenseData.forEachIndexed { index, data ->
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SummaryCategoryColors[index % SummaryCategoryColors.size]))
                        Spacer(Modifier.width(12.dp))
                        Text(data.first, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, color = Color(0xFF1A1C1E))
                        Text("₱${localFormatMoney(data.second)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryLegendItem(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
        Text("₱${localFormatMoney(amount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// Renamed and marked private to prevent conflicts
private fun localFormatMoney(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale.US).format(amount)
}