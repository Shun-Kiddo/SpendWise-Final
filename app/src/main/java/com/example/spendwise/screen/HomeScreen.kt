package com.example.spendwise.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spendwise.viewmodel.HomeViewModel
import com.example.spendwise.viewmodel.TransactionViewModel
import com.example.spendwise.viewmodel.Transaction
import com.example.spendwise.viewmodel.TransactionType
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.spendwise.navigation.TopNavBar

val PrimaryTeal = Color(0xFF29CFAE)
val BackgroundColor = Color(0xFFF8FAFB)
val CardWhite = Color(0xFFFFFFFF)
val TextDark = Color(0xFF1A1C1E)
val IncomeGreen = Color(0xFF43A047)
val ExpenseRed = Color(0xFFE53935)

fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 2
    return formatter.format(amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    transactviewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var showDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }

    val hiddenByByUser by viewModel.hiddenCategories.collectAsState()

    val dynamicCategories = remember(transactions, hiddenByByUser) {
        transactions.map { it.title }
            .distinct()
            .filter { it !in hiddenByByUser }
    }

    val incomeTotal = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expenseTotal = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val uiBalance = incomeTotal - expenseTotal

    val todayTransactions = transactions.filter { it.date.startsWith(todayDateStr) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = { TopNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedType = TransactionType.EXPENSE
                    showDialog = true
                },
                containerColor = ExpenseRed,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            HeaderSection(
                balance = uiBalance,
                income = incomeTotal,
                expense = expenseTotal,
                onAddIncomeClick = {
                    selectedType = TransactionType.INCOME
                    showDialog = true
                }
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )
                Text(
                    "Today",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (todayTransactions.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(todayTransactions) { transaction ->
                            ModernTransactionItem(transaction)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddTransactionDialog(
            type = selectedType,
            currentBalance = uiBalance,
            existingCategories = dynamicCategories,
            onDismiss = { showDialog = false },
            onSave = { viewModel.addTransaction(it) },
            onDeleteCategory = { categoryTitle ->
                viewModel.hideCategoryFromSuggestions(categoryTitle)
            }
        )
    }
}

@Composable
fun HeaderSection(
    balance: Double,
    income: Double,
    expense: Double,
    onAddIncomeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    color = PrimaryTeal,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total Balance",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    Text(
                        "₱ ${formatMoney(balance)}",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                IconButton(
                    onClick = onAddIncomeClick,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                        .size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Summary Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(110.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem(
                    Modifier.weight(1f),
                    "Total Income",
                    income,
                    Icons.Rounded.ArrowUpward,
                    IncomeGreen
                )
                Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.5f).background(Color(0xFFF5F5F5)))
                SummaryItem(
                    Modifier.weight(1f),
                    "Total Expenses",
                    expense,
                    Icons.Rounded.ArrowDownward,
                    ExpenseRed
                )
            }
        }
    }
}

@Composable
fun SummaryItem(modifier: Modifier, label: String, amount: Double, icon: ImageVector, color: Color) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, size = 20.dp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(
                "₱${formatMoney(amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
    }
}

@Composable
fun ModernTransactionItem(transaction: Transaction) {
    val isIncome = transaction.type == TransactionType.INCOME
    val icon = when (transaction.title.lowercase()) {
        "food" -> Icons.Rounded.Restaurant
        "transport" -> Icons.Rounded.DirectionsCar
        "salary" -> Icons.Rounded.Payments
        "shopping" -> Icons.Rounded.ShoppingBag
        else -> Icons.Rounded.Category
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(BackgroundColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if(isIncome) IncomeGreen else ExpenseRed
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, fontWeight = FontWeight.Bold, color = TextDark)
                Text(
                    text = try {
                        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = parser.parse(transaction.date)
                        parser.format(date!!)
                    } catch (e: Exception) {
                        transaction.date.split(" ").first()
                    },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = (if (isIncome) "+" else "-") + "₱${formatMoney(transaction.amount)}",
                fontWeight = FontWeight.ExtraBold,
                color = if (isIncome) Color(0xFF43A047) else Color(0xFFE53935),
                fontSize = 16.sp
            )
        }
    }
}


@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No transactions yet today", color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    type: TransactionType,
    currentBalance: Double,
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val defaultSuggestions = if (type == TransactionType.INCOME)
        listOf("Salary", "Allowance", "Gift", "Investment")
    else
        listOf("Food", "Transport", "Bills", "Shopping", "Entertainment")

    val finalSuggestions = remember(title, existingCategories) {
        (defaultSuggestions + existingCategories)
            .distinct()
            .filter { it.contains(title, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "New ${type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; expanded = true },
                        label = { Text("Category") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal, focusedLabelColor = PrimaryTeal),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )

                    if (finalSuggestions.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            finalSuggestions.forEach { selection ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(selection, modifier = Modifier.weight(1f))

                                            // Only show delete icon for custom (non-default) items
                                            if (selection !in defaultSuggestions) {
                                                IconButton(
                                                    onClick = {
                                                        onDeleteCategory(selection)
                                                        // Close dropdown to refresh state
                                                        expanded = false
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.DeleteOutline,
                                                        contentDescription = "Delete",
                                                        tint = Color.Red.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        title = selection
                                        expanded = false
                                    }
                                )
                            }
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
                    prefix = { Text("₱ ") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal, focusedLabelColor = PrimaryTeal)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal, focusedLabelColor = PrimaryTeal)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }

                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val isAmountValid = amountValue > 0.0
                    val hasEnoughBalance = if (type == TransactionType.EXPENSE) {
                        amountValue <= currentBalance
                    } else {
                        true
                    }

                    Button(
                        onClick = {
                            if (isAmountValid && hasEnoughBalance) {
                                onSave(
                                    Transaction(
                                        id = UUID.randomUUID().toString(),
                                        title = title,
                                        amount = amountValue,
                                        type = type,
                                        date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                                        notes = notes
                                    )
                                )
                                onDismiss()
                            }
                        },
                        // The button will be grayed out if any condition fails
                        enabled = title.isNotBlank() && isAmountValid && hasEnoughBalance,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            disabledContainerColor = Color.LightGray // Visual hint it's disabled
                        )
                    ) {
                        Text(
                            text = if (!hasEnoughBalance && type == TransactionType.EXPENSE)
                                "Low Balance"
                            else
                                "Save Transaction",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Icon(icon: ImageVector, contentDescription: String?, tint: Color, size: Dp) {
    Icon(icon, contentDescription, modifier = Modifier.size(size), tint = tint)
}


@Preview(showBackground = true, device = "id:pixel_8")
@Composable
fun ModernHomeScreenPreview() {
    MaterialTheme {
        // Mock Data for Preview
        val mockTransactions = listOf(
            Transaction(
                id = "1",
                title = "Salary",
                amount = 45000.0,
                type = TransactionType.INCOME,
                date = "2026-05-13 08:00",
                notes = "Monthly Pay"
            ),
            Transaction(
                id = "2",
                title = "Food",
                amount = 1250.50,
                type = TransactionType.EXPENSE,
                date = "2026-05-13 12:30",
                notes = "Lunch with team"
            ),
            Transaction(
                id = "3",
                title = "Transport",
                amount = 350.0,
                type = TransactionType.EXPENSE,
                date = "2026-05-13 17:15",
                notes = "Grab ride"
            ),
            Transaction(
                id = "4",
                title = "Shopping",
                amount = 2100.0,
                type = TransactionType.EXPENSE,
                date = "2026-05-13 19:00",
                notes = "New shoes"
            )
        )

        Scaffold(
            containerColor = BackgroundColor,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                    containerColor = PrimaryTeal,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header with Gradient and Summary Card
                item {
                    HeaderSection(
                        balance = 41299.50,
                        income = 45000.0,
                        expense = 3700.50,
                        onAddIncomeClick = {} // Empty lambda for preview
                    )
                }

                // Section Title
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Activity",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        )
                        Text(
                            "Today",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }

                // List of Transactions
                items(mockTransactions) { transaction ->
                    ModernTransactionItem(transaction)
                }
            }
        }
    }
}