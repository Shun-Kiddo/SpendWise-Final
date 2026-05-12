package com.example.spendwise.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spendwise.navigation.TopNavBar
import com.example.spendwise.utils.BalanceCard
import com.example.spendwise.utils.SmallCard
import com.example.spendwise.viewmodel.HomeViewModel
import com.example.spendwise.viewmodel.TransactionViewModel
import com.example.spendwise.viewmodel.Transaction
import com.example.spendwise.viewmodel.TransactionType
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return formatter.format(amount)
}

fun parseDateToMillis(date: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sdf.parse(date)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    transactviewModel: TransactionViewModel = hiltViewModel()

) {
    val savedBalanceEntity by transactviewModel.currentMonthBalance.collectAsState(initial = null)
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())

    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var showDialog by remember { mutableStateOf(false) }
    var showNoIncomeDialog by remember { mutableStateOf(false) }
    var showAlreadyResetDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }

    /* ---------------- FILTER LOGIC ---------------- */

    // 1. Filtered Transactions for Balance (Monthly/After Reset)
    val monthlyTransactions = remember(transactions, savedBalanceEntity) {
        val resetTime = try {
            savedBalanceEntity?.let { 0L } ?: 0L
        } catch (e: Exception) { 0L }

        if (resetTime == 0L) transactions
        else transactions.filter { parseDateToMillis(it.date) > resetTime }
    }

    // 2. Filtered Transactions for UI List (TODAY ONLY)
    val todayTransactions = remember(transactions) {
        transactions.filter { it.date.startsWith(todayDateStr) }
    }

    val incomeTotal = monthlyTransactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val expenseTotal = monthlyTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    val uiBalance = incomeTotal - expenseTotal

    Scaffold(
        topBar = { TopNavBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            BalanceCard(
                balance = "₱ ${formatMoney(uiBalance)}"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallCard(
                    "Income",
                    Icons.Default.ArrowUpward,
                    "₱ ${formatMoney(incomeTotal)}",
                    Color(0xFFCFDACF)
                ) {
                    selectedType = TransactionType.INCOME
                    showDialog = true
                }

                SmallCard(
                    "Expense",
                    Icons.Default.ArrowDownward,
                    "₱ ${formatMoney(expenseTotal)}",
                    Color(0xFFEED2D2)
                ) {
                    selectedType = TransactionType.EXPENSE
                    if (uiBalance <= 0) showNoIncomeDialog = true
                    else showDialog = true
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            TransactionSection(todayTransactions)
        }
    }

    /* ---------------- DIALOGS ---------------- */

    if (showAlreadyResetDialog) {
        AlertDialog(
            onDismissRequest = { showAlreadyResetDialog = false },
            confirmButton = {
                TextButton(onClick = { showAlreadyResetDialog = false }) { Text("OK") }
            },
            title = { Text("Already Reset") },
            text = { Text("You have already reset your balance for this month.") }
        )
    }

    if (showDialog) {
        AddTransactionDialog(
            type = selectedType,
            currentBalance = uiBalance,
            onDismiss = { showDialog = false },
            onSave = { viewModel.addTransaction(it) }
        )
    }

    if (showNoIncomeDialog) {
        AlertDialog(
            onDismissRequest = { showNoIncomeDialog = false },
            confirmButton = {
                TextButton(onClick = { showNoIncomeDialog = false }) { Text("OK") }
            },
            title = { Text("Insufficient Balance") },
            text = { Text("You cannot add an expense because there is no remaining balance or income.") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    type: TransactionType,
    currentBalance: Double,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val suggestions = if (type == TransactionType.INCOME)
        listOf("Salary", "Allowance", "Gift", "Investment")
    else
        listOf("Food", "Transport", "Bills", "Shopping", "Entertainment")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "New ${type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (type == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Category") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF29CFAE),
                            focusedLabelColor = Color(0xFF29CFAE)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        suggestions.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    title = selection
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF29CFAE),
                        focusedLabelColor = Color(0xFF29CFAE)
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF29CFAE),
                        focusedLabelColor = Color(0xFF29CFAE)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull() ?: 0.0

                            if (amountValue <= 0.0) {
                                Toast.makeText(context, "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
                            }
                            else if (type == TransactionType.EXPENSE && amountValue > currentBalance) {
                                Toast.makeText(context, "Insufficient Balance! You cannot exceed ₱${formatMoney(currentBalance)}", Toast.LENGTH_LONG).show()
                            }
                            else {
                                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                                val generatedId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

                                onSave(
                                    Transaction(
                                        id = generatedId.toString(),
                                        title = title,
                                        amount = amountValue,
                                        type = type,
                                        date = timestamp,
                                        notes = notes
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() && amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0.0,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == TransactionType.INCOME) Color(0xFFCFDACF) else Color(0xFFEED2D2),
                            contentColor = if (type == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828),
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionSection(list: List<Transaction>) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(list) {
        isLoading = true
        delay(500)
        isLoading = false
    }

    Column {
        Text("Today's Transactions", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF29CFAE))
                    }
                }
                list.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transactions for today", color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list) { item ->
                            TransactionItem(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(transaction.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                // Pinanatili ang oras sa display
                Text(transaction.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                text = if (transaction.type == TransactionType.INCOME)
                    "+₱ ${formatMoney(transaction.amount)}"
                else
                    "-₱ ${formatMoney(transaction.amount)}",
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}