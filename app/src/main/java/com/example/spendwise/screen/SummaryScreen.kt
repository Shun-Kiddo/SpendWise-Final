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
import android.app.DatePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Event
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current
    val transactionEntities by transactionViewModel.transactions.collectAsState(initial = emptyList())
    val transactions = remember(transactionEntities) { transactionEntities.map { it.toTransaction() } }

    var isAllTime by remember { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance() }
    var selectedDateStr by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            isAllTime = false
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val displayTransactions = remember(transactions, isAllTime, selectedDateStr) {
        if (isAllTime) transactions
        else transactions.filter { it.date.contains(selectedDateStr) }
    }

    val incomeVal = displayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expenseVal = displayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    Scaffold(
        containerColor = Color(0xFFF8FAFB),
        topBar = { TopNavBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isAllTime) "All-Time Analysis" else "Daily Analysis",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C1E)
                            )
                        )
                        if (!isAllTime) {
                            Text(
                                text = selectedDateStr,
                                color = Color(0xFF29CFAE),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Row {

                        IconButton(
                            onClick = { isAllTime = !isAllTime },
                            modifier = Modifier.background(
                                if (isAllTime) Color(0xFF29CFAE) else Color.White,
                                CircleShape
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = "All History",
                                tint = if (isAllTime) Color.White else Color.Gray
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        IconButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Event,
                                contentDescription = "Pick Date",
                                tint = if (!isAllTime) Color(0xFF29CFAE) else Color.Gray
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                DailyDoughnutCard(incomeVal, expenseVal)
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
                CategoryBreakdownCard(displayTransactions)
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
                    drawCircle(Color(0xFFF1F3F4), style = Stroke(strokeWidth))

                    drawArc(
                        color = Color(0xFFE53935).copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )

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

                expenseData.forEachIndexed { index, (category, amount) ->
                    val color = SummaryCategoryColors[index % SummaryCategoryColors.size]

                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                                Spacer(Modifier.width(12.dp))
                                Text(category, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
                            }

                            val percentage = ((amount / totalExpense) * 100).toInt()
                            Text("$percentage%", fontWeight = FontWeight.Black, color = color)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (amount / totalExpense).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = color,
                            trackColor = Color(0xFFF1F3F4),
                            strokeCap = StrokeCap.Round
                        )

                        Text(
                            text = "₱${localFormatMoney(amount)} spent",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
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

private fun localFormatMoney(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale.US).format(amount)
}