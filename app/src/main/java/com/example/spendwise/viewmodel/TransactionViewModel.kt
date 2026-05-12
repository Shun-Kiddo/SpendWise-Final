package com.example.spendwise.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwise.data.dao.MonthlyBalanceDao
import com.example.spendwise.data.dao.TransactionDao
import com.example.spendwise.data.entity.TransactionEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val dao: TransactionDao,
    private val firestore: FirebaseFirestore,
    private val monthlyBalanceDao: MonthlyBalanceDao,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _userUid = MutableStateFlow(auth.currentUser?.uid ?: "")
    private val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(java.util.Date())
    val transactions = _userUid.flatMapLatest { uid ->
        dao.getTransactionsByUser(uid)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val currentMonthBalance = monthlyBalanceDao.getCurrentBalance(currentMonth)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    private fun getCurrentUid(): String {
        val uid = auth.currentUser?.uid ?: ""
        _userUid.value = uid
        return uid
    }
    fun fetchDataFromFirestore() {
        val uid = getCurrentUid()
        if (uid.isEmpty()) {
            Log.d("TransactionVM", "No user logged in, fetch skipped.")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("TransactionVM", "Fetching for UID: $uid")
                val result = firestore.collection("transactions")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()

                if (!result.isEmpty) {
                    result.documents.forEach { doc ->
                        val transaction = TransactionEntity(
                            id = doc.getString("id") ?: doc.id,
                            title = doc.getString("title") ?: "",
                            amount = doc.getDouble("amount") ?: 0.0,
                            date = doc.getString("date") ?: "",
                            notes = doc.getString("notes") ?: "",
                            type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE"),
                            userId = doc.getString("userId") ?: uid,
                            synced = true
                        )
                        dao.insertTransaction(transaction)
                    }
                    Log.d("TransactionVM", "Restored ${result.size()} records.")
                } else {
                    Log.d("TransactionVM", "No data found in Cloud for this UID.")
                }
            } catch (e: Exception) {
                Log.e("TransactionVM", "Fetch Error: ${e.message}")
            }
        }
    }
    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {val uid = getCurrentUid()
            val secureTransaction = transaction.copy(userId = uid)
            dao.updateTransaction(secureTransaction)
            syncWithFirebase(secureTransaction)
        }
    }
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                dao.deleteTransaction(transaction)
                val uid = getCurrentUid()
                if (uid.isNotEmpty()) {
                    firestore.collection("transactions")
                        .document(transaction.id)
                        .delete()
                        .await()
                }
            } catch (e: Exception) {
                Log.e("TransactionVM", "Error deleting: ${e.message}")
            }
        }
    }
    private suspend fun syncWithFirebase(transaction: TransactionEntity) {
        val uid = getCurrentUid()
        if (uid.isEmpty()) return

        try {
            val dataToSync = hashMapOf(
                "id" to transaction.id,
                "title" to transaction.title,
                "amount" to transaction.amount,
                "date" to transaction.date,
                "notes" to transaction.notes,
                "type" to transaction.type.name,
                "userId" to uid,
                "synced" to true
            )

            firestore.collection("transactions")
                .document(transaction.id)
                .set(dataToSync)
                .await()

            dao.updateTransaction(transaction.copy(synced = true))
        } catch (e: Exception) {
            Log.e("TransactionVM", "Firestore Sync Failed: ${e.message}")
        }
    }
}