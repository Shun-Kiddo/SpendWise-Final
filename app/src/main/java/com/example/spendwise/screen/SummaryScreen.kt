package com.example.spendwise.screen
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spendwise.data.toTransaction
import com.example.spendwise.navigation.TopNavBar
import com.example.spendwise.viewmodel.Transaction
import com.example.spendwise.viewmodel.TransactionType
import com.example.spendwise.viewmodel.TransactionViewModel
import com.example.spendwise.data.roomdb.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
val CategoryColors = listOf(
    Color(0xFF4A71C0), // Blue
    Color(0xFFE9883A), // Orange
    Color(0xFFF1B71C), // Yellow
    Color(0xFF70AD47), // Green
    Color(0xFF8E44AD), // Purple
    Color(0xFF16A085)  // Teal
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
        topBar = { TopNavBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Daily Summary",
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                DailyPizzaCard(incomeToday, expenseToday)
                Spacer(Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "All Expenses",
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                CategoryPizzaCard(transactions)
            }
        }
    }
}
@Composable
fun DailyPizzaCard(income: Double, expense: Double) {
    val textPaint = remember {
        Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = 34f
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                val total = if (income + expense == 0.0) 1.0 else income + expense
                val incomeAngle = (income / total * 360f).toFloat()
                val expenseAngle = (expense / total * 360f).toFloat()

                // Income Slice (Green)
                if (income > 0 || (income == 0.0 && expense == 0.0)) {
                    val sweep = if (income == 0.0 && expense == 0.0) 180f else incomeAngle
                    drawArc(Color(0xF257B65A), -90f, sweep, true, style = Fill)

                    val rad = Math.toRadians((-90f + sweep / 2f).toDouble())
                    drawContext.canvas.nativeCanvas.drawText(
                        "Income",
                        center.x + (radius * 0.6f * cos(rad)).toFloat(),
                        center.y + (radius * 0.6f * sin(rad)).toFloat() + 10f,
                        textPaint
                    )
                }

                // Expense Slice (Red)
                if (expense > 0 || (income == 0.0 && expense == 0.0)) {
                    val start = if (income == 0.0 && expense == 0.0) 90f else -90f + incomeAngle
                    val sweep = if (income == 0.0 && expense == 0.0) 180f else expenseAngle
                    drawArc(Color(0xFFC24949), start, sweep, true, style = Fill)

                    val rad = Math.toRadians((start + sweep / 2f).toDouble())
                    drawContext.canvas.nativeCanvas.drawText(
                        "Expense",
                        center.x + (radius * 0.6f * cos(rad)).toFloat(),
                        center.y + (radius * 0.6f * sin(rad)).toFloat() + 10f,
                        textPaint
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryPizzaCard(transactions: List<Transaction>) {
    val expenseData = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.title.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .toList().sortedByDescending { it.second }
    }
    val totalExpense = expenseData.sumOf { it.second }

    val textPaint = remember {
        Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = 32f
            isAntiAlias = true
            color = android.graphics.Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                var startAngle = -90f

                if (expenseData.isEmpty()) {
                    drawCircle(Color.LightGray.copy(alpha = 0.3f), radius, center, style = Fill)
                }

                expenseData.forEachIndexed { index, data ->
                    val sweepAngle = if (totalExpense > 0) (data.second / totalExpense * 360f).toFloat() else 0f

                    if (sweepAngle > 0) {
                        // Color Slice
                        drawArc(CategoryColors[index % CategoryColors.size], startAngle, sweepAngle, true, style = Fill)
                        // White Divider Border
                        drawArc(Color.White, startAngle, sweepAngle, true, style = Stroke(width = 4f))

                        // Text Label Logic
                        val rad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                        val textX = center.x + (radius * 0.65f * cos(rad)).toFloat()
                        val textY = center.y + (radius * 0.65f * sin(rad)).toFloat()
                        drawContext.canvas.nativeCanvas.drawText(data.first, textX, textY + 10f, textPaint)

                        startAngle += sweepAngle
                    }
                }
            }
        }
    }
}