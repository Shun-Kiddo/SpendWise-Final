package com.example.spendwise.screen
import android.app.DatePickerDialog
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
@Composable
fun TransactionScreen(
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser

    // Restore data from Cloud on login
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            transactionViewModel.fetchDataFromFirestore()
        }
    }

    val allTransactions by transactionViewModel.transactions.collectAsState()

    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(getFormattedDate(calendar)) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = getFormattedDate(cal)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val currentBalance = remember(allTransactions) {
        allTransactions.sumOf {
            if (it.type == TransactionType.INCOME) it.amount else -it.amount
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val filteredTransactions = remember(searchQuery, allTransactions, selectedDate) {
        allTransactions.filter { tx ->
            val matchesSearch = if (searchQuery.isBlank()) true
            else tx.title.contains(searchQuery, ignoreCase = true) ||
                    tx.notes.contains(searchQuery, ignoreCase = true) ||
                    tx.amount.toString().contains(searchQuery)

            val txNormalizedDate = normalizeDateString(tx.date)
            val matchesDate = txNormalizedDate == selectedDate

            matchesSearch && matchesDate
        }
    }

    LaunchedEffect(allTransactions) {
        isLoading = true
        kotlinx.coroutines.delay(500)
        isLoading = false
    }

    Scaffold(
        topBar = { TopNavBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                shape = RoundedCornerShape(20.dp),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by keyword") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF29CFAE),
                    focusedLabelColor = Color(0xFF29CFAE)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("All Transactions", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = if (selectedDate == getFormattedDate(Calendar.getInstance())) "Today ($selectedDate)" else selectedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF29CFAE),
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Filter by Date",
                        tint = Color(0xFF29CFAE)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF29CFAE))
                        }
                    }
                    filteredTransactions.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No transactions for $selectedDate", color = Color.Gray)
                        }
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredTransactions, key = { it.id }) { transaction ->
                                TransactionCard(
                                    transaction = transaction,
                                    onEdit = {
                                        selectedTransaction = transaction
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        transactionViewModel.deleteTransaction(transaction)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && selectedTransaction != null) {
        EditTransactionDialog(
            transaction = selectedTransaction!!,
            currentBalance = currentBalance,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                transactionViewModel.updateTransaction(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isLoading: Boolean = false
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    if (transaction.notes.isNotBlank()) {
                        Text(
                            text = transaction.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (transaction.type == TransactionType.INCOME)
                            "+₱ ${formatMoney(transaction.amount)}"
                        else
                            "-₱ ${formatMoney(transaction.amount)}",
                        color = if (transaction.type == TransactionType.INCOME)
                            Color(0xFF2E7D32)
                        else
                            Color(0xFFC62828),
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color.Gray
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    expanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    expanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    currentBalance: Double,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(transaction.title) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var notes by remember { mutableStateOf(transaction.notes) }
    var expanded by remember { mutableStateOf(false) }

    val suggestions = if (transaction.type == TransactionType.INCOME)
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