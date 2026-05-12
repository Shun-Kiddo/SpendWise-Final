package com.example.spendwise.screen
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwise.navigation.TopNavBar
import com.example.spendwise.viewmodel.TransactionType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.data.entity.TransactionEntity
import com.example.spendwise.data.roomdb.AppDatabase
import com.example.spendwise.viewmodel.TransactionViewModel
import com.example.spendwise.viewmodel.Transaction
import com.example.spendwise.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

import androidx.hilt.navigation.compose.hiltViewModel
private fun getFormattedDate(calendar: Calendar): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(calendar.time)
}
private fun normalizeDateString(dateStr: String): String {
    val potentialFormats = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    )
    val targetFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    for (format in potentialFormats) {
        try {
            val date = format.parse(dateStr)
            if (date != null) return targetFormat.format(date)
        } catch (e: Exception) {
            continue
        }
    }
    return dateStr
}

private fun getCategoryIcon(title: String): ImageVector {
    return when (title.lowercase()) {
        "food" -> Icons.Rounded.Restaurant
        "salary" -> Icons.Rounded.Payments
        "transport" -> Icons.Rounded.DirectionsCar
        "shopping" -> Icons.Rounded.ShoppingBag
        "bills" -> Icons.Rounded.ReceiptLong
        "allowance" -> Icons.Rounded.AccountBalanceWallet
        else -> Icons.Rounded.Category
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser
    val allTransactions by transactionViewModel.transactions.collectAsState()

    // Date Filtering
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(getFormattedDate(calendar)) }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = getFormattedDate(cal)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    // States
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val currentBalance = remember(allTransactions) {
        allTransactions.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
    }

    val filteredTransactions = remember(searchQuery, allTransactions, selectedDate) {
        allTransactions.filter { tx ->
            val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true) ||
                    tx.notes.contains(searchQuery, ignoreCase = true)
            val matchesDate = normalizeDateString(tx.date) == selectedDate
            matchesSearch && matchesDate
        }
    }

    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    val hiddenByByUser by viewModel.hiddenCategories.collectAsState(initial = emptyList())

    val dynamicCategories = remember(transactions, hiddenByByUser) {
        transactions.map { it.title }
            .distinct()
            .filter { it !in hiddenByByUser }
    }

    LaunchedEffect(currentUser) { if (currentUser != null) transactionViewModel.fetchDataFromFirestore() }

    Scaffold(
        containerColor = Color(0xFFF8FAFB),
        topBar = { TopNavBar() }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Search Bar
            Box(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search transactions...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF29CFAE),
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("History", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black))
                    Text(
                        text = if (selectedDate == getFormattedDate(Calendar.getInstance())) "Today" else selectedDate,
                        color = Color(0xFF29CFAE),
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    onClick = { datePickerDialog.show() },
                    shape = CircleShape,
                    color = Color(0xFF29CFAE).copy(alpha = 0.1f)
                ) {
                    Icon(Icons.Rounded.Event, null, tint = Color(0xFF29CFAE), modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List
            Box(modifier = Modifier.weight(1f)) {
                if (filteredTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transactions found", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp)
                    ) {
                        items(filteredTransactions, key = { it.id }) { transaction ->
                            SwipeableTransactionCard(
                                transaction = transaction,
                                onEdit = {
                                    selectedTransaction = transaction
                                    showEditDialog = true
                                },
                                onDelete = {
                                    transactionToDelete = transaction
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmationDialog(
            onConfirm = {
                transactionToDelete?.let { transactionViewModel.deleteTransaction(it) }
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (showEditDialog && selectedTransaction != null) {
        EditTransactionDialog(
            transaction = selectedTransaction!!,
            currentBalance = currentBalance,
            existingCategories = dynamicCategories,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                transactionViewModel.updateTransaction(updated)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionCard(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onEdit(); false }
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF29CFAE)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(color).padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = if (direction == SwipeToDismissBoxValue.StartToEnd) Icons.Rounded.Edit else Icons.Rounded.Delete,
                    contentDescription = null, tint = Color.White
                )
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                val isIncome = transaction.type == TransactionType.INCOME
                Box(
                    modifier = Modifier.size(48.dp).background(
                        color = if (isIncome) Color(0xFF43A047).copy(0.1f) else Color(0xFFE53935).copy(0.1f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.title),
                        contentDescription = null,
                        tint = if (isIncome) Color(0xFF43A047) else Color(0xFFE53935)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(transaction.title, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E), fontSize = 16.sp)
                    Text(transaction.date, fontSize = 12.sp, color = Color.Gray)
                }
                Text(
                    text = (if (isIncome) "+" else "-") + "₱${formatMoney(transaction.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) Color(0xFF43A047) else Color(0xFF1A1C1E),
                    fontSize = 16.sp
                )
            }
        }
    }
}


@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Transaction", fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to permanently delete this record? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    currentBalance: Double,
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(transaction.title) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var notes by remember { mutableStateOf(transaction.notes) }
    var expanded by remember { mutableStateOf(false) }

    val defaultSuggestions = if (transaction.type == TransactionType.INCOME)
        listOf("Salary", "Allowance", "Gift", "Investment")
    else
        listOf("Food", "Transport", "Bills", "Shopping", "Entertainment")

    // Filter logic: standard suggestions + previous history - hidden items
    val finalSuggestions = remember(title, existingCategories) {
        (defaultSuggestions + existingCategories)
            .distinct()
            .filter { it.contains(title, ignoreCase = true) }
    }
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
                Column {
                    Text(
                        text = "Edit ${transaction.type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (transaction.type == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Category") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF29CFAE),
                            focusedLabelColor =  Color(0xFF29CFAE)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        finalSuggestions.forEach { selection ->
                            DropdownMenuItem(
                                text = {
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                        Text(selection)
                                    }
                                },
                                onClick = { title = selection; expanded = false }
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
                            val newAmount = amount.toDoubleOrNull() ?: 0.0
                            val oldAmount = transaction.amount

                            val projectedBalance = if (transaction.type == TransactionType.INCOME) {
                                (currentBalance - oldAmount) + newAmount
                            } else {
                                (currentBalance + oldAmount) - newAmount
                            }

                            if (projectedBalance < 0) {
                                Toast.makeText(context, "Insufficient Balance!.", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            if (newAmount <= 0.0) {
                                Toast.makeText(context, "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
                            } else {
                                val updatedTransaction = transaction.copy(
                                    title = title,
                                    amount = newAmount,
                                    notes = notes
                                )
                                onSave(updatedTransaction)
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() && amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0.0,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (transaction.type == TransactionType.INCOME) Color(0xFFCFDACF) else Color(0xFFEED2D2),
                            contentColor = if (transaction.type == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828),
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "Update",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}