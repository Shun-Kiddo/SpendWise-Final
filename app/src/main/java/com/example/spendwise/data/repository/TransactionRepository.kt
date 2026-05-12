package com.example.spendwise.data.repository
import android.util.Log
import com.example.spendwise.data.dao.TransactionDao
import com.example.spendwise.data.entity.TransactionEntity
import com.example.spendwise.viewmodel.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val currentUid: String
        get() = auth.currentUser?.uid ?: ""

    fun getTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByUser(currentUid)
    }

    suspend fun fetchFromFirestore(userId: String) {
        try {
            val result = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (!result.isEmpty) {
                val remoteEntities = result.documents.mapNotNull { doc ->
                    TransactionEntity(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        date = doc.getString("date") ?: "",
                        notes = doc.getString("notes") ?: "",
                        type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE"),
                        userId = doc.getString("userId") ?: userId,
                        synced = true
                    )
                }

                remoteEntities.forEach { transactionDao.insertTransaction(it) }
                Log.d("Repo", "Successfully restored ${remoteEntities.size} records from Cloud.")
            }
        } catch (e: Exception) {
            Log.e("Repo", "Error fetching from Firestore: ${e.message}")
        }
    }

    suspend fun addTransaction(transaction: TransactionEntity) {
        val secureTransaction = transaction.copy(userId = currentUid)
        transactionDao.insertTransaction(secureTransaction)
        uploadToFirebase(secureTransaction)
    }

    private fun uploadToFirebase(transaction: TransactionEntity) {
        val uid = currentUid
        if (uid.isEmpty()) return

        val dataToSync = hashMapOf(
            "id" to transaction.id,
            "userId" to uid,
            "title" to transaction.title,
            "amount" to transaction.amount,
            "date" to transaction.date,
            "notes" to transaction.notes,
            "type" to transaction.type.name,
            "synced" to true
        )

        firestore.collection("transactions")
            .document(transaction.id)
            .set(dataToSync)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    transactionDao.updateTransaction(transaction.copy(synced = true))
                }
            }
            .addOnFailureListener { e ->
                Log.e("Repo", "Sync failed for ${transaction.title}: ${e.message}")
            }
    }

}